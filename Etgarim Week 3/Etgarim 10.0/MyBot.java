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
            BotPirate.startVariables();
            handler = new PirateHandler();
            handler.setTactic(Tactic.SPLIT);
            game.debug(game.getEnemy().botName);

            // Handle Different Bot types:
<<<<<<< HEAD
            int bot = Integer.parseInt(game.getEnemy().botName);
            switch(bot){
                case 25186: //one man army
                    handler.setTactic(Tactic.OneManArmy);
                    break;
                case 25191: //you shall not pass
                    handler.setTactic(Tactic.COOL);
                    break;
                case 25241: //
                    handler.setTactic(Tactic.SPAGHT);
                    break;
                case 25236: //outOfSpace
                    break;
                case 25766: //stateMachine
                    handler.setTactic(Tactic.STATEMACHINE);
                    break;
                case 25772: //space race
                    handler.setTactic(Tactic.SPACERACE);
                    break;
                case 25768: //heavy lifting
                    handler.setTactic(Tactic.HEAVYLIFTING);
                    break;
                default:
                    break;
            }
            handler.doWork();
        } catch (Exception e) {
            System.out.println(e);
=======
            // OneManArmy
            if (game.getEnemy().botName.equals("25186")) {
                //game.debug("Name: OneManArmy");
                handler.setTactic(Tactic.OneManArmy);
            }

            // You Shell Not Pass
            else if (game.getEnemy().botName.equals("25191")) {
                //game.debug("Name: You shell not pass");
                handler.setTactic(Tactic.COOL);
            } else if(game.getEnemy().botName.equals("25241")) {
                handler.setTactic(Tactic.SPAGHT);
            }
            else if(game.getEnemy().botName.equals("25236")){
                //BotPirate 463 - OutOfSpace
            }
            handler.doWork();
        } catch (Exception e) {
            System.out.println("Bug");
>>>>>>> 33e9a227669746eb870bcbcaa0439b03c31563d2
        }
    }
}