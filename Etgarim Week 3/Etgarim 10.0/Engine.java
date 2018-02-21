package bots;

import java.util.*;

import pirates.*;

// Class for all the calculation in the game.
public class Engine {
    // INIT the game Variable
    private static final PirateGame game;

    static {
        game = MyBot.gameInstance;
    }

    /**
     * @param obj
     * @return The closest capsule to the given pirate
     */
    public static Capsule getClosestMyCapsule(MapObject obj) {
        List<Capsule> capsules = Arrays.asList(game.getMyCapsules());
        Collections.sort(capsules, (m1, m2) -> m1.distance(obj) - m2.distance(obj));
        for (Capsule x : capsules) {
            if (!x.equals(obj))
                return x;
        }
        return capsules.get(0);
    }

    public static Capsule getClosestMyCapsuleByIndex(MapObject obj, int x) {
        List<Capsule> capsules = Arrays.asList(game.getMyCapsules());
        //game.debug(capsules.get(0));
        Collections.sort(capsules, (m1, m2) -> m1.distance(obj) - m2.distance(obj));
        for (Capsule c : capsules) {
            if (c.holder == null) {
                return c;
            }
        }
        return capsules.get(0);
    }

    /**
     * @param obj
     * @return The closest capsule to the given pirate
     */
    public static Capsule getClosestEnemyCapsule(MapObject obj) {
        List<Capsule> capsules = Arrays.asList(game.getEnemyCapsules());
        Collections.sort(capsules, (m1, m2) -> m1.distance(obj) - m2.distance(obj));
        for (Capsule x : capsules) {
            if (!x.equals(obj))
                return x;
        }
        return capsules.get(0);
    }

    /**
     * @param obj
     * @return The closest my mothership to the given pirate
     */
    public static Mothership getClosestMyMothership(MapObject obj) {
        List<Mothership> motherships = Arrays.asList(game.getMyMotherships());
        Collections.sort(motherships, (m1, m2) -> m1.distance(obj) - m2.distance(obj));
        for (Mothership x : motherships) {
            if (!x.equals(obj))
                return x;
        }
        if (game.getMyMotherships().length > 0)
            return motherships.get(0);
        else return null;
    }

    /**
     * @param obj
     * @return The closest enemy mothership to the given pirate
     */
    public static Mothership getClosestEnemyMothership(MapObject obj) {
        List<Mothership> motherships = Arrays.asList(game.getEnemyMotherships());
        Collections.sort(motherships, (m1, m2) -> m1.distance(obj) - m2.distance(obj));
        for (Mothership x : motherships) {
            if (!x.equals(obj))
                return x;
        }
        if (game.getEnemyMotherships().length > 0)
            return motherships.get(0);
        else return null;
    }

    /**
     * @param obj
     * @return The closest mothership (either your's or enemy's) to the given pirate
     */
    public static Mothership getClosestAnyMothership(MapObject obj) {
        List<Mothership> motherships = Arrays.asList(game.getAllMotherships());
        Collections.sort(motherships, (m1, m2) -> m1.distance(obj) - m2.distance(obj));
        for (Mothership x : motherships) {
            if (!x.equals(obj))
                return x;
        }
        if (game.getAllMotherships().length > 0)
            return motherships.get(0);
        else return null;
    }

    /**
     * @return Enemy Capsuler SHOULD NOT BE USED!
     */
    public static Pirate getEnemyCapsuler() {
        for (Pirate p : game.getEnemyLivingPirates()) {
            if (p.hasCapsule()) {
                return p;
            }
        }
        return null;
    }

    /**
     * @param obj given pirate
     * @return the closest enemy capsuler to the given pirate! if enemy doesn't have
     * any capsulers, returns the closest enemy to an enemy's capsule (initial location)!
     */
    public static Pirate getClosestEnemyCapsuler(MapObject obj) {

        List<Pirate> enemies;
        ArrayList<Pirate> enemyCapsulers = new ArrayList<>();
        boolean found = false;

        for (Pirate pirate : game.getEnemyLivingPirates()) {
            if (pirate.hasCapsule()) {
                enemyCapsulers.add(pirate);
                found = true;
            }
        }
        if (found) {
            Collections.sort(enemyCapsulers, (e1, e2) -> e1.distance(obj) - e2.distance(obj));
            return enemyCapsulers.get(0);
        }

        enemies = Arrays.asList(game.getEnemyLivingPirates());

//        Collections.sort(enemies, (e1, e2) -> e1.distance(getClosestEnemyMothership(e1)) - e2.distance(getClosestEnemyMothership(e2)));

        Collections.sort(enemies, (e1, e2) -> e1.distance(getClosestEnemyCapsule(e1)) - e2.distance(getClosestEnemyCapsule(e2)));

        try {
            // for some reason game.getEnemyLivingPirates(0) crashes the bot -
            // ArrayIndexOutOfBoundsException
            return enemies.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            //game.debug("getClosestEnemyCapsuler() is throwing exceptions...");
        }
        if (game.getEnemyLivingPirates().length > 0) {
            return game.getEnemyLivingPirates()[0];
        }
        return game.getAllEnemyPirates()[0];

    }

