package bots;
//-----------------------------------------------------------
/**
 * @author      RoyRenzik
 * @author      ElayM
 * @version     1.4.6
 *
 **/
//-----------------------------------------------------------

import pirates.*;

import java.util.*;
import java.util.logging.Handler;

/**
 * <pre>
 * Bot for the SkillZ2018 Competition.
 * The Bot was created by Gsociety.
 * </pre>
 */
//-----------------------------------------------------------
// 1.4.5 with FUTHEST CAMPER!
public class MyBot implements PirateBot {


    //Static Variable for the game
    public static PirateGame gameInstance;
    PirateHandler handler;

    @Override
    public void doTurn(PirateGame game){
        MyBot.gameInstance = game;

        handler = new PirateHandler();
        handler.doWork();

    }
}

//Class for all the calculation in the game.
class Engine
{
    //INIT the game Variable
    private static final PirateGame game;
    static {
        game = MyBot.gameInstance;
    }

    /**
     * @return The Closest Capsule from the Pirate
     * Currently don't work because there is only one capsule.
     */
    public static Capsule getClosestCapsule()
    {
        List<Capsule> capsules = Arrays.asList(game.getMyCapsule());
        return capsules.get(0);
    }

    /**
     * @return The Closest My MotherShip from the Pirate
     * Currently don't work because there is only one Mothership.
     */
    public static Mothership getClosestMyMotherShip()
    {
        List<Mothership> motherships = Arrays.asList(game.getMyMothership());
        return motherships.get(0);
    }

    /**
     *
     * @return Enemy Capsuler
     */
    public static Pirate getEnemyCapsuler()
    {
        for(Pirate p : game.getEnemyLivingPirates()) {
            if (p.hasCapsule()) {
                return p;
            }
        }
        return null;
    }

    public static Pirate getClosestFriend(Pirate p)
    {
        List<Pirate> myPirates = Arrays.asList(game.getMyLivingPirates());
        Collections.sort(myPirates, (p1,p2) -> p1.distance(p) - p2.distance(p));
        //return myPirates.get(0); this returns the same pirate?
        return myPirates.get(1);
    }
    public static Pirate getClosestEnemy(Pirate p)
    {
        List<Pirate> myPirats = Arrays.asList(game.getEnemyLivingPirates());
        Collections.sort(myPirats, (p1,p2) -> p1.distance(p) - p2.distance(p));
        return myPirats.get(0);
    }


    /**
     *
     * @param obj
     * @return Which direction the pirate should push the obj to.
     * Currently its Stupid af, and works poorly
     */
    public static Location pushAwayFromShip(MapObject obj)
    {
        Mothership enemyShip = game.getEnemyMothership();
        int x,y;
        y = enemyShip.location.col < obj.getLocation().col ? 9000 : -9000;
        x = enemyShip.location.row < obj.getLocation().row ? 9000 : -9000;
        return new Location(x,y);
    }

    /**
     *
     * @return the location of the nearest wall/border to the pirate.
     * Example - If the method runs on a Pirate at (100,4600) the method will return (0,4600) - a
     * straight line towards the nearest wall.
     */
    public static Location nearestWall(MapObject obj) {
        int row = obj.getLocation().row;
        int col = obj.getLocation().col;
        // TODO - change 0&6400 to out of bounds values -> -1&6401 (?)
        if (row < 3200) {
            if (col < 3200) {
                if (col < row) return new Location(row, 0);
                return new Location(0, col);
            }
            if (row < 6400 - col) return new Location(0, col);
            return new Location(row, 6400);
        }

        if (col < 3200) {
            if (col < 6400 - row) return new Location(row, 0);
            return new Location(6400, col);
        }

        if (6400 - col < 6400 - row) return new Location(row, 6400);
        return new Location(6400, col);
    }
}

//Class that represent a single Pirate
class BotPirate {
    //INIT the game Variable
    private static final PirateGame game;

    static {
        game = MyBot.gameInstance;
    }

    Pirate pirate;
    MapObject destination;
    Job job;
    boolean didPush;

    //Constructor
    public BotPirate(Pirate pirate) {
        this.pirate = pirate;
        this.destination = null;
        this.job = Job.CAPSULER;
        this.didPush = false;
    }

    //Getters & Setters
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

