package bots;

//-----------------------------------------------------------
/**
 * @author RoyRenzik
 * @author ElayM
 * @version Etgarim 3.1
 **/

//-----------------------------------------------------------
import pirates.PirateBot;
import pirates.PirateGame;

/**
 * <pre>
 * Bot for the SkillZ2018 Competition.
 * The Bot was created by Gsociety.
 * </pre>
 */
// -----------------------------------------------------------
// fixed a problem with the new methods
// #MarzukMasterRace
public class MyBot implements PirateBot {

	// Static Variable for the game
	public static PirateGame gameInstance;
	PirateHandler handler;

	@Override
	public void doTurn(PirateGame game) {
		MyBot.gameInstance = game;

		handler = new PirateHandler();

		// Handle Different Bot types:
		// OneManArmy
		if (game.getEnemy().botName.equals("25186")) {
			game.debug("Name: OneManArmy");
			handler.setTactic(Tactic.OneManArmy);
		}
		handler.doWork();

	}
}