    public static List<Pirate> getMyCapsulers() {
        ArrayList<Pirate> myCapsulers = new ArrayList<>(Arrays.asList(game.getMyLivingPirates()));
        myCapsulers.removeIf(pirate -> !pirate.hasCapsule());
        return myCapsulers;
    }

    public static boolean weHoldCapsules() {
        int counter = 0;
        int counter2 = 0;
        for (Capsule c : game.getMyCapsules()) {
            if (c.holder != null) counter++;
        }
        for (Pirate p : game.getMyLivingPirates()) {
            if (p.hasCapsule()) counter2++;
        }
        return counter == counter2 && (counter > 0 || counter2 > 0);
    }
    
    public static List<Pirate> getHeavyPirates()
    {
        List<Pirate> ps  = new ArrayList<>();
        for(Pirate p : game.getMyLivingPirates())
        {
            if(p.stateName.equals("heavy"))
            {
                ps.add(p);
            }
        }
        return ps;
    }
    
    public static List<Pirate> getNormalPirates()
    {
        List<Pirate> ps  = new ArrayList<>();
        for(Pirate p : game.getMyLivingPirates())
        {
            if(p.stateName.equals("normal"))
            {
                ps.add(p);
            }
        }
        return ps;
    }
    

    /**
     * @param obj
     * @return the closest my capsuler to the given pirate! if we don't have any
     * capsulers, returns the closest friend to a my mothership.!
     */
    public static Pirate getClosestMyCapsuler(MapObject obj) {

        List<Pirate> myPirates;
        ArrayList<Pirate> myCapsulers = new ArrayList<>();
        boolean found = false;

        for (Pirate pirate : game.getMyLivingPirates()) {
            if (pirate.hasCapsule()) {
                myCapsulers.add(pirate);
                found = true;
            }
        }

        if (found) {
            Collections.sort(myCapsulers, (e1, e2) -> e1.distance(obj) - e2.distance(obj));
            for (Pirate capsuler : myCapsulers) {
                if (!capsuler.equals(obj))
                    return capsuler;
            }
        }

        myPirates = Arrays.asList(game.getMyLivingPirates());
        if(myPirates.isEmpty()){
            return game.getAllMyPirates()[0];
        }
//        Collections.sort(myPirates,
//                (e1, e2) -> e1.distance(getClosestMyMothership(e1)) - e2.distance(getClosestMyMothership(e2)));
        Collections.sort(myPirates, (e1, e2) -> e1.distance(getClosestMyCapsule(e1)) - e2.distance(getClosestMyCapsule(e2)));

        return myPirates.get(0);
    }

//    public static Pirate getClosestFriendToFriend(MapObject p) {
//        List<Pirate> myPirates = Arrays.asList(game.getMyLivingPirates());
//        Collections.sort(myPirates, (p1, p2) -> p1.distance(p) - p2.distance(p));
//        // return myPirates.get(0); this returns the same pirate?
//        if (myPirates.size() > 2) { // >= ?
//            return myPirates.get(1);
//        }
//        return myPirates.get(0);
//
//    }

//    public static Pirate getClosestFriendToObject(MapObject p) {
//        List<Pirate> myPirates = Arrays.asList(game.getMyLivingPirates());
//        Collections.sort(myPirates, (p1, p2) -> p1.distance(p) - p2.distance(p));
//        // return myPirates.get(0); this returns the same pirate?
//        try {
//            return myPirates.get(0);
//        } catch (Exception e) {
//            //game.debug("getClosestFriendToObject exception");
//        }
//        return myPirates.get(1);
//    }