    //Stupid method that pushes anytime any enemy.
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
    //Stupid method that pushes anytime any enemy.
    public void tryPushWall() {
        // Go over all enemies.
        for (Pirate enemy : game.getEnemyLivingPirates()) {
            // Check if the pirate can push the enemy.
            if (pirate.canPush(enemy)) {
                // Push the enemy!
                pirate.push(enemy, Engine.nearestWall(enemy));

                // Print a message.
                System.out.println("pirate " + pirate + " pushes " + enemy + " towards nearest wall");

                // Did push.
                didPush = true;
            }
        }
    }
    /**
     * @param p - AnyPirate
     * @param obj - AnyMapObject
     *
     * This methods will try to push p in the obj location
     */
    public void tryPushPirate(Pirate p, MapObject obj)
    {
        if (pirate.canPush(p)) {
            // Push the enemy!
            pirate.push(p, obj.getLocation());
            // Did push.
            didPush = true;
        }
    }


    //Sail to the Destination
    public void sailToDest() {
        if (!pirate.isAlive()) {
            this.job = Job.DEAD;
            return;
        }
        if (this.didPush) {
            game.debug("Already push");
            return;
        }
        if (this.destination == null) {
            game.debug("No Dest");
            return;
        }
        // should add: if (pirate.getLocation() != destination) ???
        pirate.sail(destination);
    }

    /**
     * Method is written in class BotPirate because it is going to be used in Anti-Camper Strategy
     *
     * @return the location of the nearest wall/border to the pirate.
     * Example - If the method runs on a Pirate at (100,4600) the method will return (0,4600) - a
     * straight line towards the nearest wall.
     */
    public Location nearestWall() {
        int row = this.pirate.location.row;
        int col = this.pirate.location.col;
        // TODO - change 0&6400 to out of bounds values -> -1&6401 (?)
        if (row < 3200) {
            if (col < 3200) {
                if (col < row) return new Location(row, 0);
                return new Location(0, col);
            }
            if (row < 6400 - col) return new Location(0, col);
            return new Location(row, 6400);
        }

        if (col < 3200) {
            if (col < 6400 - row) return new Location(row, 0);
            return new Location(6400, col);
        }

        if (6400 - col < 6400 - row) return new Location(row, 6400);
        return new Location(6400, col);
    }

    /**
     *
     * @return your capsuler or if there isn't a capsuler return closest pirate to the capsule
     */

    public Pirate getMyCapsuler() {
        for (Pirate p : game.getMyLivingPirates()) {
            if (p.hasCapsule()) return p;
        }
        Pirate closest = game.getMyLivingPirates()[0];
        for(Pirate p: game.getMyLivingPirates()){
            if(p.distance(game.getMyCapsule())<closest.distance(game.getMyCapsule()))
                closest=p;
        }
        return closest;
    }

