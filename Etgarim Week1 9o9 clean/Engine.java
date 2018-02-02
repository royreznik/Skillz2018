package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pirates.Asteroid;
import pirates.Capsule;
import pirates.Location;
import pirates.MapObject;
import pirates.Mothership;
import pirates.Pirate;
import pirates.PirateGame;

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
    public static Capsule getClosestMyCapsule(Pirate p) {
        List<Capsule> capsules = Arrays.asList(game.getMyCapsules());
        Collections.sort(capsules, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return capsules.get(0);
    }

    public static Capsule getClosestMyCapsuleByIndex(Pirate p, int x) {
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
    public static Capsule getClosestEnemyCapsule(Pirate p) {
        List<Capsule> capsules = Arrays.asList(game.getEnemyCapsules());
        Collections.sort(capsules, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return capsules.get(0);
    }

    /**
     * @param p
     * @return The closest my mothership to the given pirate
     */
    public static Mothership getClosestMyMothership(Pirate p) {
        List<Mothership> motherships = Arrays.asList(game.getMyMotherships());
        Collections.sort(motherships, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return motherships.get(0);
    }

    /**
     * @param p
     * @return The closest enemy mothership to the given pirate
     */
    public static Mothership getClosestEnemyMothership(Pirate p) {
        List<Mothership> motherships = Arrays.asList(game.getEnemyMotherships());
        Collections.sort(motherships, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return motherships.get(0);
    }


    /**
     * @param p
     * @return The closest mothership (either your's or enemy's) to the given pirate
     */
    public static Mothership getClosestAnyMothership(Pirate p) {
        List<Mothership> motherships = Arrays.asList(game.getEnemyMotherships());
        motherships.addAll(Arrays.asList(game.getMyMotherships()));
        Collections.sort(motherships, (m1, m2) -> m1.distance(p) - m2.distance(p));
        return motherships.get(0);
    }

    /**
     * @return Enemy Capsuler SHOULD NOT BE USED!
     */
//    public static Pirate getEnemyCapsuler() {
//        for (Pirate p : game.getEnemyLivingPirates()) {
//            if (p.hasCapsule()) {
//                return p;
//            }
//        }
//        return null;
//    }

    /**
     * @param p given pirate
     * @return the closest enemy capsuler to the given pirate! if enemy doesn't have
     * any capsulers, returns the closest enemy to an enemy's capsule (initial location)!
     */
    public static Pirate getClosestEnemyCapsuler(Pirate p) {

        List<Pirate> enemys;
        ArrayList<Pirate> enemyCapsulers = new ArrayList<>();
        boolean found = false;

        for (Pirate pirate : game.getEnemyLivingPirates()) {
            if (p.hasCapsule()) {
                enemyCapsulers.add(pirate);
                found = true;
            }
        }
        if (found) {
            Collections.sort(enemyCapsulers, (e1, e2) -> e1.distance(p) - e2.distance(p));
            return enemyCapsulers.get(0);
        }

        enemys = Arrays.asList(game.getEnemyLivingPirates());
        Collections.sort(enemys,
                (e1, e2) -> e1.distance(getClosestEnemyMothership(e1)) - e2.distance(getClosestEnemyMothership(e2)));
//        Collections.sort(myPirates,
//                (e1, e2) -> e1.distance(getClosestEnemyCapsule(e1)) - e2.distance(getClosestEnemyCapsule(e2)));

        try {
            // for some reason game.getEnemyLivingPirates(0) crashes the bot -
            // ArrayIndexOutOfBoundsException
            return enemys.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            game.debug(e.getStackTrace());
        }
        if (game.getEnemyLivingPirates().length > 0) {
            return game.getEnemyLivingPirates()[0];
        }
        return game.getAllEnemyPirates()[0];

    }

    /**
     * @param p closest my capsuler to this pirate
     * @return the closest my capsuler to the given pirate! if we don't have any
     * capsulers, returns the closest friend to a my mothership.!
     */
    public static Pirate getClosestMyCapsuler(Pirate p) {

        List<Pirate> myPirates;
        ArrayList<Pirate> myCapsulers = new ArrayList<>();
        boolean found = false;

        for (Pirate pirate : game.getMyLivingPirates()) {
            if (p.hasCapsule()) {
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

    public static Pirate getClosestFriend(Pirate p) {
        List<Pirate> myPirates = Arrays.asList(game.getMyLivingPirates());
        Collections.sort(myPirates, (p1, p2) -> p1.distance(p) - p2.distance(p));
        // return myPirates.get(0); this returns the same pirate?
        if (myPirates.size() > 2) {
            return myPirates.get(1);
        }
        return myPirates.get(0);

    }

    public static Pirate getClosestEnemy(Pirate p) {
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
                    return new Location(row, 0);
                return new Location(0, col);
            }
            if (row < 6400 - col)
                return new Location(0, col);
            return new Location(row, 6400);
        }

        if (col < 3200) {
            if (col < 6400 - row)
                return new Location(row, 0);
            return new Location(6400, col);
        }

        if (6400 - col < 6400 - row)
            return new Location(row, 6400);
        return new Location(6400, col);
    }
}