    /**
     * @param obj
     * @return closest friend to anything!
     * if @param is a friendly pirate it will skip it and return the closest pirate!
     */
    public static Pirate getClosestFriend(MapObject obj) {
        List<Pirate> myPirates = Arrays.asList(game.getMyLivingPirates());
        Collections.sort(myPirates, (p1, p2) -> p1.distance(obj) - p2.distance(obj));
        try {
            for (Pirate p : myPirates) {
                if (p != null && !p.equals(obj))
                    return p;
            }
            return game.getMyLivingPirates()[0];
        } catch (Exception e) {
        }
        return null;

    }

    /**
     * @param obj
     * @return closest friend to any MapObject,
     * if @param is an enemy pirate it will skip it and return the closest pirate!
     */
    public static Pirate getClosestEnemy(MapObject obj) {
        List<Pirate> enemies = Arrays.asList(game.getEnemyLivingPirates());
        Collections.sort(enemies, (p1, p2) -> p1.distance(obj) - p2.distance(obj));
        try {
            for (Pirate p : enemies) {
                if (p != null && !p.equals(obj))
                    return p;
            }
            return game.getEnemyLivingPirates()[0];
        } catch (Exception e) {
        }
        return null;
    }


    public static Asteroid getClosestAsteroid(MapObject obj) {
        List<Asteroid> ads = Arrays.asList(game.getAllAsteroids());
        if (ads.size() > 0) {
            Collections.sort(ads, (a1, a2) -> a1.distance(obj) - a2.distance(obj));
            for (Asteroid a : ads) {
                if (!a.equals(obj))
                    return a;
            }
            if (!(obj instanceof Asteroid))
                return game.getAllAsteroids()[0];
        }
        return null;
    }

    /**
     * @param obj
     * @return closest living asteroid. if no asteroid is alive returns null.
     */
    public static Asteroid getClosestLivingAsteroid(MapObject obj) {
        List<Asteroid> ads = Arrays.asList(game.getLivingAsteroids());
        if (ads.size() > 0) {
            Collections.sort(ads, (a1, a2) -> a1.distance(obj) - a2.distance(obj));
            for (Asteroid a : ads) {
                if (!a.equals(obj))
                    return a;
            }
        }
        return null;
    }

    public static Wormhole getClosestWormhole(MapObject obj) {
        List<Wormhole> holes = Arrays.asList(game.getAllWormholes());
        if(holes.isEmpty()) return null;
        holes.sort((h1, h2) -> h1.distance(obj) - h2.distance(obj));
        try {
            for (Wormhole hole : holes) {
                if (!hole.equals(obj)) {
                    return hole;
                }
            }
        } catch (Exception e) {
        }
        return game.getAllWormholes()[0];
    }

    public static Wormhole getClosestEnemyWormhole(MapObject obj) {
        List<Wormhole> holes = Arrays.asList(game.getAllWormholes());
        if(holes.isEmpty()) return null;
        holes.sort((h1, h2) -> h1.distance(obj) - h2.distance(obj));
        try {
            for (Wormhole hole : holes) {
                if (!BotPirate.assignedHoles.contains(hole) && !hole.equals(obj)) {
                    return hole;
                }
            }
        } catch (Exception e) {
        }
        return game.getAllWormholes()[0];
    }

    public static Wormhole getClosestActiveWormhole(MapObject obj) {
        List<Wormhole> holes = Arrays.asList(game.getActiveWormholes());
        if(holes.isEmpty()) return null;
        holes.sort((h1, h2) -> h1.distance(obj) - h2.distance(obj));
        try {
            for (Wormhole hole : holes) {
                if (hole != null && !hole.equals(obj)) {
                    return hole;
                }
            }
        } catch (Exception e) {
        }
        try {
            return getClosestWormhole(obj);
        } catch (Exception e) {
        }
        return null;
    }

    public static Mothership getClosestEnemyMothershipToCapsule() {
        List<Mothership> enemyShip = Arrays.asList(game.getEnemyMotherships());
        List<Capsule> enemyCapsules = Arrays.asList(game.getEnemyCapsules());
        Mothership best = enemyShip.get(0);
        for (Mothership ship : enemyShip) {
            for (Capsule c : enemyCapsules) {
                if (ship.distance(c) < best.distance(c)) {
                    best = ship;
                }
            }
        }
        return best;
    }

