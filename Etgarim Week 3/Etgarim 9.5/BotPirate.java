package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;

import pirates.*;

//Class that represent a single Pirate
public class BotPirate {
    // INIT the game Variable
    private static final PirateGame game;
    public static HashSet<Wormhole> assignedHoles;
    public static HashSet<Capsule> unAvailableCapsules;

    private static int capsulerCounter = -1;
    private static int camperCounter = -1;
    private static int astroCounter = -1;
    private static boolean moveAsteroid = false;
    private static int trySomething = 0;

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

    public BotPirate(BotPirate pirate) {
        this.pirate = pirate.pirate;
        this.destination = pirate.destination;
        this.job = pirate.job;
        this.didPush = pirate.didPush;
    }
    
    public static void startVariables(){
        assignedHoles = new HashSet<>();
        unAvailableCapsules = new HashSet<>();
        capsulerCounter = -1;
        camperCounter = -1;
        astroCounter = -1;
        moveAsteroid = false;
    }

    public String toString() {
        return "ID: ";
    }

    public void setJobNDest(BotPirate bp) {
        this.destination = bp.destination;
        this.job = bp.job;
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

    /**
     * @param p   - AnyPirate
     * @param obj - AnyMapObject
     *            <p>
     *            This methods will try to push p in the obj location
     */
    public void tryPushPirate(Pirate p, MapObject obj) {
        if (this.pirate.canPush(p)) {
            // Push the enemy!
            this.pirate.push(p, obj.getLocation());
            // Did push.
            this.didPush = true;
        }
    }

    public void tryPushAsteroid(Asteroid a, MapObject obj) {
        if (pirate.canPush(a) && a.turnsToRevive == 0) {
            // Push the enemy!
            //game.debug("pushAsteroidTo " + obj.getLocation());
            pirate.push(a, obj.getLocation());
            // Did push.
            didPush = true;

            if (job.equals(Job.CAMPER))
                moveAsteroid = true;
        }
    }

    public void tryPushWormhole(Wormhole w, MapObject obj) {
        if (pirate.canPush(w)) {
            // Push the Wormhole!
            //game.debug("Pushing Wormhole to " + obj.getLocation());
            pirate.push(w, obj.getLocation());
            // Did push.ca
            didPush = true;
            if (job.equals(Job.CAMPER)) assignedHoles.add(w);
        }
    }

    // Sail to the Destination
    public void sailToDest() {
        if (!pirate.isAlive()) {
            this.job = Job.DEAD;
            return;
        }
        if (this.didPush) {
            //game.debug("Already push");
            return;
        }
        if (game.getLivingAsteroids().length > 0) {
//			if (Engine.areThereCampers()) {
//				tryPushAsteroid(game.getLivingAsteroids()[0], game.getMyMotherships()[0]);
//			}
//			tryPushAsteroid(game.getLivingAsteroids()[0], game.getEnemyCapsules()[0]);
            tryPushAsteroid(Engine.getClosestAsteroid(this.pirate), Engine.pushAsteroidTo(Engine.getClosestAsteroid(this.pirate)));
            if (didPush) {
                return;
            }
        }
        if (this.destination == null) {
            //game.debug("No Dest");
            return;
        }
        if (this.destination.getLocation().row < 0 || this.destination.getLocation().row > game.rows
                || this.destination.getLocation().col < 0 || this.destination.getLocation().col > game.cols) {
            //game.debug("This dest Invalid");
            return;
        }
        // should add: if (pirate.getLocation() != destination) ???
        //pirate.sail(destination);
        PirateNavigator.navigate(this);

    }

    /**
     * ~ DO NOT USE ~
     * Method is written in class BotPirate because it is going to be used in
     * Anti-Camper Strategy
     *
     * @return the location of the nearest wall/border to the pirate. Example - If
     * the method runs on a Pirate at (100,4600) the method will return
     * (0,4600) - a straight line towards the nearest wall.
     */
    public Location nearestWall() {
        int maxRows = game.rows;
        int maxCols = game.cols;

        int row = this.pirate.location.row;
        int col = this.pirate.location.col;

        if (row < (maxRows / 2.0)) {
            if (col < (maxCols / 2.0)) {
                if (col < row)
                    return new Location(row, -1);
                return new Location(-1, col);
            }
            if (row < (maxCols - col))
                return new Location(-1, col);
            return new Location(row, maxCols + 1);
        }
        if (col < (maxCols / 2.0)) {
            if (col < (maxRows - row))
                return new Location(row, -1);
            return new Location(maxRows + 1, col);
        }

        if ((maxCols - col) < (maxRows - row))
            return new Location(row, maxCols + 1);
        return new Location(maxRows + 1, col);
    }

    /**
     * @return the first capsuler he finds or if there isn't a capsuler return
     * closest pirate to a capsule
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

    public void camperLogicV2() {
        camperCounter++;
        Pirate enemyCapsuler = Engine.getClosestEnemyCapsuler(this.pirate);
        int numOfEnemyShips = game.getEnemyMotherships().length;
        Mothership enemyMothership = game.getEnemyMotherships()[0];
        //Wormhole w = Engine.getClosestEnemyWormhole(this.pirate);
        Mothership enemyMom = Engine.getClosestEnemyMothership(this.pirate);
        Wormhole w = Engine.getClosestWormhole(enemyMom);
        int firstLayer = 0;
        if (game.getAllWormholes().length > 0 && enemyMom != null && w != null && ((enemyMom.distance(w)) - game.mothershipUnloadRange) <= 600)
            destination = w.getLocation().towards(this.pirate, game.wormholeRange + 50);
        if (numOfEnemyShips < 2 && numOfEnemyShips > 0) {
            if(enemyCapsuler.stateName.equals("heavy"))
            {
                firstLayer = 1;
            }
            else{
                firstLayer = 2;
            }
            if (camperCounter < firstLayer) {
                if (enemyCapsuler == null)
                    destination = enemyMothership;
                else
                    destination = enemyMothership.getLocation().towards(enemyCapsuler, 301);
            } else {
                if (enemyCapsuler == null)
                    destination = enemyMothership.getLocation().towards(game.getEnemyCapsules()[0], 800);
                else
                    destination = enemyMothership.getLocation().towards(enemyCapsuler, 1200);
            }
            if (enemyCapsuler != null) {
                if (enemyCapsuler.distance(this.pirate) < 600 && this.pirate.pushReloadTurns < 1 && enemyCapsuler.hasCapsule()) {
                    destination = enemyCapsuler;
                }
            }
            Pirate target = Engine.getClosestEnemyCapsuler(this.pirate);
            Asteroid asteroid = Engine.getClosestLivingAsteroid(target);
            try {
                if (target.distance(asteroid) - asteroid.size <= 600)
                    tryPushPirate(target, asteroid);
                else
                    tryPushPirate(target, Engine.nearestWall(target));
            } catch (NullPointerException e) {
                tryPushPirate(target, Engine.nearestWall(target));
            }

        } else {
            List<Mothership> enemyMotherShips = Arrays.asList(game.getEnemyMotherships());
            List<Capsule> enemyCapsules = Arrays.asList(game.getEnemyCapsules());
            List<Mothership> best = Engine.getBestEnemyBase(enemyMotherShips, enemyCapsules);

            if (camperCounter < 2) {
                if (enemyCapsuler == null) {
                    destination = best.get(0);
                } else {
                    destination = best.get(0).getLocation().towards(enemyCapsuler, 300);
                }
            } else if (camperCounter < 5) {
                if (enemyCapsuler == null) {
                    destination = best.get(0).getLocation().towards(game.getEnemyCapsules()[0], 1000);
                } else {
                    destination = best.get(0).getLocation().towards(enemyCapsuler, 1200);
                }

            } else {
                if (enemyCapsuler == null) {
                    destination = best.get(1).getLocation().towards(game.getEnemyCapsules()[0], 1000);
                } else {
                    destination = best.get(1).getLocation().towards(enemyCapsuler, 1200);
                }
            }
            if (enemyCapsuler != null) {
                if (enemyCapsuler.distance(this.pirate) < 601) {
                    destination = enemyCapsuler;
                }
            }
            Pirate target = Engine.getClosestEnemyCapsuler(this.pirate);
            Asteroid asteroid = Engine.getClosestLivingAsteroid(target);
            try {
                if (target.distance(asteroid) - asteroid.size < 600)
                    tryPushPirate(target, asteroid);
                else
                    tryPushPirate(target, Engine.nearestWall(target));
            } catch (NullPointerException eNull) {
                tryPushPirate(target, Engine.nearestWall(target));
            }

        }

//        if (game.getAllAsteroids().length > 0) {
//            Asteroid a = Engine.getClosestLivingAsteroid(this.pirate);
//            if (a != null && this.pirate.canPush(a))
//                tryPushAsteroid(a, Engine.pushAsteroidTo(a));
//            Asteroid[] threats = Engine.willAsteroidHit(this.pirate);
//            if (threats.length > 0)
//                for (Asteroid asteroid : threats)
//                    tryPushAsteroid(asteroid, Engine.pushAsteroidTo(asteroid));
//        }
        for (Asteroid astro : game.getLivingAsteroids()) {
            if (!moveAsteroid) {
                //tryPushAsteroid(astro, game.getMyMotherships()[0]);
                tryPushAsteroid(astro, Engine.pushAsteroidTo(astro));
            }
        }

        if (!didPush && game.getAllWormholes().length > 0 && this.pirate.distance(w) < 800) {
//            Wormhole w = Engine.getClosestEnemyWormhole(this.pirate);
//            Mothership enemyMom = Engine.getClosestEnemyMothership(this.pirate);
            //game.debug("Wormhole distance from enemy mothership = " + w.distance(enemyMom));
//            if (enemyMom != null && w != null
////                    && this.pirate.distance(enemyMom) > 700
////                    && (((enemyMom.distance(w)) - game.wormholeRange) - game.mothershipUnloadRange) <= 600) {
//                    && enemyMom.distance(w) <= 1200) {
//                destination = w.getLocation().towards(this.pirate, game.wormholeRange + 1);
            //game.debug("dest = " + destination);
            Mothership partnerMothership = Engine.getClosestAnyMothership(w.partner);
            //game.debug("Partnermothership id = " + partnerMothership.owner.id + " My id = " + game.getMyself().id);
            if (w.isActive && Engine.getClosestEnemy(w).distance(w) - game.wormholeRange <= 600)
                tryPushWormhole(w, Engine.getClosestEnemy(w));
            else if (partnerMothership != null && partnerMothership.owner.id == game.getMyself().id)
                tryPushWormhole(w, Engine.getClosestMyCapsuler(this.pirate));
            else if ((w.partner.distance(w) - (game.wormholeRange * 2)) <= 600)
                tryPushWormhole(w, w.partner);
            else
                tryPushWormhole(w, Engine.getClosestMyMothership(w));
        }
        try
        {
        for(Wormhole worm : game.getAllWormholes())
        {
            if(worm.distance(game.getEnemyMotherships()[0]) < 800 && pirate.distance(enemyCapsuler) > 1000 && pirate.pushReloadTurns<2)
            {
                destination = worm.getLocation().add(new Location(worm.wormholeRange,worm.wormholeRange));
            }
            if(game.getEnemyMotherships().length > 1)
            {
                if(worm.distance(game.getEnemyMotherships()[1]) < 800 && pirate.distance(enemyCapsuler) > 1000 && pirate.pushReloadTurns<2)
                {
                    destination = worm.getLocation().add(new Location(worm.wormholeRange,worm.wormholeRange));
                }
            }
        }
            
        }catch(Exception e)
        {
            game.debug("Bug");
        }
//    }
        /*
         * { if(numOfEnemyShips == 0) { this.capsulerLogic(); return; } Mothership
         * enemyBestShip = Engine.getClosestEnemyMothershipToCapsule(); if(camperCounter
         * < 3) { if (enemyCapsuler == null) { destination = enemyBestShip; } else {
         * destination =enemyBestShip.getLocation().towards(enemyCapsuler, 301); } }
         * else { List<Mothership> enemyShips =
         * Arrays.asList(game.getEnemyMotherships()); for(int x = 0; x <
         * enemyShips.size(); x++) { if(camperCounter < 4) { if (enemyCapsuler == null)
         * { destination = enemyBestShip; } else { destination
         * =enemyShips.get(x).getLocation().towards(enemyCapsuler, 301); } } } } }
         */

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

            // //game.debug(game.getEnemyMothership().getLocation()==new Location(5400,1000));
            // if(game.getEnemyMothership().getLocation().row==5400&&game.getEnemyMothership().getLocation().col==1000)
            // {
            // //game.debug("the fucking map..");
            // this.tryPushPirate((Pirate) destination,
            // Engine.pushAwayFromShip(destination));
            //
            // }else
            Asteroid a = Engine.getClosestAsteroid(destination);
            if (a != null) {
                if (a.distance(destination) < 601) {
                    this.tryPushPirate((Pirate) destination, a);
                }
            }
            if ((nearestWall.col == motherWall.col || nearestWall.row == motherWall.row)
                    && destination.distance(nearestWall) >= 600) {
                // if the nearest wall is the same wall nearest to enemy mothership and push
                // won't instant kill
                this.tryPushPirate((Pirate) destination, Engine.pushAwayFromShip(destination));
            } else
                this.tryPushPirate((Pirate) destination, nearestWall);
        }
        if (didPush) {
            return;
        }
        // Case Enemy don't have any Capsules on him
        // Follow the closest enemy to the capsule
        if (destination == null) {
            this.destination = Engine.getClosestEnemyMothership(this.pirate);
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
        ++capsulerCounter;
        System.out.println(this.pirate.id);
        //game.debug(capsulerCounter);

//        boolean campersOnRadius = false;
//        int camperDist = 2000;
//        Pirate camper = game.getAllEnemyPirates()[0];


//        if (game.getAllAsteroids().length > 0) {
//            Asteroid a = Engine.getClosestLivingAsteroid(this.pirate);
//            if (a.isAlive() && this.pirate.canPush(a))
//                tryPushAsteroid(a, Engine.pushAsteroidTo(a));
//            Asteroid[] threats = Engine.willAsteroidHit(this.pirate);
//            if (threats.length > 0)
//                for (Asteroid asteroid : threats)
//                    tryPushAsteroid(asteroid, Engine.pushAsteroidTo(asteroid));
//        }

        //OutOfSpace Logic
    
        if(this.pirate.stateName.equals("heavy") && !this.pirate.hasCapsule() && game.turn > 10)
        {
            List<Pirate> normals = Engine.getNormalPirates();
            if(normals.size() > 0)
            {
                if(game.getEnemyMotherships().length > 0)
                {
                    Collections.sort(normals, (e1, e2) -> e1.distance(game.getEnemyMotherships()[0]) - e2.distance(game.getEnemyMotherships()[0]));
                }
                if(!PirateHandler.getSwap())
                {
                    this.pirate.swapStates(normals.get(0));
                    PirateHandler.setSwap(true);
                    return; 
                }
            }
        }
        
        if(this.pirate.stateName.equals("normal") && this.pirate.hasCapsule())
        {
            if(game.getMyMotherships().length > 0)
            {
               
                if(pirate.distance(game.getMyMotherships()[0]) < 1500)
                {
                    List<Pirate> normals = Engine.getHeavyPirates();
                    if(normals.size() > 0)
                    {
                        if(game.getEnemyMotherships().length > 0)
                        {
                            Collections.sort(normals, (e1, e2) -> e1.distance(game.getEnemyMotherships()[0]) - e2.distance(game.getEnemyMotherships()[0]));
                        }
                        if(!PirateHandler.getSwap())
                        {
                            if(!normals.get(0).hasCapsule())
                            {
                                boolean isClose = false;
                                for(Pirate p : game.getEnemyLivingPirates())
                                {
                                    if(p.distance(pirate) < 302)
                                    {
                                        isClose = true;
                                    }
                                }
                                if(isClose)
                                {
                                    this.pirate.swapStates(normals.get(0));
                                    PirateHandler.setSwap(true);
                                    return; 
                                }
                                
                            }
                        }
                    }
                }
            }
            
        }
        
        if(game.getEnemy().botName.equals("25236"))
        {
            if(!pirate.hasCapsule())
            {
                destination = game.getMyCapsules()[0];
            }
            else if(pirate.getLocation().col > game.getMyMotherships()[0].getLocation().col)
            {
                destination = new Location(0,game.getMyMotherships()[0].getLocation().col);
            }
            else{
                destination = game.getMyMotherships()[0];
            }
            return;
        }
        
        for (Asteroid astro : game.getLivingAsteroids()) {
            tryPushAsteroid(astro, Engine.pushAsteroidTo(astro));
        }

//        for (Pirate enemy : game.getAllEnemyPirates()) {
//            if (enemy.distance(myMothership) < camperDist) {// enemys on/close to my mothership radius.
//                camperDist = enemy.distance(myMothership);
//                camper = enemy;
//                campersOnRadius = true;
//            }
//        }
        if (didPush) {
            //game.debug("Already pushed");
            return;
        }

        if (!this.pirate.hasCapsule()) {
            destination = Engine.getClosestAvailableMyCapsule(this.pirate, BotPirate.unAvailableCapsules);
            unAvailableCapsules.add((Capsule)destination);
            return;
        }

        Pirate fellowCapsuler = Engine.getClosestMyCapsuler(this.pirate);

        if (fellowCapsuler != null) {
            if (fellowCapsuler.hasCapsule() && Engine.willPushScore(this.pirate, fellowCapsuler)) {
                this.tryPushPirate(fellowCapsuler, Engine.getClosestMyMothership(fellowCapsuler));
            }
        }
        destination = Engine.getClosestMyMothership(this.pirate);
        Wormhole w = Engine.getClosestWormhole(this.pirate);
        if (w != null && game.getEnemyMotherships().length > 0) {
            if (!didPush && w.distance(Engine.getClosestMyMothership(w)) < w.partner.distance(Engine.getClosestMyMothership(w.partner))) {
                tryPushWormhole(w, Engine.getClosestMyMothership(w));
            }
        } else if (w != null && !didPush) {
            tryPushWormhole(w, Engine.getClosestMyCapsule(this.pirate));
        }
        
        

        /*
            //game.debug("distance from helper - " + Engine.getClosestFriend(this.pirate).distance(this.pirate));
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
                //game.debug("no campers");
                // else if(this.pirate.canPush(myHelper())){
                // sails to the center of the mother ship,
                // TODO: better if it sails until capsule is unloaded (in the 300 range of the
                // mothership..) - DONE
                destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
            }
        */
    }

