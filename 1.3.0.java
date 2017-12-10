package bots;
//-----------------------------------------------------------
/**
 * @author      RoyRenzik
 * @author      ElayM
 * @version     1.2.9
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

//THIS VERSION HASNT BEEN CHECKED!
//Added: Defender Bot, availablePush Method
//Changed: make Handler Static varibale to makes calcualtion much faster and easier, getEnemyCapsuler return the Closest Enemy to the capsuler if there is not capsuler
//Added: A*
//TODO: Make the efficiency of A* much better.
public class MyBot implements PirateBot {


    //Static Variable for the game
    public static PirateGame gameInstance;
    public static PirateHandler handler;

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
     *
     * @return the Enemy Capsuler or the cloeset enemy to the Capsule
     */
    public static Pirate getEnemyCapsuler()
    {
        Pirate[] enemys = game.getAllEnemyPirates();
        if(enemys.length <1)
        {
            game.debug("All dead");
            return null;
        }
        Pirate cloeset = enemys[0];
        for (Pirate p : game.getAllEnemyPirates()) {
            //return the capsuler if there is one
            if (p.hasCapsule()) {
                return p;
            }
            //Check for the Closest enemy to the capsule
            if(cloeset.distance(game.getEnemyCapsule()) < p.distance(game.getEnemyCapsule()))
            {
                cloeset = p;
            }
        }
        return cloeset;
    }

    public static Pirate getClosestEnemy(Pirate myPirate)
    {
        List<Pirate> enemys = Arrays.asList(game.getEnemyLivingPirates());
        Collections.sort(enemys, (p1,p2) -> p1.distance(myPirate) - p2.distance(myPirate));
        return enemys.get(0);
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

    /**


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

    //TODO: Check if the Index are currect

    /**
     * Strategy:
     * 1.Check if there is double push avilable - push it
     * 2.Check if double push could in the few next turns - just follow the capsuler
     * 3.Try to push the Capsuler anyway
     * 4.if there is not Capsuler go to the closest
     */
    public void defenderLogic()
    {
        Pirate enemyCapsuler = Engine.getEnemyCapsuler();
        int willMakeIt = MyBot.handler.canItPushInTime(enemyCapsuler,4,8);
        List<BotPirate> available = MyBot.handler.availablePush(enemyCapsuler, 4,8);
        //For now if there is one more then avilaible push do all of the pushes
        //TODO: Only if needed to push, Make the tryPushPirate better with the locations
        if(available.size() > 1)
        {
            tryPushPirate(enemyCapsuler,Engine.nearestWall(enemyCapsuler));
        }
        //Check if the defenders could make the EnemyCapsuler drop the capsule in time, if they could, just keep following
        else if(available.size() == 1 && willMakeIt > 1)
        {
            destination = enemyCapsuler;
            return;
        }
        //try to push the capsuler anyway if noone coulde make it
        else if(!this.didPush)
        {
            tryPushPirate(enemyCapsuler,Engine.nearestWall(enemyCapsuler));
        }
        //Follow the capsuler if u dont have anything to do
        else
        {
            destination = enemyCapsuler;
        }

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
            Location nearestWall = Engine.nearestWall(destination);
            Location motherWall = Engine.nearestWall(game.getEnemyMothership());
            if ((nearestWall.col == motherWall.col || nearestWall.row == motherWall.row)
                    && destination.distance(nearestWall) >= 600) {
                // if the nearest wall is the same wall nearest to enemy mothership and push won't instant kill
                this.tryPushPirate((Pirate) destination, Engine.pushAwayFromShip(destination));
            } else
                this.tryPushPirate((Pirate) destination, nearestWall);
        }
        //Case Enemy don't have any Capsules on him
        //goes to mother ship
        if (destination == null) {
            this.destination = game.getEnemyMothership();
        }
    }

    //Capsuler Strategy
    public void capsulerLogic() {
        Mothership myMothership = game.getMyMothership();
        boolean campersOnRadius = false;
        int camperDist = 1500;
        //tryPush();
        for (Pirate enemy : game.getAllEnemyPirates())
            if (enemy.distance(myMothership) <= camperDist) {// enemys on/close to my mothership radius.
                camperDist = enemy.distance(myMothership);
                campersOnRadius = true;
            }
        if (!this.pirate.hasCapsule()) {
            destination = Engine.getClosestCapsule();
        }
        else
        if (!campersOnRadius || camperDist > pirate.distance(myMothership) || this.pirate.distance(myHelper()) > 300) {
            game.debug("Capsuler going to mothership radius");
            destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
            return;
        }
//        else
//            if (campersOnRadius && this.pirate.distance(myMothership) != camperDist + 301) {
//            // if there are campers, go to position
        destination = myMothership.getLocation().towards(game.getMyCapsule().initialLocation, camperDist + 301);
//            return;
//        } else {
////        else if(this.pirate.canPush(myHelper())){
//            // sails to the center of the mother ship,
//            // TODO: better if it sails until capsule is unloaded (in the 300 range of the mothership..) - DONE
//            destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
//        }
    }

    //Anti-Camper Strategy
    //TODO: fix logic to work on different type of campers. (campers standing farther then 300 from mothership..)
    //Added: new Push methods
    public void antiCamperLogic() {
        Mothership myMothership = game.getMyMothership();
        boolean campersOnRadius = false;
        int camperDist = 1500;
        Pirate capsuler = getMyCapsuler();
        Pirate camper=game.getAllEnemyPirates()[0];
        for (Pirate enemy : game.getAllEnemyPirates()) {
            if (enemy.distance(myMothership) <= camperDist) {// enemys on/close to my mothership radius.
                camperDist = enemy.distance(myMothership);
                camper=enemy;
                campersOnRadius = true;
                game.debug("AntiCamper detected Campers, "+" closest camper distance = "+camperDist);
            }
        }
        if (!campersOnRadius && !pirate.canPush(capsuler)) {
            // goes to capsuler or staying close to the closest pirate (to the capsule) until he becomes capsuler.
            destination = capsuler;
            return;
        }
        if (camper.distance(myMothership)<=1500
                &&campersOnRadius
                && this.pirate.distance(myMothership) != camper.distance(myMothership) + 301) {
            // if there are campers, go to position
            destination = myMothership.getLocation().towards(game.getMyCapsule().initialLocation, camperDist + 301);
            return;
        }
        if (capsuler.hasCapsule()) { // if anticamper is in position to push.. && if the pirate has a capsule
            game.debug("CAPSULER HAS A CAPSULE");
            if (campersOnRadius && camperDist < capsuler.distance(myMothership)) { //if there are campers
                if (pirate.canPush(capsuler) && capsuler.distance(myMothership) <= 900) {
                    // only if the push will result in a point
                    game.debug("PUSHING CAPSULER");
                    this.tryPushPirate(capsuler, myMothership); // pushes capsuler to the mothership
                } else
                    destination = myMothership.getLocation().towards(this.pirate.getLocation(), 300);
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
            case DEFENDER:
                game.debug("This pirate is an DEFENDER");
                defenderLogic();
                sailToDest();
            default:
                game.debug("No Job Found");
                break;
        }
    }


    //A* Stuff
    private static final byte INFINITY = -1;
    private Tile[][] getGraph(Location start, Location goal) {
        // get length of array outer dimension (row difference between start and goal)
        int outerLen = (Math.abs(goal.row - start.row) + 1)/2;
        // get length of array outer dimension (col difference between start and goal)
        int innerLen = (Math.abs(goal.col - start.col) + 1)/2;
        // create map to hold Graph
        Tile[][] map = new Tile[outerLen][innerLen];
        // get outer dimension modifier (is the up or down from Drone)
        int outerModifier = goal.row > start.row ? 1 : -1;
        // get inner dimension modifier (is the left or right from Drone)
        int innerModifier = goal.col > start.col ? 1 : -1;
        // now we Initialize the Locations in the array so that map[0][0] points to
        // start
        // and map[outerLen-1][innerLen-1] points to goal
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                // weight is automatically updated
                map[i][j] = new Tile(new Location(start.row + outerModifier * i, start.col + innerModifier * j));
            }
        }
        return map;
    }

    private void setDistances(Tile[][] graph) {
        // now we calculate the distances
        // map[0][0] is start so its distance is set to 0
        graph[0][0].distance = 0;
        // we start from [0][1] since [0][0] is mapped
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph[i].length; j++) {
                // get tile goal column distance
                if (j + 1 < graph[i].length)
                    updateTileDistance(graph[i][j + 1], graph[i][j].distance + graph[i][j + 1].weight);
                // get tile towards goal row distance
                if (i + 1 < graph.length)
                    updateTileDistance(graph[i + 1][j], graph[i][j].distance + graph[i + 1][j].weight);
            }
        }
    }

    private void updateSafestPath() {
        Tile[][] Graph = getGraph(this.pirate.location, Engine.getClosestMyMotherShip().location);
        setDistances(Graph);
        findFastestPath(Graph);
    }

    public void updateTileDistance(Tile t, int distance) {
        if (t.distance == INFINITY || t.distance > distance)
            t.distance = distance;
    }

    private void findFastestPath(Tile[][] graph) {
        // find fastest path
        int outerIndex = graph.length - 1;
        int innerIndex = graph[0].length - 1;
        // used to random the next tile incase of equality in distances
        Random r = new Random();
        while ((outerIndex + innerIndex) > 1) {
            // if can move both direction check which has lower distance
            if (innerIndex - 1 >= 0 && outerIndex - 1 >= 0) {
                if (graph[outerIndex][innerIndex - 1].distance > graph[outerIndex - 1][innerIndex].distance) {
                    outerIndex--;
                    this.destination = graph[outerIndex][innerIndex].location;
                    // if they are equal checks if index is even for a 50% parity rate
                } else if (graph[outerIndex][innerIndex - 1].distance == graph[outerIndex - 1][innerIndex].distance
                        && r.nextBoolean()) {
                    outerIndex--;
                    this.destination = graph[outerIndex][innerIndex].location;
                } else {
                    innerIndex--;
                    this.destination = graph[outerIndex][innerIndex].location;
                }
            }
            // can only move left/right
            else if (innerIndex - 1 >= 0) {
                innerIndex--;
                this.destination = graph[outerIndex][innerIndex].location;

            }
            // must be able to move up/down if can't move left/right and loop is called
            else {
                outerIndex--;
                // check if location is
                this.destination = graph[outerIndex][innerIndex].location;
            }
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
     * @param P Target
     * @return return List with the pirates that can push the Traget
     */
    public List<BotPirate> availablePush(Pirate P,int startIndex, int endIndex)
    {
        List<BotPirate> avi = new ArrayList<BotPirate>();
        for(int i = startIndex; i<=endIndex;i++)
        {
            if(pirates.get(i).pirate.inPushRange(P))
                avi.add(pirates.get(i));
        }
        return avi;
    }

    /**
     *
     * @param target
     * @param startIndex
     * @param endIndex
     * @return how many pirates could push before the Enemy Capsuler will get to the city
     * Only for the next 6 turns.
     */
    public int canItPushInTime(Pirate target,int startIndex,int endIndex)
    {
        int i = 0;
        for(BotPirate MyPirate: pirates)
            if(target.hasCapsule())
            {
                if(target.distance(game.getEnemyMothership())/200 > MyPirate.pirate.pushReloadTurns && MyPirate.pirate.pushReloadTurns < 6)
                {
                    if(MyPirate.pirate.distance(target)/200 < 10)
                        i++;
                }
            }
        return i;
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
     *
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
            case DFENDERTEST:
                sortingByDistance(Engine.getClosestCapsule());
                int countV2 = 0;
                for(BotPirate p : pirates)
                {
                    if(!p.pirate.isAlive())
                    {
                        p.setJob(Job.DEAD);
                    }
                    else if(countV2<1)
                    {
                        p.setJob(Job.CAPSULER);
                    }
                    else if(countV2<2){
                        p.setJob(Job.ANTICAMPER);
                    }
                    else if(countV2< 5)
                    {
                        p.setJob(Job.CAMPER);
                    }
                    else
                    {
                        p.setJob(Job.DEFENDER);
                    }
                    p.work();
                    ++countV2;
                }
                break;
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

class Tile {
    Location location;
    int weight;
    int distance;
    static PirateGame game;
    static
    {
        game = MyBot.gameInstance;
    }

    // distance must be positive so its set to INFINITY (-1) before
    // being checked
    private static final byte INFINITY = -1;

    public Tile(Location l) {
        this.location = l;
        calculateTileWeight(l);
        // set to INFINITY before being checked
        this.distance = INFINITY;
    }

    // calculates a Tiles weight
    // base weight for all Tiles is 1
    // for each Pirate looking at a Tile another 2 points are added
    /**
     *
     * @param l
     *            Location to check for
     */
    private void calculateTileWeight(Location l) {
        this.weight = 1;
        for (Pirate p : game.getEnemyLivingPirates()) {
            // if the Pirate can hit the Tile add 5 points to its weight
            if (p.inPushRange(l))
                this.weight += 5;
        }
    }
}

//Job for Pirates
enum Job
{
    DEAD,CAPSULER,CAMPER,ANTICAMPER,SAVER, DEFENDER;
}


//Tactic
enum Tactic
{
    BASIC, BASICV2, BASICV3, BASICV4, STANDARD,DFENDERTEST;
}