    public static List<Mothership> getBestEnemyBase(List<Mothership> ships, List<Capsule> capsules) {
        if (ships.isEmpty()) {
            return null;
        }
        if (capsules.isEmpty()) {
            return null;
        }
        
        if(ships.size() == 1) return ships;

        ArrayList<Mothership> best = new ArrayList<>();
        double avg = 0;
        int counter = 0;
        Map<Double, Mothership> options = new HashMap<Double, Mothership>();
        for (Mothership ship : ships) {
            for (Capsule c : capsules) {
                avg += ship.distance(c);
                counter++;
            }
            avg /= counter;
            options.put(avg, ship);
            counter = 0;
            avg = 0;
        }
        options = new TreeMap<Double, Mothership>(options);
        for (Map.Entry<Double, Mothership> entry : options.entrySet()) {
            best.add(entry.getValue());
        }
        return best;
    }

    /**
     * @param obj
     * @return Which direction the pirate should push the obj to. Currently its
     * Stupid af, and works poorly
     */
    public static Location pushAwayFromShip(MapObject obj) {
        Mothership enemyShip = game.getEnemyMotherships()[0];
        int x, y;
        y = enemyShip.location.col < obj.getLocation().col ? 9000 : -9000;
        x = enemyShip.location.row < obj.getLocation().row ? 9000 : -9000;
        return new Location(x, y);
    }

    public static boolean isAsteroidMoving(Asteroid a) {
        return a.direction.col != 0 || a.direction.row != 0;
    }

    public static boolean areThereCampers() {
        int counter = 0;
        for (Pirate p : game.getEnemyLivingPirates()) {
            if (p.distance(game.getMyMotherships()[0]) < 2500) {
                counter++;
            }
        }
        return (counter > 1);
    }

    public static boolean areThereCampers(Mothership m) {
        int counter = 0;
        for (Pirate p : game.getEnemyLivingPirates()) {
            if (p.distance(m) < 2500) {
                counter++;
            }
        }
        return (counter > 1);
    }

    public static boolean areThereCampersAstro(Mothership m) {
        int counter = 0;
        for (Pirate p : game.getEnemyLivingPirates()) {
            if (p.distance(m) < 1000) {
                counter++;
            }
        }
        return (counter > 1);
    }

//    TODO: do.
//    public static Pirate getClosestMyCapsulerToMyMothership() {
//        ArrayList<Pirate> myCapsulers = new ArrayList<>();
//        boolean found = false;
//
//        for (Pirate pirate : game.getMyLivingPirates()) {
//            if (pirate.hasCapsule()) {
//                myCapsulers.add(pirate);
//                found = true;
//            }
//        }
//
//        if (found) {
//            myCapsulers.sort((e1, e2) -> e1.distance(getClosestMyMothership(e1)) - e2.distance(getClosestMyMothership(e2)));
//            return myCapsulers.get(0);
//        } else {
//            myCapsulers.clear();
//            myCapsulers.addAll(Arrays.asList(game.getMyLivingPirates()));
//            myCapsulers.sort((e1, e2) -> e1.distance(getClosestMyCapsule(e1)) - e2.distance(getClosestMyCapsule(e2)));
//        }
//    }

    public static double minCapsulerTurnsToMothership() {
        ArrayList<Pirate> myCapsulers = new ArrayList<>();
        boolean found = false;

        for (Pirate pirate : game.getMyLivingPirates()) {
            if (pirate.hasCapsule()) {
                myCapsulers.add(pirate);
                found = true;
            }
        }

        if (found) {
            Collections.sort(myCapsulers, (e1, e2) -> e1.distance(getClosestMyMothership(e1)) - e2.distance(getClosestMyMothership(e2)));
            Pirate capsuler = myCapsulers.get(0);
            return (capsuler.distance(getClosestMyMothership(capsuler)) - game.mothershipUnloadRange) / (double) capsuler.maxSpeed;
        } else {
            //TODO: return min distance to capsule + distance to mothership
            return 9999;
        }
    }

    public static double minTurnsToMothership(Pirate capsuler) {
        return (capsuler.distance(getClosestMyMothership(capsuler)) - game.mothershipUnloadRange) / (double) capsuler.maxSpeed;
    }

    public static double minTurnsToDest(MapObject obj, MapObject dest) {
        if (dest instanceof Wormhole)
            return (obj.distance(dest) - game.wormholeRange) / (double) game.pirateMaxSpeed;

        if (dest instanceof Mothership)
            return (obj.distance(dest) - game.mothershipUnloadRange) / (double) game.pirateMaxSpeed;

        return obj.distance(dest) / (double) game.pirateMaxSpeed;

    }

