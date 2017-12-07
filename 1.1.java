package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pirates.*;


//TODO: Job that attack the Pirate that have the enemy capsul, Check for Campers
//TODO: Priority to double push


/***
 * Vers:
 * 1.0 - https://pastebin.com/mLPUEEVj
 * 1.1 -
 */

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

    //In case there is more then 1


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
        tatic = Tactics.BASIC;
    }

    public void doWork()
    {
        switch (tatic)
        {
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
    BASIC;
}
