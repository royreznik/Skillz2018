package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;
import pirates.*;


//TODO: Job that attack the Pirate that have the enemy capsul, Check for Campers

public class MyBot implements PirateBot {


    public static PirateGame gameInstance;

    @Override
    public void doTurn(PirateGame game){
        MyBot.gameInstance = game;

        PirateHandler handler = new PirateHandler();
        handler.doWork();

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
    public Capsule getClosestCapsul()
    {
        List<Capsule> capsules = Arrays.asList(game.getMyCapsule());
        return capsules.get(0);
    }
    //In case there is more then 1
    public Mothership getClosestMotherShip()
    {
        List<Mothership> motherships = Arrays.asList(game.getMyMothership());
        return motherships.get(0);
    }

    public void sailToDest()
    {
        if(this.destenation == null)
        {
            game.debug("No Dest");
            return;
        }
        pirate.sail(destenation);
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
                if(this.pirate.capsule == null)
                {
                    destenation = getClosestCapsul();
                }
                else
                {
                    destenation = getClosestMotherShip();
                }
                sailToDest();
                break;
            default:
                game.debug("No Jobs Found");
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
    DEAD,CAPSULER,CAMPER;
}

enum Tactics
{
    BASIC;
}