    public static double minTurnsFromObjToDest(MapObject obj, MapObject dest) {
        if (dest instanceof Wormhole)
            return (obj.distance(dest) - game.wormholeRange) / (double) game.pirateMaxSpeed;

        if (dest instanceof Mothership)
            return (obj.distance(dest) - game.mothershipUnloadRange) / (double) game.pirateMaxSpeed;

        return obj.distance(dest) / (double) game.pirateMaxSpeed;

    }

//    public static MapObject fastestWayToMothership(Pirate capsuler) {
//        double sailTurns = minTurnsToMothership(capsuler);
//        HashMap<Wormhole, Double> options = new HashMap<>();
//        double turns = 0;
//        for (Wormhole wormhole : game.getAllWormholes()) {
//            turns = 0;
//            if (wormhole.isActive) {
//                turns += Engine.minTurnsToDest(capsuler, wormhole);
//            } else {
//                turns += Math.max(Engine.minTurnsToDest(capsuler, wormhole), wormhole.turnsToReactivate);
//            }
//            turns += Engine.minTurnsFromObjToDest(wormhole.partner, Engine.getClosestMyMothership(wormhole));
//            options.put(wormhole, turns);
//        }
//        Double min = Collections.min(options.values());
//        //game.debug("Turns = " + min);
//
//        if (min < sailTurns) {
//            try {
//                for (Map.Entry<Wormhole, Double> entry : options.entrySet()) {
//                    if (entry.getValue().equals(min)) {
//                        //game.debug("Shortest route is to Wormhole #" + (entry.getKey()).id
//                                + " = " + entry.getValue() + " turns.");
//                        return entry.getKey();
//                    }
//                }
////        Iterator it = options.entrySet().iterator();
////        while (it.hasNext()) {
////            Map.Entry pair = (Map.Entry) it.next();
////            if (pair.getValue().equals(values[0])) {
////                //game.debug("Shortest route is to Wormhole #" + ((Wormhole) pair.getKey()).id
////                        + " = " + pair.getValue() + " turns.");
////                return (Wormhole) pair.getKey();
////            }
////            it.remove(); // avoids a ConcurrentModificationException
////        }
//            } catch (Exception e) {
//                //game.debug("Caught exception in fastestWayToMothership");
//            }
//        }
//        //game.debug("Shortest route is straight to the Mothership!");
//        return Engine.getClosestMyMothership(capsuler);
//    }


    public static MapObject fastestWayToDest(Pirate pirate, MapObject destination) {
        if(game.getAllWormholes().length == 0) return destination;
        int range = 0;
        if (destination instanceof Mothership)
            range = game.mothershipUnloadRange;
        else if (destination instanceof Wormhole)
            range = game.wormholeRange;

        double sailTurns = minTurnsToDest(pirate, destination);
        HashMap<Wormhole, Double> options = new HashMap<>();
        double turns;
        for (Wormhole wormhole : game.getAllWormholes()) {
            turns = 0;
            if (wormhole.isActive) {
                turns += Engine.minTurnsToDest(pirate, wormhole);
            } else {
                turns += Math.max(Engine.minTurnsToDest(pirate, wormhole), wormhole.turnsToReactivate);
            }
            turns += Engine.minTurnsFromObjToDest(wormhole.partner, destination);
            options.put(wormhole, turns);
        }
        Double min = Collections.min(options.values());
        //game.debug("Turns = " + min);

        if (min < sailTurns) {
            try {
                for (Map.Entry<Wormhole, Double> entry : options.entrySet()) {
                    if (entry.getValue().equals(min)) {
                        //game.debug("Shortest route is to Wormhole #" + (entry.getKey()).id+" = " + entry.getValue() + " turns.");
                        return entry.getKey();
                    }
                }
//        Iterator it = options.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry) it.next();
//            if (pair.getValue().equals(values[0])) {
//                //game.debug("Shortest route is to Wormhole #" + ((Wormhole) pair.getKey()).id
//                        + " = " + pair.getValue() + " turns.");
//                return (Wormhole) pair.getKey();
//            }
//            it.remove(); // avoids a ConcurrentModificationException
//        }
            } catch (Exception e) {
                //game.debug("Caught exception in fastestWayToDest");
            }
        }
        //game.debug("Shortest route is straight to the destination!");
        return destination;
    }

