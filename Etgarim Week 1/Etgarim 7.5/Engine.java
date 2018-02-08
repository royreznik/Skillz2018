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
     * @param p
     * @return The closest capsule to the given pirate
     */
    public static Capsule getClosestMyCapsule(MapObject p) {
        List<Capsule> capsules = Arrays.asList(game.getMyCapsules());
        Collections.sort(capsules, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return capsules.get(0);
    }

    public static Capsule getClosestMyCapsuleByIndex(MapObject p, int x) {
        List<Capsule> capsules = Arrays.asList(game.getMyCapsules());
        game.debug(capsules.get(0));
        Collections.sort(capsules, (m1, m2) -> m1.distance(p) - m2.distance(p));
        for (Capsule c : capsules) {
            if (c.holder == null) {
                return c;
            }
        }
        return capsules.get(0);
    }

    /**
     * @param p
     * @return The closest capsule to the given pirate
     */
    public static Capsule getClosestEnemyCapsule(MapObject p) {
        List<Capsule> capsules = Arrays.asList(game.getEnemyCapsules());
        Collections.sort(capsules, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return capsules.get(0);
    }

    /**
     * @param p
     * @return The closest my mothership to the given pirate
     */
    public static Mothership getClosestMyMothership(MapObject p) {
        List<Mothership> motherships = Arrays.asList(game.getMyMotherships());
        Collections.sort(motherships, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return motherships.get(0);
    }

    /**
     * @param p
     * @return The closest enemy mothership to the given pirate
     */
    public static Mothership getClosestEnemyMothership(MapObject p) {
        List<Mothership> motherships = Arrays.asList(game.getEnemyMotherships());
        Collections.sort(motherships, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return motherships.get(0);
    }


    /**
     * @param p
     * @return The closest mothership (either your's or enemy's) to the given pirate
     */
    public static Mothership getClosestAnyMothership(MapObject p) {
        List<Mothership> motherships = Arrays.asList(game.getEnemyMotherships());
        motherships.addAll(Arrays.asList(game.getMyMotherships()));
        Collections.sort(motherships, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return motherships.get(0);
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
     * @param p given pirate
     * @return the closest enemy capsuler to the given pirate! if enemy doesn't have
     * any capsulers, returns the closest enemy to an enemy's capsule (initial location)!
     */
    public static Pirate getClosestEnemyCapsuler(MapObject p) {

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
            Collections.sort(enemyCapsulers, (e1, e2) -> e1.distance(p) - e2.distance(p));
            return enemyCapsulers.get(0);
        }

        enemies = Arrays.asList(game.getEnemyLivingPirates());
        Collections.sort(enemies,
                (e1, e2) -> e1.distance(getClosestEnemyMothership(e1)) - e2.distance(getClosestEnemyMothership(e2)));
//        Collections.sort(enemies,
//                (e1, e2) -> e1.distance(getClosestEnemyCapsule(e1)) - e2.distance(getClosestEnemyCapsule(e2)));

        try {
            // for some reason game.getEnemyLivingPirates(0) crashes the bot -
            // ArrayIndexOutOfBoundsException
            return enemies.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            game.debug("getClosestEnemyCapsuler() is throwing exceptions...");
        }
        if (game.getEnemyLivingPirates().length > 0) {
            return game.getEnemyLivingPirates()[0];
        }
        return game.getAllEnemyPirates()[0];

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

    /**
     * @param p closest my capsuler to this pirate
     * @return the closest my capsuler to the given pirate! if we don't have any
     * capsulers, returns the closest friend to a my mothership.!
     */
    public static Pirate getClosestMyCapsuler(MapObject p) {

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
            Collections.sort(myCapsulers, (e1, e2) -> e1.distance(p) - e2.distance(p));
            return myCapsulers.get(0);
        }

        myPirates = Arrays.asList(game.getMyLivingPirates());
        Collections.sort(myPirates,
                (e1, e2) -> e1.distance(getClosestMyMothership(e1)) - e2.distance(getClosestMyMothership(e2)));
//        Collections.sort(myPirates,
//                (e1, e2) -> e1.distance(getClosestMyCapsule(e1)) - e2.distance(getClosestMyCapsule(e2)));

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
//            game.debug("getClosestFriendToObject exception");
//        }
//        return myPirates.get(1);
//    }

    /**
     * @param m
     * @return closest friend to anything! will not return the same pirate if a friend is already given.
     */
    public static Pirate getClosestFriend(MapObject m) {
        List<Pirate> myPirates = Arrays.asList(game.getMyLivingPirates());
        Collections.sort(myPirates, (p1, p2) -> p1.distance(m) - p2.distance(m));
        // return myPirates.get(0); this returns the same pirate?
        try {
            for (Pirate p : myPirates) {
                if (p != null && m != p)
                    return p;
            }
            return game.getMyLivingPirates()[0];
        } catch (Exception e) {
        }
        return null;

    }

    public static Pirate getClosestEnemy(MapObject p) {
        List<Pirate> myPirates = Arrays.asList(game.getEnemyLivingPirates());
        Collections.sort(myPirates, (p1, p2) -> p1.distance(p) - p2.distance(p));
        return myPirates.get(0);
    }

    public static Asteroid getClosestAsteroid(MapObject p) {
        List<Asteroid> ads = Arrays.asList(game.getAllAsteroids());
        if (ads.size() > 0) {
            Collections.sort(ads, (a1, a2) -> a1.distance(p) - a2.distance(p));
            return ads.get(0);
        }
        return null;
    }

    public static Asteroid getClosestLivingAsteroid(MapObject p) {
        List<Asteroid> ads = Arrays.asList(game.getLivingAsteroids());
        if (ads.size() > 0) {
            Collections.sort(ads, (a1, a2) -> a1.distance(p) - a2.distance(p));
            return ads.get(0);
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
            return myCapsulers.get(0).distance(getClosestMyMothership(myCapsulers.get(0))) / 200.0;
        }
        return 9999;
    }

    public static MapObject pushAsteroidTo(Asteroid asteroid) {
        Mothership m;
        Pirate[] enemies = game.getEnemyLivingPirates();
        Pirate[] myPirates = game.getMyLivingPirates();
        List<Mothership> motherships = Arrays.asList(game.getMyMotherships());
        Collections.sort(motherships, (m1, m2) -> m1.distance(asteroid) - m2.distance(asteroid));
        for (int i = 0; i < motherships.size(); i++) {
            if (Engine.areThereCampers(motherships.get(i))
                    && Engine.minCapsulerTurnsToMothership() < asteroid.spawnTurns) {
                game.debug("in");
                m = motherships.get(i);
                Pirate camper = Engine.getClosestEnemy(m);
                if (camper.distance(Engine.getClosestEnemy(camper)) < asteroid.size) {
                    Pirate friend = Engine.getClosestFriend(camper);
                    ArrayList<Pirate> enemyGroup = new ArrayList<>();
                    ArrayList<Pirate> friendGroup = new ArrayList<>();
//                    if (camper.distance(friend) > asteroid.size) {
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
//                        game.debug("contains...");
//                        present=true;
//                    }
//                }
                if (current.contains(enemies[i]))
                    present = true;
            }
            if (!present) {
//                ArrayList<Pirate> temp = new ArrayList<>();
//                game.debug("Temp size = " + temp.size());
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
        //     game.debug("Size=" + p.size() + "\n" + p + "\n");
        // }
        //game.debug("--------------");
//        game.debug("0 size= " + enemyGroups.get(0).size());
//        game.debug("1 size= " + enemyGroups.get(1).size());

        for (ArrayList<Pirate> current : enemyGroups) {
//            current.sort((g1, g2) -> g1.distance(asteroid) - g2.distance(asteroid));
//            Pirate target = current.get(0);
//            ArrayList<MapObject> temp3 = new ArrayList<>();
//            temp3.addAll(current);
            MapObject midPoint = Engine.midPoint(current);
            Pirate closeFriend = Engine.getClosestFriend(midPoint);
            if (closeFriend.distance(midPoint) > asteroid.size) {
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

//    public static MapObject midPoint(ArrayList<MapObject> locations) {
//        try {
//            double rowSum = 0, colSum = 0;
//            int count = 0;
//            for (MapObject l : locations) {
//                count++;
//                rowSum += l.getLocation().row;
//                colSum += l.getLocation().col;
//            }
//            int row = (int) Math.round(rowSum / (double) count);
//            int col = (int) Math.round(colSum / (double) count);
//            Location midPoint = new Location(row, col);
//            game.debug("MIDPOINT = " + midPoint);
//            return midPoint;
//        } catch (Exception e) {
//            game.debug("Exception in midPoint!");
//            return locations.get(0);
//        }
//    }

    public static <T extends MapObject> Location midPoint(List<T> objects) {
        try {
            double rowSum = 0, colSum = 0;
            int count = 0;
            for (T l : objects) {
                count++;
                rowSum += l.getLocation().row;
                colSum += l.getLocation().col;
            }
            int row = (int) Math.round(rowSum / (double) count);
            int col = (int) Math.round(colSum / (double) count);
            Location midPoint = new Location(row, col);
            game.debug("MIDPOINT = " + midPoint);
            return midPoint;
        } catch (Exception e) {
            game.debug("Exception in midPoint!");
            return objects.get(0).getLocation();
        }
    }

    public static <T extends MapObject> Location midPoint(T[] objects) {
        try {
            double rowSum = 0, colSum = 0;
            int count = 0;
            for (T l : objects) {
                count++;
                rowSum += l.getLocation().row;
                colSum += l.getLocation().col;
            }
            int row = (int) Math.round(rowSum / (double) count);
            int col = (int) Math.round(colSum / (double) count);
            Location midPoint = new Location(row, col);
            game.debug("MIDPOINT = " + midPoint);
            return midPoint;
        } catch (Exception e) {
            game.debug("Exception in midPoint!");
            return objects[0].getLocation();
        }
    }

    /**
     * @return the location of the nearest wall/border to the pirate. Example - If
     * the method runs on a Pirate at (100,4600) the method will return
     * (0,4600) - a straight line towards the nearest wall.
     */
    public static Location nearestWall(MapObject obj) {
        int row = obj.getLocation().row;
        int col = obj.getLocation().col;
        // TODO - change 0&6400 to out of bounds values -> -1&6401 (?)
        if (row < 3200) {
            if (col < 3200) {
                if (col < row)
                    return new Location(row, -1);
                return new Location(-1, col);
            }
            if (row < 6400 - col)
                return new Location(-1, col);
            return new Location(row, 6401);
        }

        if (col < 3200) {
            if (col < 6400 - row)
                return new Location(row, -1);
            return new Location(6401, col);
        }

        if (6400 - col < 6400 - row)
            return new Location(row, 6401);
        return new Location(6401, col);
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
     * checks if the space object can hit the map object
     *
     * @param obj
     * @param loc
     * @return true if the space object can hit
     */
    public static boolean canObjectHit(SpaceObject obj, MapObject loc) {
        return obj.inRange(loc, obj.size);
    }

}