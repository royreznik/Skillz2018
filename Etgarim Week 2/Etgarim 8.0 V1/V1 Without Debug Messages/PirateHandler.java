package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pirates.MapObject;
import pirates.Pirate;
import pirates.PirateGame;

//Class that represent the Strategy
public class PirateHandler {
    private static final PirateGame game;

    static {
        game = MyBot.gameInstance;
    }

    List<BotPirate> pirates;
    Tactic tactic;
    DefenseHandler defense;

    public static int numOfCapsulers = -1;

    public PirateHandler() {
        pirates = new ArrayList<BotPirate>();
        for (Pirate p : game.getAllMyPirates()) {
            pirates.add(new BotPirate(p));
        }
        tactic = Tactic.TryV1;
        defense = new DefenseHandler();
    }

    public void setTactic(Tactic t) {
        this.tactic = t;
    }


    /**
     * @param obj Sorting the Pirate Array by distance from object
     */
    public void sortingByDistance(MapObject obj) {
        Collections.sort(pirates, (p1, p2) -> p1.pirate.distance(obj) - p2.pirate.distance(obj));
    }

    /**
     * Note: Can't be used in BotPirate class (roy, where is it being used?)
     *
     * @return if any of my pirates has the capsule
     */
    public boolean capsuleOwn() {
        for (BotPirate p : pirates) {
            if (p.pirate.hasCapsule())
                return true;
        }
        return false;
    }

    public int getNumOfCapsulers() {
        int counter = 0;
        for (BotPirate p : pirates) {
            if (p.getJob() == Job.CAPSULER) counter++;
        }
        return counter;
    }

    // Tactic Maker
    // Represent as Job like: Capsuler - Camper - AntiCamper and etc'
    // 4 - 4 means that there is 4 Capulsers and 4 Campers
    public void doWork() {
        int i = 0;
        List<Pirate> myPirates = Arrays.asList(game.getMyLivingPirates());
        Collections.sort(myPirates, (e1, e2) -> e1.distance(Engine.getClosestMyMothership(e1))
                - e2.distance(Engine.getClosestMyMothership(e2)));
        switch (tactic) {

            case TryV1:
                int numOfCapsule = game.getMyCapsules().length;
                int numOfAsteroids = game.getAllAsteroids().length;
                int numOfCampers = 0;
                if (numOfCapsule == 0) {
                    //game.debug("imma camper him");
                    pirates.get(0).setJob(Job.CAMPER);
                    pirates.get(0).work();
                    break;
                }
                for (int test = 0; test < game.getAllMyPirates().length; test++) {
                    if (!pirates.get(test).getPirate().isAlive()) {
                        continue;
                    }
                    if (pirates.get(test).getPirate().hasCapsule()) {
                        pirates.get(test).setJob(Job.CAPSULER);
                        pirates.get(test).work();
                        numOfCapsule--;
                        continue;
                    }
                    if (numOfCapsule >= 1 && numOfCampers > 3) {
                        pirates.get(test).setJob(Job.CAPSULER);
                        numOfCapsule--;
                        numOfCapsulers++;
                    } else if (numOfCapsule == 0 && numOfCampers > 3 && numOfAsteroids > 0) {
                        pirates.get(test).setJob(Job.ASTRO);
                        numOfCapsule--;
                    } else {
                        if (pirates.size() == 1) pirates.get(test).setJob(Job.CAPSULER);
                        else {
                            //game.debug("go camper go");
                            pirates.get(test).setJob(Job.CAMPER);
                            numOfCampers++;
                            this.defense.defenders.add(pirates.get(test));
                        }
                    }
                    pirates.get(test).work();
                }
                break;
            case REPLACER:
                int dead = 0;
                for (int j = 0; j < pirates.size(); j++) {
                    if (!pirates.get(j).pirate.isAlive()) {
                        pirates.get(j).setJob(Job.DEAD);
                        dead++;
                    } else {
                        switch ((j + 1) - dead) {
                            case 1:
                                pirates.get(j).setJob(Job.CAPSULER);
                                break;
                            case 2:
                                pirates.get(j).setJob(Job.ANTICAMPER);
                                break;
                            default:
                                pirates.get(j).setJob(Job.CAMPER);
                        }
                    }
                    pirates.get(j).work();
                }
                break;

            case STANDARD:
                // (Engine.getClosestMyCapsule());
                int count = 0;
                for (BotPirate p : pirates) {
                    if (!p.pirate.isAlive()) {
                        // --count;
                        p.setJob(Job.DEAD);
                    } else if (count < 1) {
                        p.setJob(Job.CAPSULER);
                    } else if (count < 2) {
                        p.setJob(Job.ASTRO);
                    } else {
                        p.setJob(Job.CAMPER);
                    }
                    p.work();
                    ++count;
                }
                break;
            case BASICV4: // BUG capsulers are campers
                sortingByDistance(Engine.getClosestMyCapsuler(myPirates.get(0))); // sorts with the closest myPirate to a
                // myMothership.
                i = 0;
                for (BotPirate p : pirates) {
                    if (!p.pirate.isAlive()) {
                        p.setJob(Job.DEAD);
                    } else if (i == 0) {
                        p.setJob(Job.CAPSULER);
                    }
                    if (i > 0 && i < 2) {
                        p.setJob(Job.ANTICAMPER);
                    } else {
                        p.setJob(Job.CAMPER);
                        ;
                    }
                    p.work();
                    ++i;
                }
                break;
            case BASICV3: // BUG capsulers are campers
                sortingByDistance(Engine.getClosestMyCapsule(myPirates.get(0)));
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
            // 3 - 5
            case BASICV2:
                sortingByDistance(Engine.getClosestMyCapsule(myPirates.get(0)));
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
            // 4-4
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

            case OneManArmy:
                pirates.get(0).setJob(Job.OneManArmy);
                //game.debug("OneManArmy");
                pirates.get(0).work();
                break;

            case COOL:
                pirates.get(0).setJob(Job.COOL);
                //game.debug("YOU_SHALL_NOT_PASS");
                pirates.get(0).work();
                break;

            // 8-0
            default:
                for (BotPirate p : pirates) {
                    p.work();
                }
                break;
        }

    }
}