    public static MapObject pushAsteroidTo(Asteroid asteroid) {
        Mothership m;
        Pirate[] enemies = game.getEnemyLivingPirates();
        Pirate[] myPirates = game.getMyLivingPirates();
        List<Mothership> motherships = Arrays.asList(game.getMyMotherships());
        Collections.sort(motherships, (m1, m2) -> m1.distance(asteroid) - m2.distance(asteroid));
        // int aSize = asteroid.size * 2;
        for (int i = 0; i < motherships.size(); i++) {
            if (Engine.areThereCampers(motherships.get(i))
                    && Engine.minCapsulerTurnsToMothership() < asteroid.spawnTurns) {
                //game.debug("in");
                m = motherships.get(i);
                Pirate camper = Engine.getClosestEnemy(m);
                if (camper.distance(Engine.getClosestEnemy(camper)) < asteroid.size) {
                    Pirate friend = Engine.getClosestFriend(camper);
                    ArrayList<Pirate> enemyGroup = new ArrayList<>();
                    ArrayList<Pirate> friendGroup = new ArrayList<>();
//                    if (camper.distance(friend) > aSize) {
//                        return camper;
//                    } else {
                    int friendCounter = 1;
                    int enemyCounter = 1;
                    for (Pirate enemy : enemies) {
                        if (enemy != camper && enemy.distance(camper) < asteroid.size) {
                            enemyCounter++;
                            enemyGroup.add(enemy);
                        }
                    }
                    for (Pirate myFriend : myPirates) {
                        if (myFriend != friend && myFriend.distance(camper) < asteroid.size) {
                            friendCounter++;
                            friendGroup.add(myFriend);
                        }
                    }
                    if (enemyCounter > friendCounter) {
                        MapObject camperMidPoint = Engine.midPoint(enemyGroup);
                        MapObject friendMidPoint = Engine.midPoint(friendGroup);
                        if (friendMidPoint.distance(camperMidPoint) > asteroid.size)
                            return camperMidPoint;
//                        }
                    }
                }
            }
        }
//        int friendCounter;
//        int enemyCounter;
        ArrayList<ArrayList<Pirate>> enemyGroups = new ArrayList<>();
        ArrayList<ArrayList<Pirate>> friendGroups = new ArrayList<>();
        boolean present;
        for (int i = 0; i < enemies.length; i++) {
            ArrayList<Pirate> enemyGroup = new ArrayList<>();
            enemyGroup.add(enemies[i]);
            for (Pirate enemy : enemies) {
                if (enemies[i] != enemy && enemies[i].distance(enemy) < asteroid.size) {
                    enemyGroup.add(enemy);
                }
            }
            present = false;
            for (ArrayList<Pirate> current : enemyGroups) {
//                for (int j = 0; j < current.size(); j++) {
//                    if(current.get(j)==enemies[i])
//                    {
//                        //game.debug("contains...");
//                        present=true;
//                    }
//                }
                if (current.contains(enemies[i]))
                    present = true;
            }
            if (!present) {
//                ArrayList<Pirate> temp = new ArrayList<>();
//                //game.debug("Temp size = " + temp.size());
//                temp.addAll(enemyGroup);
//                enemyGroups.add(temp);
                enemyGroups.add(enemyGroup);
            }
//            enemyGroup.clear();
        }
        for (int i = 0; i < myPirates.length; i++) {
            ArrayList<Pirate> friendGroup = new ArrayList<>();
            friendGroup.add(myPirates[i]);
            for (Pirate myFriend : myPirates) {
                if (myPirates[i] != myFriend && myPirates[i].distance(myFriend) < asteroid.size) {
                    friendGroup.add(myFriend);
                }
            }
            present = false;
            for (ArrayList<Pirate> current : friendGroups)
                if (current.contains(myPirates[i])) present = true;
            if (!present) {
//                ArrayList<Pirate> temp2 = new ArrayList<>();
//                temp2.addAll(friendGroup);
//                friendGroups.add(temp2);
                friendGroups.add(friendGroup);
            }
//            friendGroup.clear();
        }
        enemyGroups.sort((g1, g2) -> g2.size() - g1.size());
        // for (ArrayList<Pirate> p : enemyGroups) {
        //     //game.debug("Size=" + p.size() + "\n" + p + "\n");
        // }
        ////game.debug("--------------");
//        //game.debug("0 size= " + enemyGroups.get(0).size());
//        //game.debug("1 size= " + enemyGroups.get(1).size());

        for (ArrayList<Pirate> current : enemyGroups) {
//            current.sort((g1, g2) -> g1.distance(asteroid) - g2.distance(asteroid));
//            Pirate target = current.get(0);
//            ArrayList<MapObject> temp3 = new ArrayList<>();
//            temp3.addAll(current);
            MapObject midPoint = Engine.midPoint(current);
            Pirate closeFriend = Engine.getClosestFriend(midPoint);
            if (closeFriend != null && closeFriend.distance(midPoint) > asteroid.size) {
                return midPoint;
            } else {
                for (ArrayList<Pirate> friendList : friendGroups) {
                    if (friendList.contains(closeFriend)) {
                        if (current.size() > friendList.size()) return midPoint;
                    }
                }
            }
        }

//        Collections.sort(enemyGroups, (m1, m2) -> m2.getValue() - m1.getValue());

//        ListIterator<Pair<Pirate, Integer>> iter = enemyGroups.listIterator();
//        while (iter.hasNext()) {
//            Pair<Pirate, Integer> temp = iter.next();
//            Pair<Pirate, Integer> temp2 = null;
//            if (iter.hasNext()) { // leaves 1 unchecked pair in the end if its E-zugi buts its fine...
//                temp2 = iter.next();
//                if (temp.getValue().intValue() == temp2.getValue().intValue()
//                        && temp.getKey().distance(temp2.getKey()) < asteroid.size)
//                    iter.remove();
//            }
//        }


        //Collections.sort(friendGroups, (m1, m2) -> m1.getKey().distance() - m2.getKey().distance());

        Asteroid[] livingA = game.getLivingAsteroids();
        if (livingA.length > 1) {
            List<Asteroid> ads = Arrays.asList(game.getAllAsteroids());
            Collections.sort(ads, (a1, a2) -> a1.distance(asteroid) - a2.distance(asteroid));
            try {
                if (asteroid.distance(ads.get(1)) < 1000) return ads.get(1);
            } catch (Exception e) {
            }
        }
        return Engine.nearestWall(asteroid);
        //}
    }