    /**
     *
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


    //Camper Strategy
    public void camperLogic() {
        for (Pirate p : game.getAllEnemyPirates()) {
            if (p.hasCapsule()) {
                this.destination = p;
            }
        }
        //push the holder away from the ship
        if (destination != null && this.pirate.canPush(destination)) {
            Location nearestWall = Engine.nearestWall(destination);
            Location motherWall = Engine.nearestWall(game.getEnemyMothership());
//            game.debug(game.getEnemyMothership().getLocation()==new Location(5400,1000));
//            if(game.getEnemyMothership().getLocation().row==5400&&game.getEnemyMothership().getLocation().col==1000) {
//                game.debug("the fucking map..");
//                this.tryPushPirate((Pirate) destination, Engine.pushAwayFromShip(destination));
//
//            }else
            if ((nearestWall.col == motherWall.col || nearestWall.row == motherWall.row)
                    && destination.distance(nearestWall) >= 600) {
                // if the nearest wall is the same wall nearest to enemy mothership and push won't instant kill
                this.tryPushPirate((Pirate) destination, Engine.pushAwayFromShip(destination));
            } else
                this.tryPushPirate((Pirate) destination, nearestWall);
        }
        //Case Enemy don't have any Capsules on him
        //Follow the closest enemy to the capsule
        else if (destination == null) {
            this.destination = game.getEnemyMothership();
        } else {
            Pirate enemycapsuler = Engine.getEnemyCapsuler();
            if (enemycapsuler != null && pirate.distance(enemycapsuler) > 599) {
                if (enemycapsuler.distance(game.getEnemyMothership()) < pirate.distance(game.getEnemyMothership()))
                    //&&Engine.getClosestFriend(pirate).distance(Engine.nearestWall(enemycapsuler))>600) wrong :P
                    this.tryPushPirate(Engine.getClosestFriend(pirate), enemycapsuler);
            }

        }
    }
    //Capsuler Strategy
    public void capsulerLogic() {
        Mothership myMothership = game.getMyMothership();
        boolean campersOnRadius = false;
        int camperDist = 0;
        Pirate camper = game.getAllEnemyPirates()[0];
        for (Pirate enemy : game.getAllEnemyPirates()) {
            if (enemy.distance(myMothership) < 2000 && enemy.distance(myMothership) > camperDist) {// enemys on/close to my mothership radius.
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
            destination = bots.Engine.getClosestCapsule();
            return;
        }
        game.debug("distance from helper - "+Engine.getClosestFriend(this.pirate).distance(this.pirate));
        if (Engine.getClosestFriend(this.pirate).distance(this.pirate) > 300) {
            destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
            return;
        }
        if (camper.distance(myMothership) < 2000
                && campersOnRadius
                && this.pirate.distance(myMothership) != camperDist + 350) { // need to fix if camper is not in line with
            // mine and motherbase.
            // if there are campers, go to position
            destination = myMothership.getLocation().towards(game.getMyCapsule().initialLocation, camperDist + 350);
            return;
        }
        if (!campersOnRadius) {
            game.debug("no campers");
//        else if(this.pirate.canPush(myHelper())){
            // sails to the center of the mother ship,
            // TODO: better if it sails until capsule is unloaded (in the 300 range of the mothership..) - DONE
            destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
        }
    }


    //Anti-Camper Strategy
    //TODO: fix logic to work on different type of campers. (campers standing farther then 300 from motherbase..)
    //Added: new Push methods
    public void antiCamperLogic() {
        Mothership myMothership = game.getMyMothership();
        boolean campersOnRadius = false;
        int camperDist = 0;
        Pirate capsuler = getMyCapsuler();
        Pirate camper = game.getAllEnemyPirates()[0];
        for (Pirate enemy : game.getAllEnemyPirates()) {
            if (enemy.distance(myMothership) < 2000 && enemy.distance(myMothership) >= camperDist) {// enemys on/close to my mothership radius.
                camperDist = enemy.distance(myMothership);
                camper = enemy;
                campersOnRadius = true;
                game.debug("AntiCamper detected Campers," + " furthest camper distance = " + camperDist);
            }
        }
        if (!campersOnRadius && !pirate.canPush(capsuler)) {
            // goes to capsuler or staying close to the closest pirate (to the capsule) until he becomes capsuler.
            destination = capsuler;
            game.debug("CAPSULER IS DESTINATION");
            return;
        }
        if (camper.distance(myMothership) < 2000
                && campersOnRadius
                && this.pirate.distance(myMothership) != camperDist + 301) { // need to fix if camper is not in line with
            // mine and mothership.
            // if there are campers, go to position
            destination = myMothership.getLocation().towards(game.getMyCapsule().initialLocation, camperDist + 301);
            game.debug("anti going to position");
            return;
        }

        game.debug("anti reached position");
        capsuler = getMyCapsuler(); // this might cause the problem
        if (capsuler.hasCapsule()) { // if anticamper is in position to push.. // if the pirate has a capsule
            game.debug("CAPSULER HAS CAPSULE");
            for (Pirate enemy : game.getAllEnemyPirates()) {
                if (enemy.distance(capsuler) <= 300
                        && capsuler.distance(bots.Engine.nearestWall(myMothership)) > 600) {
                    game.debug("pushing capsuler away from creeper");
                    this.tryPushPirate(capsuler, myMothership); // pushes capsuler to the mothership
                    return;
                }
            }
            if (campersOnRadius) { //if there are campers
                game.debug("CAMPERS DETECTED");
                game.debug("CAPSULER IN POSITION?");
                game.debug("capsuler from MS = " + capsuler.distance(myMothership));
                capsuler = getMyCapsuler();
                if (capsuler.distance(game.getMyMothership()) <= 900) {
                    // only if the push will result in a point
                    game.debug("IM TRYING TO THE PUSH CAPSULER");
                    this.tryPushPirate(capsuler, game.getMyMothership()); // pushes capsuler to the mothership
                    return;
                }
                if (this.pirate.distance(capsuler) <=50 && capsuler.distance(myMothership) > 900) { // make sure its not 49.
                    game.debug("TO VALHALLA");
                    tryPushWall();
                    destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
                }
                // if the pirate has capsule and there are no campers -
                //this.tryPushPirate(capsuler, myMothership); // pushes capsuler to the motherbase
            }
        }
    }


    //INIT every job
    public void work()
    {
        switch(this.job)
        {
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
            default:
                game.debug("No Job Found");
                break;
        }
    }
}

//Class that represent the Strategy
class PirateHandler
{
    private static final PirateGame game;
    static {
        game = MyBot.gameInstance;
    }
    List<BotPirate> pirates;
    Tactic tactic;

    public PirateHandler()
    {
        pirates = new ArrayList<BotPirate>();
        for(Pirate p : game.getAllMyPirates())
        {
            pirates.add(new BotPirate(p));
        }
        tactic = Tactic.STANDARD;
    }

    /**
     *
     * @param obj
     * Sorting the Pirate Array by distance from object
     */
    public void sortingByDistance(MapObject obj)
    {
        Collections.sort(pirates, (p1, p2) -> p1.pirate.distance(obj) - p2.pirate.distance(obj));
    }

