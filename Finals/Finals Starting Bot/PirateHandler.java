package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import pirates.Capsule;
import pirates.MapObject;
import pirates.Mothership;
import pirates.Pirate;
import pirates.PirateGame;
import pirates.Location;
import pirates.Asteroid;

//Class that represent the Strategy
public class PirateHandler {
    private static final PirateGame game;
    private static final int CAMPER_RANGE = 1299;
    public static List<BotPirate> pirates;
    public static boolean swapedTrun = false;
    public static boolean bombed = false;
    private static Map<Job, List<Pirate>> roles = null;
    
    static {
        game = MyBot.gameInstance;
    }
    
    Tactic tactic;
    DefenseHandler defense;

    public static int numOfCapsulers = -1;

    public PirateHandler() {
        PirateHandler.pirates = new ArrayList<BotPirate>();
        for (Pirate p : game.getAllMyPirates()) {
            pirates.add(new BotPirate(p));
        }
        tactic = Tactic.SPLIT;
        defense = new DefenseHandler();
        swapedTrun= false;
        bombed = false;
    }

    public void setTactic(Tactic t) {
        this.tactic = t;
    }
    
    public static void setBombed(boolean b)
    {
        bombed =b;
    }
    
    public static boolean getBombed()
    {
        return bombed;
    }

    public static void setSwap(boolean b)
    {
        swapedTrun = b;
    }
    public static boolean getSwap()
    {
        return PirateHandler.swapedTrun;
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

        switch (tactic) {
        
            case SPLIT:
                PirateHandler.pirates = new ArrayList<>();
                List<Pirate> availablePirates = new ArrayList<>();
                availablePirates.addAll(Arrays.asList(game.getMyLivingPirates())); //doing this to be able to change list
                int numCapsules = game.getMyCapsules().length, numCapsulers = 0, maxCampers = 0;
                int numAsteroids = game.getAllAsteroids().length;
                int numMotherships = game.getEnemyMotherships().length, numEnemyCapsules = game.getEnemyCapsules().length;
                int numCampers = 0, numAntiCampers = 0;
                boolean hasAstro = false;
                
                for(Pirate p : game.getMyLivingPirates()){
                    if(p.stickyBombs.length > 0){
                        if(p.hasCapsule()){
                            if(Engine.getClosestMyMothership(p).inRange(p, p.maxSpeed * p.stickyBombs[0].countdown)){
                                p.sail(Engine.getClosestMyMothership(p));
                            }
                        }
                        else p.sail(Engine.getBombingLocation(p.location, p.maxSpeed * p.stickyBombs[0].countdown));
                        availablePirates.remove(p);
                    }
                }
                
                List<Pirate> capsulers = Arrays.asList(game.getMyLivingPirates()).stream().filter(Pirate::hasCapsule).collect(Collectors.toList());
                for(Pirate capsuler : capsulers){ //setting all pirates with capsule to capsulers
                    BotPirate bp = new BotPirate(capsuler);
                    bp.setJob(Job.CAPSULER);
                    PirateHandler.pirates.add(bp);
                    numCapsules--;
                    numCapsulers++;
                }
                availablePirates.removeAll(capsulers);
                
                if(numMotherships != 0 && numEnemyCapsules != 0){
                    maxCampers = game.getMyLivingPirates().length - numCapsules; //acounting for capsulers
                    if(maxCampers > 4){ 
                        maxCampers-= (numCapsules); //accounting for antcampers NOTE-no anti campers used now because they are bad
                        if(maxCampers < 4) maxCampers = 4;
                        if(numAsteroids > 0 && maxCampers > 4) maxCampers-= 1; //accounting for astro
                        
                    }
                }
                
                while(!availablePirates.isEmpty()){
                    Comparator<Pirate> sorter = null;
                    Job job = Job.CAMPER;
                    if(numCapsules > 0){ //sorting according to distance to capsule
                        sorter = Comparator.comparing(p -> p.distance(Engine.getClosestAvailableMyCapsule(p, BotPirate.unAvailableCapsules)));
                        job = Job.CAPSULER;
                        numCapsulers++;
                        numCapsules--;
                    }
                    else if(numCampers != maxCampers){ //sorting according to distance and psh reload turns
                        List<Mothership> enemyMotherShips = Arrays.asList(game.getEnemyMotherships());
                        List<Capsule> enemyCapsules = Arrays.asList(game.getEnemyCapsules());
                        Mothership enemyBase = Engine.getBestEnemyBase(enemyMotherShips, enemyCapsules).get(0);
                        List<Pirate> alreadyCampers = Arrays.asList(game.getMyLivingPirates()).stream().filter(p -> p.inRange(enemyBase, 1299)).collect(Collectors.toList());
                        if(!alreadyCampers.isEmpty()){ //if there are already campers sort according to push reload turns
                            sorter = Comparator.comparing(p -> alreadyCampers.contains(p) ? 0 : 1);
                            sorter = sorter.thenComparing(p -> p.pushReloadTurns);
                            sorter = sorter.thenComparing(enemyBase::distance);
                        }
                        else { //if not then by distance
                            sorter = Comparator.comparing(enemyBase::distance);
                            sorter = sorter.thenComparing(p -> p.pushReloadTurns);
                        }
                        job = Job.CAMPER;
                        numCampers++;
                    }
                    else if(numAntiCampers != numCapsulers){ //sorting according to distance to capsulers
                        sorter = Comparator.comparing(p -> p.distance(Engine.getClosestMyCapsuler(p)));
                        job = Job.ANTICAMPER;
                        numAntiCampers++;
                    }
                    else if(numAsteroids > 0 && !hasAstro){ //no sorting
                        job = Job.ASTRO;
                        hasAstro = true;
                        
                    }
                    else{ //this is incase of no motherships
                       
                        sorter = Comparator.comparing(p -> p.distance(Engine.getClosestAvailableMyCapsule(p, BotPirate.unAvailableCapsules)));
                        job = Job.CAPSULER;
                        numCapsulers++;
                        numCapsules--;
                    }
                    if(sorter != null){
                        availablePirates = availablePirates.stream().sorted(sorter).collect(Collectors.toList());
                    }
                    Pirate bestPirate = availablePirates.get(0);
                    availablePirates.remove(bestPirate);
                    BotPirate bestBot = new BotPirate(bestPirate);
                    bestBot.setJob(job);
                    PirateHandler.pirates.add(bestBot);
                }
                PirateHandler.pirates.stream().forEachOrdered(BotPirate::work);
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