    public static <T extends MapObject> Location midPoint(List<T> objects) {
        try {
            double rowSum = 0, colSum = 0;
            int count = 0;
            for (T l : objects) {
                if (l != null) {
                    count++;
                    rowSum += l.getLocation().row;
                    colSum += l.getLocation().col;
                }
            }
            int row = (int) Math.round(rowSum / (double) count);
            int col = (int) Math.round(colSum / (double) count);
            Location midPoint = new Location(row, col);
            //game.debug("Midpoint = " + midPoint);
            return midPoint;
        } catch (Exception e) {
            //game.debug("Exception in list midPoint!");
            return objects.get(0).getLocation();
        }
    }

    public static <T extends MapObject> Location midPoint(T[] objects) {
        return midPoint(Arrays.asList(objects));
    }


//    public static Location nearestWall(MapObject obj) {
//
//        int maxRows = game.rows;
//        int maxCols = game.cols;
//
//        int row = obj.getLocation().row;
//        int col = obj.getLocation().col;
//
//        if (row < (maxRows / 2.0)) {
//            if (col < (maxCols / 2.0)) {
//                if (col < row)
//                    return new Location(row, -1);
//                return new Location(-1, col);
//            }
//            if (row < (maxCols - col))
//                return new Location(-1, col);
//            return new Location(row, maxCols + 1);
//        }
//
//        if (col < (maxCols / 2.0)) {
//            if (col < (maxRows - row))
//                return new Location(row, -1);
//            return new Location(maxRows + 1, col);
//        }
//
//        if ((maxCols - col) < (maxRows - row))
//            return new Location(row, maxCols + 1);
//        return new Location(maxRows + 1, col);
//    }

    /**
     * @return the location of the nearest wall/border to the pirate. Example - If
     * the method runs on a Pirate at (100,4600) the method will return
     * (0,4600) - a straight line towards the nearest wall.
     */
    public static Location nearestWall(MapObject obj) {

        int row = obj.getLocation().row;
        int col = obj.getLocation().col;

        List<Location> locations = new ArrayList<>();

        locations.add(new Location(row, game.cols + 1));
        locations.add(new Location(row, -1));
        locations.add(new Location(-1, col));
        locations.add(new Location(game.rows + 1, col));

        locations.sort((l1, l2) -> l1.distance(obj.getLocation()) - l2.distance(obj.getLocation()));
        try {
            return locations.get(0);
        } catch (Exception e) {
            return locations.get(1);
        }
    }

