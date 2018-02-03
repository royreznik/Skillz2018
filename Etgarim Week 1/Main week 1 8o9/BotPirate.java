package bots;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pirates.Asteroid;
import pirates.Location;
import pirates.MapObject;
import pirates.Mothership;
import pirates.Pirate;
import pirates.PirateGame;

//Class that represent a single Pirate
public class BotPirate {
    // INIT the game Variable
    private static final PirateGame game;

    private static int CapsulerCounter = -1;

    static {
        game = MyBot.gameInstance;
    }

    Pirate pirate;
    MapObject destination;
    Job job;
    boolean didPush;

    // Constructor
    public BotPirate(Pirate pirate) {
        this.pirate = pirate;
        this.destination = null;
        this.job = Job.CAPSULER;
        this.didPush = false;
    }

    public String toString() {
        return "ID: ";
    }

    // Getters & Setters
    public Pirate getPirate() {
        return pirate;
    }

    public void setPirate(Pirate pirate) {
        this.pirate = pirate;
    }

    public MapObject getDestination() {
        return destination;
    }

    public void setDestination(MapObject destination) {
        this.destination = destination;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public boolean isDidPush() {
        return didPush;
    }

    public void setDidPush(boolean didPush) {
        this.didPush = didPush;
    }

    // Stupid method that pushes anytime any enemy.
    public void tryPush() {
        // Go over all enemies.
        for (Pirate enemy : game.getEnemyLivingPirates()) {
            // Check if the pirate can push the enemy.
            if (pirate.canPush(enemy)) {
                // Push the enemy!
                pirate.push(enemy, enemy.initialLocation);

                // Print a message.
                System.out.println("pirate " + pirate + " pushes " + enemy + " towards " + enemy.initialLocation);

                // Did push.
                didPush = true;
            }
        }
    }

    /**
     * @param p
     *            - AnyPirate
     * @param obj
     *            - AnyMapObject
     *            <p>
     *            This methods will try to push p in the obj location
     */
    public void tryPushPirate(Pirate p, MapObject obj) {
        if (pirate.canPush(p)) {
            // Push the enemy!
            pirate.push(p, obj.getLocation());
            // Did push.
            didPush = true;
        }
    }

    public void tryPushAstroid(Asteroid a, MapObject obj) {
        if (pirate.canPush(a)) {
            // Push the enemy!
            pirate.push(a, obj.getLocation());
            // Did push.
            didPush = true;
        }
    }

    // Sail to the Destination
    public void sailToDest() {
        if (!pirate.isAlive()) {
            this.job = Job.DEAD;
            return;
        }
        if (this.didPush) {
            game.debug("Already push");
            return;
        }
        if (game.getLivingAsteroids().length > 0) {
            tryPushAstroid(game.getLivingAsteroids()[0], game.getMyMotherships()[0]);
            if (didPush) {
                return;
            }
        }
        if (this.destination == null) {
            game.debug("No Dest");
            return;
        }
        // should add: if (pirate.getLocation() != destination) ???
        pirate.sail(destination);
    }

    /**
     * Method is written in class BotPirate because it is going to be used in
     * Anti-Camper Strategy
     *
     * @return the location of the nearest wall/border to the pirate. Example - If
     *         the method runs on a Pirate at (100,4600) the method will return
     *         (0,4600) - a straight line towards the nearest wall.
     */
    public Location nearestWall() {
        int row = this.pirate.location.row;
        int col = this.pirate.location.col;
        // TODO - change 0&6400 to out of bounds values -> -1&6401 (?)
        if (row < 3200) {
            if (col < 3200) {
                if (col < row)
                    return new Location(row, 0);
                return new Location(0, col);
            }
            if (row < 6400 - col)
                return new Location(0, col);
            return new Location(row, 6400);
        }

        if (col < 3200) {
            if (col < 6400 - row)
                return new Location(row, 0);
            return new Location(6400, col);
        }

        if (6400 - col < 6400 - row)
            return new Location(row, 6400);
        return new Location(6400, col);
    }

    /**
     * @return the first capsuler he finds or if there isn't a capsuler return
     *         closest pirate to a capsule
     */

    public Pirate getMyCapsuler() {
        int dist = 6400;
        for (Pirate p : game.getMyLivingPirates()) {
            if (p.hasCapsule())
                return p;
        }
        Pirate closest = game.getMyLivingPirates()[0];
        for (int i = 0; i < game.getMyCapsules().length; i++) {

            for (Pirate p : game.getMyLivingPirates()) {
                if (p.distance(game.getMyCapsules()[i]) < closest.distance(game.getMyCapsules()[i])
                        && p.distance(game.getMyCapsules()[i]) < dist) {
                    dist = p.distance(game.getMyCapsules()[i]);
                    closest = p;
                }
            }

        }
        return closest;
    }

    /**
     * @return closest myLivingPirate to me
     */
    public Pirate myHelper() {
        Pirate closest = game.getMyLivingPirates()[0];
        for (Pirate p : game.getMyLivingPirates()) {
            if (p != this.pirate && p.distance(this.pirate) < closest.distance(this.pirate)) {
                closest = p;
            }
        }
        return closest;
    }

    // Camper Strategy
    public void camperLogic() {
        for (Pirate p : game.getAllEnemyPirates()) {
            if (p.hasCapsule()) {
                this.destination = p;
            }
        }
        // push the holder away from the ship
        if (destination != null && this.pirate.canPush((Pirate) destination)) {
            Location nearestWall = Engine.nearestWall(destination);
            Location[] motherWalls = new Location[game.getEnemyMotherships().length];
            for (int i = 0; i < motherWalls.length; i++)
                motherWalls[i] = Engine.nearestWall(game.getEnemyMotherships()[i]);
            List<Location> motherWallz = Arrays.asList(motherWalls);
            Collections.sort(motherWallz, (l1, l2) -> l1.distance(destination) - l2.distance(destination));
            Location motherWall = motherWallz.get(0);

            // game.debug(game.getEnemyMothership().getLocation()==new Location(5400,1000));
            // if(game.getEnemyMothership().getLocation().row==5400&&game.getEnemyMothership().getLocation().col==1000)
            // {
            // game.debug("the fucking map..");
            // this.tryPushPirate((Pirate) destination,
            // Engine.pushAwayFromShip(destination));
            //
            // }else
            if ((nearestWall.col == motherWall.col || nearestWall.row == motherWall.row)
                    && destination.distance(nearestWall) >= 600) {
                // if the nearest wall is the same wall nearest to enemy mothership and push
                // won't instant kill
                this.tryPushPirate((Pirate) destination, Engine.pushAwayFromShip(destination));
            } else
                this.tryPushPirate((Pirate) destination, nearestWall);
        }
        // Case Enemy don't have any Capsules on him
        // Follow the closest enemy to the capsule
        else if (destination == null) {
            this.destination = Engine.getClosestEnemyCapsuler(this.pirate);
        } else {
            Pirate enemyCapsuler = Engine.getClosestEnemyCapsuler(this.pirate);
            if (enemyCapsuler != null && pirate.distance(enemyCapsuler) > 599) {
                if (enemyCapsuler.distance(Engine.getClosestEnemyMothership(enemyCapsuler)) < pirate
                        .distance(Engine.getClosestEnemyMothership(enemyCapsuler)))
                    // &&Engine.getClosestFriend(pirate).distance(Engine.nearestWall(enemyCapsuler))>600)
                    // wrong :P
                    this.tryPushPirate(Engine.getClosestFriend(pirate), enemyCapsuler);
            }

        }
    }

    // Capsuler Strategy
    public void capsulerLogic() {
        ++CapsulerCounter;
        Mothership myMothership = Engine.getClosestMyMothership(this.pirate);
        boolean campersOnRadius = false;
        int camperDist = 2000;
        Pirate camper = game.getAllEnemyPirates()[0];
        for (Pirate enemy : game.getAllEnemyPirates()) {
            if (enemy.distance(myMothership) < camperDist) {// enemys on/close to my mothership radius.
                camperDist = enemy.distance(myMothership);
                camper = enemy;
                campersOnRadius = true;
            }
        }
        if (didPush) {
            game.debug("Already pushed");
            return;
        }
        if (!this.pirate.hasCapsule()) {
            destination = bots.Engine.getClosestMyCapsule(this.pirate);
            return;
        }
        if (true) {
            destination = Engine.getClosestMyMothership(pirate);
            return;
        }
        game.debug("distance from helper - " + Engine.getClosestFriend(this.pirate).distance(this.pirate));
        if (Engine.getClosestFriend(this.pirate).distance(this.pirate) > 300) {
            destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
            return;
        }
        if (camper.distance(myMothership) < 2000 && campersOnRadius
                && this.pirate.distance(myMothership) != camperDist + 350) { // need to fix if camper is not in line
            // with
            // mine and motherbase.
            // if there are campers, go to position
            destination = myMothership.getLocation().towards(Engine.getClosestMyCapsule(this.pirate).initialLocation,
                    camperDist + 350);
            return;
        }
        if (!campersOnRadius) {
            game.debug("no campers");
            // else if(this.pirate.canPush(myHelper())){
            // sails to the center of the mother ship,
            // TODO: better if it sails until capsule is unloaded (in the 300 range of the
            // mothership..) - DONE
            destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
        }
    }

    // Anti-Camper Strategy
    // TODO: fix logic to work on different type of campers. (campers standing
    // farther then 300 from motherbase..)
    // Added: new Push methods
    public void antiCamperLogic() {
        Mothership myMothership = Engine.getClosestMyMothership(this.pirate);
        boolean campersOnRadius = false;
        int camperDist = 2000;
        Pirate capsuler = getMyCapsuler();
        Pirate camper = game.getAllEnemyPirates()[0];
        for (Pirate enemy : game.getAllEnemyPirates()) {
            if (enemy.distance(myMothership) < camperDist) {// enemys on/close to my mothership radius.
                camperDist = enemy.distance(myMothership);
                camper = enemy;
                campersOnRadius = true;
                game.debug("AntiCamper detected Campers, " + " closest camper distance = " + camperDist);
            }
        }
        if (!campersOnRadius && !pirate.canPush(capsuler)) {
            // goes to capsuler or staying close to the closest pirate (to the capsule)
            // until he becomes capsuler.
            destination = capsuler;
            game.debug("CAPSULER IS DESTINATION");
            return;
        }
        if (camper.distance(myMothership) < 2000 && campersOnRadius
                && this.pirate.distance(myMothership) != camperDist + 301) { // need to fix if camper is not in line
            // with
            // mine and mothership.
            // if there are campers, go to position
            destination = myMothership.getLocation().towards(Engine.getClosestMyCapsule(this.pirate).initialLocation,
                    camperDist + 301);
            game.debug("anti going to position");
            return;
        }
        game.debug("anti reached position");
        capsuler = getMyCapsuler(); // this might cause the problem
        if (capsuler.hasCapsule()) { // if anticamper is in position to push.. // if the pirate has a capsule
            game.debug("CAPSULER HAS CAPSULE");
            for (Pirate enemy : game.getAllEnemyPirates()) {
                if (enemy.distance(capsuler) <= 300 && capsuler.distance(bots.Engine.nearestWall(myMothership)) > 600) {
                    game.debug("pushing capsuler away from creeper");
                    this.tryPushPirate(capsuler, myMothership); // pushes capsuler to the mothership
                    return;
                }
            }
            if (campersOnRadius) { // if there are campers
                game.debug("CAMPERS DETECTED");
                game.debug("CAPSULER IN POSITION?");
                game.debug("capsuler from MS = " + capsuler.distance(myMothership));
                capsuler = getMyCapsuler();
                if (capsuler.distance(myMothership) <= 900) {
                    // only if the push will result in a point
                    game.debug("IM TRYING TO THE PUSH CAPSULER");
                    this.tryPushPirate(capsuler, myMothership); // pushes capsuler to the mothership
                    return;
                }
                if (this.pirate.distance(capsuler) <= 50 && capsuler.distance(myMothership) > 900) { // make sure its
                    // not 49.
                    game.debug("TO VALHALLA");
                    destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
                    // }

                }
                // if the pirate has capsule and there are no campers -
                // this.tryPushPirate(capsuler, myMothership); // pushes capsuler to the
                // motherbase
            }
        }
    }

    public void OneManArmyLogic() {
        if (Engine.getEnemyCapsuler() != null
                && Engine.getEnemyCapsuler().distance(game.getEnemyMotherships()[0]) < 5400) {
            tryPushPirate(Engine.getEnemyCapsuler(), new Location(3300, 1600));
        }
    }

    public void YouShallNotPassLogic()
    {
        List<Asteroid> Asteroids = Arrays.asList(game.getAllAsteroids());
        for(Asteroid a : Asteroids)
        {
            game.debug(a.direction);
            if(pirate.canPush(a) && Engine.isAsteroidMoving(a))
            {
                this.tryPushAstroid(a,a);
            }
        }
        if(!didPush && !pirate.hasCapsule())
        {
            destination = game.getMyCapsules()[0];
            sailToDest();
        }
        else if(!didPush){
            destination = game.getMyMotherships()[0];
            sailToDest();
        }
    }

    // INIT every job
    public void work() {
        switch (this.job) {
            case DEAD:
                game.debug("This pirate is dead");
                break;
            case CAPSULER:
                game.debug("This pirate is a CAPSULER");
                capsulerLogic();
                sailToDest();
                break;
            case CAMPER:
                game.debug("This pirate is a CAMPER");
                camperLogic();
                sailToDest();
                break;
            case ANTICAMPER:
                game.debug("This pirate is an ANTICAMPER");
                antiCamperLogic();
                sailToDest();
                break;
            case OneManArmy: // Specific job for OneManArmy Bot.
                OneManArmyLogic();
                break;
            case COOL:
                YouShallNotPassLogic();
                break;
            default:
                game.debug("No Job Found");
                break;
        }
    }
}