package bots;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import pirates.*;
//-----------------------------------------------------------
/**
 * @author      RoyRenzik
 * @author      ElayM
 * @version     1.1.3
 *
 **/
//-----------------------------------------------------------
/**
 * <pre>
 * Bot for the SkillZ2018 Competition.
 * The Bot created by StakeOverFlow.
 * </pre>
 */
//-----------------------------------------------------------
/**
 * Comments:
 * Elay you r a DICK
 * Add here things you think we should add to the bot
 */


//-----------------------------------------------------------
/**Things to FIX/TODO by Priority:
 *Camper Job  - FIXED
 *IMPORTENT! Check for Campers - THE MOST IMPORTANT SHIT IN THIS GAME
 *Priority to double push
 *Push away from city can push the Pirate into the Capsule
 *Saver - pirate that follow the capsule holder and save him from other pirates
 *Check if we should push our pirate to give him a boost
 *Implemets A* or dijkStar
 */
//-----------------------------------------------------------

/**
 * Vers:
 * 1.0 - https://pastebin.com/mLPUEEVj
 * 1.1 - https://pastebin.com/V0emrNnw
 * 1.2 - to be Continue
 */
//-----------------------------------------------------------
//Before Commit the Code:
// - Remove all the comments. (/\*([^*]|[\r\n]|(\*+([^*/]|[\r\n])))*\*+/|[ \t]*//.*)
// - Remove all Debug messages.
// - Using Obfuscate Program to change varibles name
// - convert into UniCodes

public class MyBot implements PirateBot {


    public static PirateGame gameInstance;

    @Override
    public void doTurn(PirateGame game){
        MyBot.gameInstance = game;

        PirateHandler handler = new PirateHandler();
        handler.doWork();

    }
}
class Engine
{
    //INIT the game Varibale
    private static final PirateGame game;
    static {
        game = MyBot.gameInstance;
    }
    public static Capsule getClosestCapsule()
    {
        List<Capsule> capsules = Arrays.asList(game.getMyCapsule());
        return capsules.get(0);
    }
    //In case there is more then 1
    public static Mothership getClosestMotherShip()
    {
        List<Mothership> motherships = Arrays.asList(game.getMyMothership());
        return motherships.get(0);
    }

    public static Location pushAwayFromShip(MapObject obj)
    {
        Mothership enemyShip = game.getEnemyMothership();
        int x,y;
        y = enemyShip.location.col < obj.getLocation().col ? 9000 : -9000;
        x = enemyShip.location.row < obj.getLocation().row ? 9000 : -9000;
        return new Location(x,y);
    }
}

//Class that represnt a single Pirate
class BotPirate
{
    //INIT the game Varibale
    private static final PirateGame game;
    static {
        game = MyBot.gameInstance;
    }

    Pirate pirate;
    MapObject destenation;
    Jobs job;
    boolean didPush;

    //Constructor
    public BotPirate(Pirate pirate) {
        this.pirate = pirate;
        this.destenation = null;
        this.job = Jobs.CAPSULER;
        this.didPush = false;
    }



    //Getter Setter
    public Pirate getPirate() {
        return pirate;
    }
    public void setPirate(Pirate pirate) {
        this.pirate = pirate;
    }
    public MapObject getDestenation() {
        return destenation;
    }
    public void setDestenation(MapObject destenation) {
        this.destenation = destenation;
    }
    public Jobs getJob() {
        return job;
    }
    public void setJob(Jobs job) {
        this.job = job;
    }
    public boolean isDidPush() {
        return didPush;
    }
    public void setDidPush(boolean didPush) {
        this.didPush = didPush;
    }


    public void tryPush() {
        // Go over all enemies.
        for (Pirate enemy: game.getEnemyLivingPirates()) {
            // Check if the pirate can push the enemy.
            if (pirate.canPush(enemy)) {
                // Push enemy!
                pirate.push(enemy, enemy.initialLocation);

                // Print a message.
                System.out.println("pirate " + pirate + " pushes " + enemy + " towards " + enemy.initialLocation);

                // Did push.
                didPush = true;
            }
        }
    }


    //Sail to the Destenation
    public void sailToDest()
    {
        if(!pirate.isAlive())
        {
            this.job = Jobs.DEAD;
            return;
        }
        if(this.didPush)
        {
            game.debug("Already push");
            return;
        }
        if(this.destenation == null)
        {
            game.debug("No Dest");
            return;
        }
        pirate.sail(destenation);
    }


    //Camper Way of Thinking
    public void camperLogic()
    {
        for(Pirate p : game.getAllEnemyPirates())
        {

            if(p.hasCapsule())
            {
                this.destenation = p;
            }

        }
        //push the holder away from the ship
        if(destenation != null)
        {
            if(this.pirate.canPush(destenation))
            {
                this.pirate.push((Pirate)destenation,Engine.pushAwayFromShip(destenation));
                this.didPush = true;
            }
        }
        //Case Enemy dont have any Capsuls on them
        //Follow the colesest enemy to the capsuls
        if(destenation == null)
        {
            this.destenation = game.getEnemyMothership();
        }
    }

    //Capsuler Way of Thinking
    public void capsulerLogic()
    {
        tryPush();
        if(didPush)
        {
            game.debug("Already push");
            return;
        }
        if(this.pirate.capsule == null)
        {
            destenation = Engine.getClosestCapsule();
        }
        else
        {
            destenation = Engine.getClosestMotherShip();
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
            default:
                game.debug("No Jobs Found");
                break;
        }
    }
}

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

    public void sortingByDistance(MapObject obj)
    {
        Collections.sort(pirates, (p1,p2) -> p1.pirate.distance(obj) - p2.pirate.distance(obj));
    }

    public boolean cpsuleOwn()
    {
        for(BotPirate p : pirates)
        {
            if(p.pirate.hasCapsule()) return true;
        }
        return false;
    }



    public void doWork()
    {
        switch (tatic)
        {

            case BASICV2:
                sortingByDistance(Engine.getClosestCapsule());
                int j = 0;
                for(BotPirate p : pirates)
                {
                    if(!p.pirate.isAlive())
                    {
                        p.setJob(Jobs.DEAD);
                    }
                    else if(j<3)
                    {
                        p.setJob(Jobs.CAPSULER);
                    }
                    else
                    {
                        p.setJob(Jobs.CAMPER);;
                    }
                    p.work();
                    ++j;
                }
                break;

            case BASIC:
                int i = 0;
                int more = pirates.size() / 2;
                int allready = 0;
                for(BotPirate p : pirates)
                {
                    if(!p.pirate.isAlive())
                    {
                        p.setJob(Jobs.DEAD);
                    }
                    if(i%2 == 0 && allready < more)
                    {
                        p.setJob(Jobs.CAMPER);
                        allready++;
                    }
                    else
                    {
                        p.setJob(Jobs.CAPSULER);
                    }
                    p.work();
                    i++;
                }
                break;
            default:
                for(BotPirate p : pirates)
                {
                    p.work();
                }
                break;



        }


    }
}

//Jobs for Pirates
enum Jobs
{
    DEAD,CAPSULER,CAMPER,SAVER;
}


//Tactics
enum Tactics
{
    BASIC, BASICV2;
}