    /**
     * checks how many enemies can push pbj
     *
     * @param obj
     * @return number of enemies
     */
    public static int getNumPushers(MapObject obj) {
        return Arrays.asList(game.getEnemyLivingPirates()).stream().filter(
                p -> p.pushReloadTurns == 0 && p.inPushRange(obj)).toArray().length;
    }

    /**
     * checks which asteroids will hit obj in the next turn
     *
     * @param obj
     * @return an Asteroid[] of all asteroid that will hit obj next turn
     */
    public static Asteroid[] willAsteroidHit(MapObject obj) {
        return Arrays.asList(game.getAllAsteroids()).stream().filter(
                a -> (a.turnsToRevive == 1 && a.initialLocation.inRange(obj, a.size)) || a.location.inRange(obj, a.size)
                        || a.location.add(a.direction).inRange(obj, a.size)).toArray(Asteroid[]::new);

    }

    /**
     * checks if the given asteroid will hit obj in the next turn
     *
     * @param obj
     * @param a
     * @return true if asteroid will hit or false if not
     */
    public static boolean willAsteroidHit(MapObject obj, Asteroid a) {
        return (a.turnsToRevive == 1 && a.initialLocation.inRange(obj, a.size)) || a.location.inRange(obj, a.size)
                || a.location.add(a.direction).inRange(obj, a.size);
    }

    /**
     * checks if the space object can hit the map object
     *
     * @param obj
     * @param loc
     * @return true if the space object can hit
     */
    public static boolean canObjectHit(SpaceObject obj, MapObject loc) {
        return obj.inRange(loc, obj.size);
    }


    /**
     * checks if pushing the asteroid towards the destination will cause the asteroid to hit obj
     *
     * @param pusher      the pirate pushing the asteroid
     * @param a           the asteroid being pushed
     * @param destination the location the asteroid is being pushed to
     * @param obj         the object being checked to see if it will get hit
     * @return true if the push will cause a hit, false otherwise
     */
    public static boolean willPushHit(Pirate pusher, Asteroid a, MapObject destination, MapObject obj) {
        return a.location.towards(destination, pusher.pushDistance).inRange(obj, a.size);
    }
    
    /**
     * returns the closest capsule that is available or a random one if all are available
     * @param obj the object to heck distance against
     * @param unAvailableCapsules the set of the unavailable capsules
     * @return the closest capsule or a random one
     */
    public static Capsule getClosestAvailableMyCapsule(MapObject obj, Set<Capsule> unAvailableCapsules){
        Comparator<Capsule> sorter = Comparator.comparing(c -> !unAvailableCapsules.contains(c) ? 0 : 1);
        sorter = sorter.thenComparing(obj::distance);
        Capsule[] capsules = Arrays.asList(game.getMyCapsules()).stream().filter(c -> c.holder == null).sorted(sorter).toArray(Capsule[]::new);
        if(capsules.length > 0) return capsules[0];
        else return game.getMyCapsules()[0];
    }

    /**
     * checks if a push of "capsuler" by "pusher" can reach our mothership unload range.
     *
     * @param pusher   the pirate pushing the capsuler.
     * @param capsuler the pirate being pushed towards our mothership.
     * @return true if the push will result in a score (doesn't check whether the capsuler has a capsule.)
     */
    public static boolean willPushScore(MapObject pusher, MapObject capsuler) {
        Mothership myMothership = Engine.getClosestMyMothership(capsuler);
        if (pusher instanceof Pirate && myMothership != null)
            return capsuler.distance(myMothership) - myMothership.unloadRange <= ((Pirate) pusher).pushDistance;
        return false;
    }
<<<<<<< HEAD
    
    /**
     * makes a map of enemy capsules that can be dropped and the number of pushed needed for drop
     * @return map of capsules and number of pushes for drop
     */
    public static Map<Capsule, Integer> getPossibleEnemyDrops(){
        Map<Capsule, Integer> capsuleDrops = new HashMap<>();
        for(Capsule capsule : game.getEnemyCapsules()){
            Pirate holder = capsule.holder;
            if(holder == null) continue;
            int numPushers = 0;
            for(BotPirate bp : PirateHandler.pirates){
                if(!bp.job.equals(Job.CAMPER) || bp.didPush) continue;
                if(bp.pirate.canPush(holder)) numPushers++;
            }
            if(numPushers >= holder.numPushesForCapsuleLoss) capsuleDrops.put(capsule, holder.numPushesForCapsuleLoss);
        }
        return capsuleDrops;
    }
}
=======
}
>>>>>>> 33e9a227669746eb870bcbcaa0439b03c31563d2
