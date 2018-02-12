package bots;

import java.util.HashSet;

//-----------------------------------------------------------
/**
 * @author RoyRenzik
 * @author ElayM
 * @author Alon-Maor
 * @author Gal258
 * @author Berensohn
 * @version Etgarim 6.0
 **/

//-----------------------------------------------------------

import pirates.PirateBot;
import pirates.PirateGame;
import pirates.Asteroid;

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
        try {
            MyBot.gameInstance = game;
            PirateNavigator.alreadyPushed = new HashSet<Asteroid>();
            handler = new PirateHandler();
            game.debug(game.getEnemy().botName);

            // Handle Different Bot types:
            // OneManArmy
            if (game.getEnemy().botName.equals("25186")) {
                game.debug("Name: OneManArmy");
                handler.setTactic(Tactic.OneManArmy);
            }

            // You Shell Not Pass
            else if (game.getEnemy().botName.equals("25191")) {
                game.debug("Name: You shell not pass");
                handler.setTactic(Tactic.COOL);
            } else {

            }
            handler.doWork();
        } catch (Exception e) {
        }
    }
}