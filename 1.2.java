package bots;
//-----------------------------------------------------------
/**
 * @author      RoyRenzik
 * @author      ElayM
 * @version     1.2
 *
 **/
//-----------------------------------------------------------

import pirates.*;

import java.util.*;

/**
 * <pre>
 * Bot for the SkillZ2018 Competition.
 * The Bot was created by StakeOverFlow.
 * </pre>
 */
//-----------------------------------------------------------

//Added this time: new Tactic, Saver Job
public class MyBot implements PirateBot {


    //Static Variable for the game
    public static PirateGame gameInstance;

    @Override
    public void doTurn(PirateGame game){
        MyBot.gameInstance = game;

        PirateHandler handler = new PirateHandler();
        handler.doWork();

    }
}

//Class for all the calculation in the game.
class Engine
{
    //INIT the game Varibale
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

    //Camper Strategy
    public void camperLogic() {
        for (Pirate p : game.getAllEnemyPirates()) {

            if (p.hasCapsule()) {
                this.destination = p;
            }

        }
        //push the holder away from the ship
        if (destination != null) {
            if (this.pirate.canPush(destination)) {
                this.pirate.push((Pirate) destination, Engine.pushAwayFromShip(destination));
                this.didPush = true;
            }
        }
        //Case Enemy don't have any Capsules on him
        //Follow the closest enemy to the capsule
        if (destination == null) {
            this.destination = game.getEnemyMothership();
        }
    }

    //Capsuler Strategy
    public void capsulerLogic() {
        tryPush();
        if (didPush) {
            game.debug("Already pushed");
            return;
        }
        if (this.pirate.capsule == null) {
            destination = Engine.getClosestCapsule();
        } else {
            // sails to the center of the mother ship,
            // TODO: better if it sails until capsule is unloaded (in the 300 range of the mothership..)
            destination = Engine.getClosestMyMotherShip();
        }
    }

    //Anti-Camper Strategy
    //TODO: fix logic to work on different type of campers. (campers standing more far then 300 from motherbase..)
    public void antiCamperLogic() {
        Mothership myMothership = game.getMyMothership();
        boolean campersOnRadius = false;
        for (Pirate enemy : game.getAllEnemyPirates())
            if (enemy.distance(myMothership) == 300) // enemys on my mothership radius.
                campersOnRadius = true;
        if (this.pirate.distance(myMothership) != 601 && campersOnRadius)
            destination = game.getMyCapsule().initialLocation.towards(myMothership.getLocation(), 601);
        else {
            for (Pirate p : game.getMyLivingPirates()) {
                if (p.hasCapsule()) {
                    if (pirate.canPush(p) && p.distance(myMothership) <= 900) {
                        pirate.push(p, game.getMyMothership()); // pushes capsuler to the motherbase
                    }
                }
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
                return;
            case CAPSULER:
                capsulerLogic();
                sailToDest();
                break;
            case CAMPER:
                camperLogic();
                sailToDest();
                break;
            case ANTICAMPER:
                antiCamperLogic();
                sailToDest();
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
    Tactics tatic;

    public PirateHandler()
    {
        pirates = new ArrayList<BotPirate>();
        for(Pirate p : game.getAllMyPirates())
        {
            pirates.add(new BotPirate(p));
        }
        tatic = Tactics.BASICV2;
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
    //Represent as Job like: Capsuler - Camper and etc'
    // 4 - 4 means that there is 4 Capulsers and 4 Campers
    public void doWork()
    {
        switch (tatic)
        {
            //3 - 5
            case BASICV2:
                sortingByDistance(Engine.getClosestCapsule());
                int j = 0;
                for(BotPirate p : pirates)
                {
                    if(!p.pirate.isAlive())
                    {
                        p.setJob(Job.DEAD);
                    }
                    else if(j<3)
                    {
                        p.setJob(Job.CAPSULER);
                    }
                    else
                    {
                        p.setJob(Job.CAMPER);;
                    }
                    p.work();
                    ++j;
                }
                break;
            //4-4
            case BASIC:
                int i = 0;
                int more = pirates.size() / 2;
                int allready = 0;
                for(BotPirate p : pirates)
                {
                    if(!p.pirate.isAlive())
                    {
                        p.setJob(Job.DEAD);
                    }
                    if(i%2 == 0 && allready < more)
                    {
                        p.setJob(Job.CAMPER);
                        allready++;
                    }
                    else
                    {
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


//Tactics
enum Tactics
{
    BASIC, BASICV2;
}
