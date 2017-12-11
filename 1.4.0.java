package bots;
 
 
 

import pirates.*;

import java.util.*;

 
 

 
 
public class MyBot implements PirateBot {


 
    public static PirateGame gameInstance;
    PirateHandler handler;

    @Override
    public void doTurn(PirateGame game){
        MyBot.gameInstance = game;

        handler = new PirateHandler();
        handler.doWork();

    }
}

 
class Engine
{
 
    private static final PirateGame game;
    static {
        game = MyBot.gameInstance;
    }

     
    public static Capsule getClosestCapsule()
    {
        List<Capsule> capsules = Arrays.asList(game.getMyCapsule());
        return capsules.get(0);
    }

     
    public static Mothership getClosestMyMotherShip()
    {
        List<Mothership> motherships = Arrays.asList(game.getMyMothership());
        return motherships.get(0);
    }

     
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
        List<Pirate> myPirats = Arrays.asList(game.getMyLivingPirates());
        Collections.sort(myPirats, (p1,p2) -> p1.distance(p) - p2.distance(p));
        return myPirats.get(0);
    }
    public static Pirate getClosestEnemy(Pirate p)
    {
        List<Pirate> myPirats = Arrays.asList(game.getEnemyLivingPirates());
        Collections.sort(myPirats, (p1,p2) -> p1.distance(p) - p2.distance(p));
        return myPirats.get(0);
    }


     
    public static Location pushAwayFromShip(MapObject obj)
    {
        Mothership enemyShip = game.getEnemyMothership();
        int x,y;
        y = enemyShip.location.col < obj.getLocation().col ? 9000 : -9000;
        x = enemyShip.location.row < obj.getLocation().row ? 9000 : -9000;
        return new Location(x,y);
    }

     
    public static Location nearestWall(MapObject obj) {
        int row = obj.getLocation().row;
        int col = obj.getLocation().col;
 
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

 
class BotPirate {
 
    private static final PirateGame game;

    static {
        game = MyBot.gameInstance;
    }

    Pirate pirate;
    MapObject destination;
    Job job;
    boolean didPush;

 
    public BotPirate(Pirate pirate) {
        this.pirate = pirate;
        this.destination = null;
        this.job = Job.CAPSULER;
        this.didPush = false;
    }

 
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

 
    public void tryPush() {
 
        for (Pirate enemy : game.getEnemyLivingPirates()) {
 
            if (pirate.canPush(enemy)) {
 
                pirate.push(enemy, enemy.initialLocation);

 
                System.out.println("pirate " + pirate + " pushes " + enemy + " towards " + enemy.initialLocation);

 
                didPush = true;
            }
        }
    }

     
    public void tryPushPirate(Pirate p, MapObject obj)
    {
        if (pirate.canPush(p)) {
 
            pirate.push(p, obj.getLocation());
 
            didPush = true;
        }
    }


 
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

     
    public Location nearestWall() {
        int row = this.pirate.location.row;
        int col = this.pirate.location.col;
 
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

     
    public Pirate myHelper() {
        Pirate closest = game.getMyLivingPirates()[0];
        for (Pirate p : game.getMyLivingPirates()) {
            if (p != this.pirate && p.distance(this.pirate) < closest.distance(this.pirate)) {
                closest = p;
            }
        }
        return closest;
    }

 
    public void camperLogic() {
        for (Pirate p : game.getAllEnemyPirates()) {
            if (p.hasCapsule()) {
                this.destination = p;
            }
        }
 
        if (destination != null) {
            Location nearestWall = Engine.nearestWall(destination);
            Location motherWall = Engine.nearestWall(game.getEnemyMothership());
            if ((nearestWall.col == motherWall.col || nearestWall.row == motherWall.row)
                    && destination.distance(nearestWall) >= 600) {
 
                this.tryPushPirate((Pirate) destination, Engine.pushAwayFromShip(destination));
            } else
                this.tryPushPirate((Pirate) destination, nearestWall);
        }
 
 
        if (destination == null) {
            this.destination = game.getEnemyMothership();
        }
        Pirate enemycapsuler = Engine.getEnemyCapsuler();
        if(enemycapsuler != null && pirate.distance(enemycapsuler) > 599)
        {
            if(enemycapsuler.distance(game.getEnemyMothership()) < pirate.distance(game.getEnemyMothership()))
                this.tryPushPirate(Engine.getClosestFriend(pirate),enemycapsuler);
        }

    }

 
    public void capsulerLogic() {
        Mothership myMothership = game.getMyMothership();
        boolean campersOnRadius = false;
        for (Pirate enemy : game.getAllEnemyPirates())
            if (enemy.distance(myMothership) <= 500) { 
                campersOnRadius = true;
                game.debug("CAPSULER Campers close to radius (<=500)");
            }
        if (didPush) {
            game.debug("Already pushed");
            return;
        }
        if (!this.pirate.hasCapsule()) {
            destination = Engine.getClosestCapsule();
        } else if (campersOnRadius && this.pirate.distance(myMothership) != 899) {
 
            destination = myMothership.getLocation().towards(game.getMyCapsule().initialLocation, 899);
            return;
        } else {
 
 
 
            destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
        }
        Pirate enemy = Engine.getClosestEnemy(this.pirate);
        tryPushPirate(enemy, Engine.nearestWall(enemy));
    }

 
 
 
    public void antiCamperLogic() {
        Mothership myMothership = game.getMyMothership();
        boolean campersOnRadius = false;
        Pirate capsuler = getMyCapsuler();
        for (Pirate enemy : game.getAllEnemyPirates()) {
            if (enemy.distance(myMothership) <= 500) { 
                campersOnRadius = true;
                game.debug("ANTI DETECT Campers close to radius (<=500)");
            }
        }
        if (!campersOnRadius && !pirate.canPush(capsuler)) {
 
            destination = capsuler;
            game.debug("CAPSULER IS DESTINATION");
            return;
        }
        if (campersOnRadius && this.pirate.distance(myMothership) >= 801) {
 
            destination =myMothership.getLocation().towards(game.getMyCapsule().initialLocation, 801);
            game.debug("IM GOING to 801 from myMothership");
            return;
        }
 
        if (capsuler.hasCapsule()) { 
            game.debug("CAPSULER HAS CAPSULE");
            for (Pirate enemy : game.getAllEnemyPirates()) {
                if (enemy.distance(capsuler) < 600) { 
                    game.debug("pushing creeper");
                    this.tryPushPirate(capsuler, myMothership); 
                    return;
                }
            }
            if (campersOnRadius) { 
                game.debug("CAMPERS DETECTED");
                game.debug("CAPSULER IN POSITION?");
                if (capsuler.distance(myMothership) <= 900) {
 
                    this.tryPushPirate(capsuler, myMothership); 
                    game.debug("IM TRYING TO THE PUSH CAPSULER");
                }
                return;
            }
 
 
        }
        return; 
    }

 
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

     
    public void sortingByDistance(MapObject obj)
    {
        Collections.sort(pirates, (p1, p2) -> p1.pirate.distance(obj) - p2.pirate.distance(obj));
    }

     
    public boolean capsuleOwn()
    {
        for(BotPirate p : pirates)
        {
            if(p.pirate.hasCapsule()) return true;
        }
        return false;
    }

 
 
 
    public void doWork()
    {
        int i = 0;
        switch (tactic)
        {
            case STANDARD:
                sortingByDistance(Engine.getClosestCapsule());
                int count = 0;
                for(BotPirate p : pirates)
                {
                    if(!p.pirate.isAlive())
                    {
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
            case BASICV4: 
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
            case BASICV3: 
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
 
            default:
                for(BotPirate p : pirates)
                {
                    p.work();
                }
                break;



        }


    }
}

 
enum Job
{
    DEAD,CAPSULER,CAMPER,ANTICAMPER,SAVER;
}


 
enum Tactic
{
    BASIC, BASICV2, BASICV3, BASICV4, STANDARD;
}
