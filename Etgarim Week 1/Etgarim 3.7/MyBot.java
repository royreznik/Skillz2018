package bots;

//-----------------------------------------------------------
/**
 * @author RoyRenzik
 * @author ElayM
 * @version Etgarim 2.0 (Based on 1.4.5)
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
// updated 90% of things to work with the new API, new methods, new tactic.
// #MarzukMasterRace
public class MyBot implements PirateBot {

	// Static Variable for the game
	public static PirateGame gameInstance;
	PirateHandler handler;

	@Override
	public void doTurn(PirateGame game) {
		MyBot.gameInstance = game;

		handler = new PirateHandler();
		game.debug(game.getEnemy().botName);

		// Handle Different Bot types:
		// OneManArmy
		if (game.getEnemy().botName.equals("25186")) {
			game.debug("Name: OneManArmy");
			handler.setTactic(Tactic.OneManArmy);
		}
		
		//You Shell Not Pass
		else if(game.getEnemy().botName.equals("25191"))
		{
		    game.debug("Name: OneManArmy");
		    handler.setTactic(Tactic.COOL); 
		}
		else{
		    
		}
		handler.doWork();

	}
}