    // Anti-Camper Strategy
    // TODO: fix logic to work on different type of campers. (campers standing
    // farther then 300 from motherbase..)
    // Added: new Push methods
    public void antiCamperLogic() {
        // if (game.getAllAsteroids().length > 0) {
        // astroLogic();
        // return;
        // }
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
                //game.debug("AntiCamper detected Campers, " + " closest camper distance = " + camperDist);
            }
        }
        if (!campersOnRadius && !pirate.canPush(capsuler)) {
            // goes to capsuler or staying close to the closest pirate (to the capsule)
            // until he becomes capsuler.
            destination = capsuler;
            //game.debug("CAPSULER IS DESTINATION");
            return;
        }
        if (camper.distance(myMothership) < 2000 && campersOnRadius
                && this.pirate.distance(myMothership) != camperDist + 301) { // need to fix if camper is not in line
            // with
            // mine and mothership.
            // if there are campers, go to position
            destination = myMothership.getLocation().towards(Engine.getClosestMyCapsule(this.pirate).initialLocation,
                    camperDist + 301);
            //game.debug("anti going to position");
            return;
        }
        //game.debug("anti reached position");
        capsuler = getMyCapsuler(); // this might cause the problem
        if (capsuler.hasCapsule()) { // if anticamper is in position to push.. // if the pirate has a capsule
            //game.debug("CAPSULER HAS CAPSULE");
            for (Pirate enemy : game.getAllEnemyPirates()) {
                if (enemy.distance(capsuler) <= 300 && capsuler.distance(bots.Engine.nearestWall(myMothership)) > 600) {
                    //game.debug("pushing capsuler away from creeper");
                    this.tryPushPirate(capsuler, myMothership); // pushes capsuler to the mothership
                    return;
                }
            }
            if (campersOnRadius) { // if there are campers
                //game.debug("CAMPERS DETECTED");
                //game.debug("CAPSULER IN POSITION?");
                //game.debug("capsuler from MS = " + capsuler.distance(myMothership));
                capsuler = getMyCapsuler();
                if (capsuler.distance(myMothership) <= 900) {
                    // only if the push will result in a point
                    //game.debug("IM TRYING TO THE PUSH CAPSULER");
                    this.tryPushPirate(capsuler, myMothership); // pushes capsuler to the mothership
                    return;
                }
                if (this.pirate.distance(capsuler) <= 50 && capsuler.distance(myMothership) > 900) { // make sure its
                    // not 49.
                    //game.debug("TO VALHALLA");
                    destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
                    // }

                }
                // if the pirate has capsule and there are no campers -
                // this.tryPushPirate(capsuler, myMothership); // pushes capsuler to the
                // motherbase
            }
        }
    }

    public void astroLogic() {
        boolean found = false;
        for (Asteroid a : game.getAllAsteroids()) {
            if (a != null)
                found = true;
        }
//        if (!found) {
//            camperLogicV2();
//            return;
//        }
        if (game.getLivingAsteroids().length == 0 || (game.getLivingAsteroids().length == 1 && Engine.isAsteroidMoving(game.getLivingAsteroids()[0]))) {
            Wormhole w = Engine.getClosestWormhole(Engine.getClosestMyMothership(this.pirate));
            destination = w;
            if (destination != null && w.distance(Engine.getClosestMyMothership(w)) > 600)
                this.tryPushWormhole(w, Engine.getClosestMyMothership(w));
            return;
        }
        Asteroid asteroid = Engine.getClosestLivingAsteroid(this.pirate);
        Pirate[] enemys = game.getEnemyLivingPirates();
        ArrayList<Pirate> targets = new ArrayList<>();
        for (int i = 0; i < enemys.length; i++) {
            if (asteroid != null && Engine.getClosestEnemy(enemys[i]).distance(enemys[i]) <= asteroid.size)
                //  && Engine.getClosestEnemyMothership(enemys[i]).distance(enemys[i]) <= 3000)
                /*
                 * Added this provides the same result for now. check it aswell in the future.
                 * && Engine.getClosestFriend(enemys[i]).distance(enemys[i]) > asteroid.size)
                 */
                targets.add(enemys[i]);
        }
        if (targets.size() > 1)
            Collections.sort(targets, (t1, t2) -> t1.distance(this.pirate) - t2.distance(this.pirate));
        if (asteroid != null)
            this.destination = asteroid.getLocation().towards(this.pirate.getLocation(), asteroid.size + 1);
        else
            this.destination = game.getAllAsteroids()[0].initialLocation.towards(this.pirate.getLocation(),
                    game.getAllAsteroids()[0].size + 1);
        if (destination != null) {
            try {
                //THESE ARE THE OPTIONS FROM WHAT I SENT: (THIS IS A BUG)
                // this.tryPushAsteroid(asteroid, targets.get(0));
//                this.tryPushAsteroid(asteroid, Engine.midPoint(targets));
                this.tryPushAsteroid(asteroid, Engine.pushAsteroidTo(asteroid));
            } catch (Exception e) {
                for (Mothership x : game.getMyMotherships()) {
                    try {
                        if (Engine.areThereCampers(x))
                            this.tryPushAsteroid(asteroid, Engine.getClosestEnemy(x));
                    } catch (Exception e2) {
                    }
                }
            }

        }
    }

    public void OneManArmyLogic() {
        if (Engine.getEnemyCapsuler() != null
                && Engine.getEnemyCapsuler().distance(game.getEnemyMotherships()[0]) < 5400) {
            tryPushPirate(Engine.getEnemyCapsuler(), new Location(3300, 1600));
        }
    }

    public void YouShallNotPassLogic() {
        List<Asteroid> Asteroids = Arrays.asList(game.getLivingAsteroids());
        for (Asteroid a : Asteroids) {
            //game.debug(a.direction);
            if (pirate.canPush(a) && Engine.isAsteroidMoving(a)) {
                this.tryPushAsteroid(a, a);
            }
        }
        if (!didPush && !pirate.hasCapsule()) {
            destination = game.getMyCapsules()[0];
            pirate.sail(destination);
        } else if (!didPush) {
            destination = game.getMyMotherships()[0];
            pirate.sail(destination);
        }
    }
    
    public void SpaghtLogic()
    {
        game.debug("SpaghtLogic");
        if(this.pirate.hasCapsule() && this.pirate.distance(game.getMyCapsules()[0].initialLocation) < 1000)
        {
            this.destination = game.getAllWormholes()[0];
        }
        else{
            this.capsulerLogic();
        }
    }

    // INIT every job
    public void work() {
        switch (this.job) {
            case DEAD:
                //game.debug("Pirate #" + this.pirate.id + " is DEAD");
                break;
            case CAPSULER:
                //game.debug("Pirate #" + this.pirate.id + " is a CAPSULER");
                capsulerLogic();
                sailToDest();
                break;
            case CAMPER:
                //game.debug("Pirate #" + this.pirate.id + " is a CAMPER");
                camperLogicV2();
                sailToDest();
                break;
            case ANTICAMPER:
                //game.debug("Pirate #" + this.pirate.id + " is an ANTICAMPER");
                antiCamperLogic();
                sailToDest();
                break;
            case OneManArmy: // Specific job for OneManArmy Bot.
                OneManArmyLogic();
                break;
            case COOL:
                YouShallNotPassLogic();
                break;
            case ASTRO:
                //game.debug("Pirate #" + this.pirate.id + " is an ASTRO");

                astroLogic();
                sailToDest();
                break;
            case SPAGHT:
                SpaghtLogic();
                sailToDest();
            default:
                //game.debug("No Job Found");
                break;
        }
    }
}