    /**
     * Note: Can't be used in BotPirate class (roy, where is it being used?)
     * @return if any of my pirates has the capsule
     */
    public boolean capsuleOwn()
    {
        for(BotPirate p : pirates)
        {
            if(p.pirate.hasCapsule()) return true;
        }
        return false;
    }

    //Tactic Maker
    //Represent as Job like: Capsuler - Camper - AntiCamper and etc'
    // 4 - 4 means that there is 4 Capulsers and 4 Campers
    public void doWork()
    {
        int i = 0;
        switch (tactic)
        {
            case STANDARD:
                //(Engine.getClosestCapsule());
                int count = 0;
                for(BotPirate p : pirates)
                {
                    if(!p.pirate.isAlive())
                    {
                        //  --count;
                        p.setJob(Job.DEAD);
                    }
                    else if(count<1)
                    {
                        p.setJob(Job.CAPSULER);
                    }
                    else if(count<2){
                        p.setJob(Job.ANTICAMPER);
                    }
                    else
                    {
                        p.setJob(Job.CAMPER);;
                    }
                    p.work();
                    ++count;
                }
                break;
            case BASICV4: // BUG capsulers are campers
                sortingByDistance(Engine.getClosestCapsule());
                i = 0;
                for(BotPirate p : pirates)
                {
                    if(!p.pirate.isAlive())
                    {
                        p.setJob(Job.DEAD);
                    }
                    else if(i==0)
                    {
                        p.setJob(Job.CAPSULER);
                    }
                    if(i >0 && i < 2)
                    {
                        p.setJob(Job.ANTICAMPER);
                    }
                    else
                    {
                        p.setJob(Job.CAMPER);;
                    }
                    p.work();
                    ++i;
                }
                break;
            case BASICV3: // BUG capsulers are campers
                sortingByDistance(Engine.getClosestCapsule());
                i = 0;
                for (BotPirate p : pirates) {
                    if (!p.pirate.isAlive()) {
                        p.setJob(Job.DEAD);
                    } else if (i == 0) {
                        p.setJob(Job.CAPSULER);
                    }
                    if (i > 0 && i < 3) {
                        p.setJob(Job.ANTICAMPER);
                    } else {
                        p.setJob(Job.CAMPER);

                    }
                    p.work();
                    ++i;
                }
                break;
            //3 - 5
            case BASICV2:
                sortingByDistance(Engine.getClosestCapsule());
                int j = 0;
                for (BotPirate p : pirates) {
                    if (!p.pirate.isAlive()) {
                        p.setJob(Job.DEAD);
                    } else if (j < 3) {
                        p.setJob(Job.CAPSULER);
                    } else {
                        p.setJob(Job.CAMPER);

                    }
                    p.work();
                    ++j;
                }
                break;
            //4-4
            case BASIC:
                i = 0;
                int more = pirates.size() / 2;
                int allready = 0;
                for (BotPirate p : pirates) {
                    if (!p.pirate.isAlive()) {
                        p.setJob(Job.DEAD);
                    }
                    if (i % 2 == 0 && allready < more) {
                        p.setJob(Job.CAMPER);
                        allready++;
                    } else {
                        p.setJob(Job.CAPSULER);
                    }
                    p.work();
                    i++;
                }
                break;
            //8-0
            default:
                for(BotPirate p : pirates)
                {
                    p.work();
                }
                break;
        }


    }
}

//Job for Pirates
enum Job
{
    DEAD,CAPSULER,CAMPER,ANTICAMPER,SAVER;
}


//Tactic
enum Tactic
{
    BASIC, BASICV2, BASICV3, BASICV4, STANDARD;
}