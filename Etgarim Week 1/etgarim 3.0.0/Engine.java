package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	 * @return The Closest Capsule from the Pirate
	 */
	public static Capsule getClosestMyCapsule(Pirate p) {
		List<Capsule> capsules = Arrays.asList(game.getMyCapsules());
		Collections.sort(capsules, (m1, m2) -> m1.distance(p) - m2.distance(p));
		return capsules.get(0);
	}

	/**
	 * @param p
	 * @return The Closest My Mothership from the given pirate
	 */
	public static Mothership getClosestMyMothership(Pirate p) {
		List<Mothership> motherships = Arrays.asList(game.getMyMotherships());
		Collections.sort(motherships, (m1, m2) -> m1.distance(p) - m2.distance(p));
		return motherships.get(0);
	}

	/**
	 * @param p
	 * @return The Closest Enemy Mothership from the given pirate
	 */
	public static Mothership getClosestEnemyMothership(Pirate p) {
		List<Mothership> motherships = Arrays.asList(game.getEnemyMotherships());
		Collections.sort(motherships, (m1, m2) -> m1.distance(p) - m2.distance(p));
		return motherships.get(0);
	}

	/**
	 * @param p
	 * @return The Closest Mothership (either your's or enemy's) from the given
	 *         pirate
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
	public static Pirate getEnemyCapsuler() {
		for (Pirate p : game.getEnemyLivingPirates()) {
			if (p.hasCapsule()) {
				return p;
			}
		}
		return null;
	}

	/**
	 * @param p
	 *            closest enemy capsuler to this pirate
	 * @return the closest enemy capsuler to the given pirate! if enemy don't have
	 *         any capsulers, returns the closest enemy to an enemy's mothership!
	 */
	public static Pirate getClosestEnemyCapsuler(Pirate p) {

		List<Pirate> enemys;
		ArrayList<Pirate> enemyCapsulers = new ArrayList<>();
		boolean found = false;

		for (Pirate pirate : game.getAllEnemyPirates()) {
			if (p.hasCapsule()) {
				enemyCapsulers.add(pirate);
				found = true;
			}
		}
		if (found) {
			Collections.sort(enemyCapsulers, (e1, e2) -> e1.distance(p) - e2.distance(p));
			return enemyCapsulers.get(0);
		}

		enemys = Arrays.asList(game.getAllEnemyPirates());
		Collections.sort(enemys,
				(e1, e2) -> e1.distance(getClosestEnemyMothership(e1)) - e2.distance(getClosestEnemyMothership(e2)));

		try {
			// for some reason game.getEnemyLivingPirates(0) crashes the bot -
			// ArrayIndexOutOfBoundsException
			return enemys.get(0);
		} catch (ArrayIndexOutOfBoundsException e) {
			game.debug(e.getStackTrace());
		}
		return game.getAllEnemyPirates()[0];
	}

	/**
	 * @param p
	 *            closest my capsuler to this pirate
	 * @return the closest my capsuler to the given pirate! if we don't have any
	 *         capsulers, returns the closest friend to a myMothership!
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

	/**
	 * @param obj
	 * @return Which direction the pirate should push the obj to. Currently its
	 *         Stupid af, and works poorly
	 */
	public static Location pushAwayFromShip(MapObject obj) {
		Mothership enemyShip = game.getEnemyMotherships()[0];
		int x, y;
		y = enemyShip.location.col < obj.getLocation().col ? 9000 : -9000;
		x = enemyShip.location.row < obj.getLocation().row ? 9000 : -9000;
		return new Location(x, y);
	}

	/**
	 * @return the location of the nearest wall/border to the pirate. Example - If
	 *         the method runs on a Pirate at (100,4600) the method will return
	 *         (0,4600) - a straight line towards the nearest wall.
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