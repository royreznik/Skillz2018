package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                if(numMotherships != 0 && numEnemyCapsules != 0){
                    maxCampers = game.getMyLivingPirates().length - numCapsules; //acounting for capsulers
                    if(maxCampers > 4){ 
                        //maxCampers-= numCapsules; //accounting for antcampers NOTE-no anti campers used now because they are bad
                        if(maxCampers < 4) maxCampers = 4;
                        if(numAsteroids > 0 && maxCampers > 4) maxCampers-= 1; //accounting for astro
                        
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
                while(!availablePirates.isEmpty()){
                    Comparator<Pirate> sorter = null;
                    Job job = Job.CAMPER;
                    if(numCapsules > 0){ //sorting according to distance to capsule
                        /* doesnt work well
                        if(numCapsulers > 0){ 
                            sorter = Comparator.comparing(p -> p.inRange(enemyBase, CAMPER_RANGE) ? 0 : 1);
                            sorter = sorter.thenComparing(p -> p.distance(Engine.getClosestAvailableMyCapsule(p, BotPirate.unAvailableCapsules)));
                        } else {
                        */
                        sorter = Comparator.comparing(p -> p.distance(Engine.getClosestAvailableMyCapsule(p, BotPirate.unAvailableCapsules)));
                        //}
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
                    /* ------------------------------ currently commented out because anti camper is bad
                    else if(numAntiCampers != numCapsulers){ //sorting according to distance to capsulers
                        sorter = Comparator.comparing(p -> p.distance(Engine.getClosestMyCapsuler(p)));
                        job = Job.ANTICAMPER;
                        numAntiCampers++;
                    }--------------------------------
                    */ 
                    else if(numAsteroids > 0 && !hasAstro){ //no sorting
                        job = Job.ASTRO;
                        hasAstro = true;
                        
                    }
                    else{ //this is incase of no motherships
                       
                        sorter = Comparator.comparing(p -> p.distance(Engine.getClosestAvailableMyCapsule(p, BotPirate.unAvailableCapsules)));
                        //}
                        job = Job.CAPSULER;
                        numCapsulers++;
                        numCapsules--;
                    }
                    if(sorter != null){
                        availablePirates = availablePirates.stream().sorted(sorter).collect(Collectors.toList());
                    }
                    Pirate bestPirate = availablePirates.get(0);
                    //if(job.equals(Job.CAMPER) && numCapsulers > 0 && bestPirate.inRange(enemyBase, CAMPER_RANGE)) continue; //DOESNT work wellif we dont want to take campers but all are campers
                    availablePirates.remove(bestPirate);
                    BotPirate bestBot = new BotPirate(bestPirate);
                    bestBot.setJob(job);
                    PirateHandler.pirates.add(bestBot);
                }
                PirateHandler.pirates.stream().forEachOrdered(BotPirate::work);
                break;
                
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
            case SPAGHT:
                pirates.get(0).setJob(Job.SPAGHT);
                game.debug("spaghettification");
                pirates.get(0).work();
                break;
            case STATEMACHINE:
                List<Pirate> pis = Arrays.asList(game.getMyLivingPirates());
                if(!pis.get(1).hasCapsule())
                {
                    if(pis.get(1).stateName.equals("heavy"))
                    {
                        pis.get(1).swapStates(pis.get(0));
                    }
                    else{
                        if(pis.get(0).getLocation().equals(pis.get(1).getLocation()))
                        {
                            pis.get(0).push(pis.get(1), game.getMyCapsules()[0]);
                            pis.get(1).push(pis.get(0), game.getMyCapsules()[0]);
                        }
                        else{
                            pis.get(0).sail(new Location(2750,3800));
                            pis.get(1).sail(new Location(2750,3800));
                        }
                    }
                    
                }
                else{
                    if(pis.get(1).stateName.equals("normal"))
                    {
                        pis.get(1).swapStates(pis.get(0));
                    }
                    else{
                        pis.get(1).sail(game.getMyMotherships()[0]);
                    }
                }
                
                game.debug("StateMachine");
                break;
                
            case SPACERACE:
                BotPirate helper = null, capsuler = null;
                Capsule capsule = game.getMyCapsules()[0];
                for(int index = 0; index < 3; index++){
                    Pirate pirate = game.getMyLivingPirates()[index];
                    BotPirate bp = new BotPirate(pirate);
                    if(capsule.holder == null){
                        bp.setDestination(capsule);
                        bp.sailToDest();
                    }
                    else if(pirate.hasCapsule()){
                        if(pirate.stateName.equals("heavy")){
                            int x = (index == 0 ? 1 : 0);
                            Pirate swpper = game.getMyLivingPirates()[x];
                            pirate.swapStates(swpper);
                        }
                        else{
                            bp.setDestination(game.getMyMotherships()[0]);
                            bp.sailToDest();
                        }
                    }
                    else {
                        bp.tryPushPirate(capsule.holder, game.getMyMotherships()[0]);
                        bp.setDestination(capsule);
                        bp.sailToDest();
                    }
                }
                break;
                
            case HEAVYLIFTING:
                helper = null;
                capsuler = null;
                capsule = game.getMyCapsules()[0];
                for(Pirate pirate : game.getMyLivingPirates()){
                    BotPirate bp = new BotPirate(pirate);
                    Location loc = game.getMyMotherships()[0].location.towards(capsule, 1299);
                    if(pirate.stateName.equals("heavy")){
                        if(!pirate.location.equals(loc)){
                            bp.setDestination(loc);
                            bp.sailToDest();
                        }
                        else if(capsule.holder != null && capsule.location.equals(loc) && pirate.canPush(capsule.holder)){
                                bp.tryPushPirate(capsule.holder, game.getMyMotherships()[0]);
                            }
                        }
                    else if(capsule.holder == null){
                        bp.setDestination(capsule);
                        bp.sailToDest();
                    }
                    else if(pirate.hasCapsule()){
                        bp.setDestination(loc);
                        bp.sailToDest();
                    }
                    else{
                        bp.setJob(Job.CAMPER);
                        bp.work();
                    }
                }
                break; 
                
           case RAINFROMHELL:
                Location astroLoc = game.getAllAsteroids()[0].initialLocation.towards(game.getAllAsteroids()[1].initialLocation, 600);
                if(game.getAllAsteroids()[0].inRange(Engine.getClosestEnemyMothership(game.getAllAsteroids()[0]), 600)) astroLoc = game.getAllAsteroids()[1].initialLocation.towards(game.getAllAsteroids()[0].initialLocation, 600);
                List<Pirate> available = new ArrayList<>();
                available.addAll(Arrays.asList(game.getMyLivingPirates())); 
                Comparator<Pirate> sorter = Comparator.comparing(astroLoc::distance);
                Pirate astroP = available.stream().min(sorter).get();
                available.remove(astroP);
                if(astroP.location.equals(astroLoc)){
                    Pirate capsulerBomb = Engine.getClosestEnemyCapsuler(astroP);
                    if(game.getMyself().turnsToStickyBomb == 0 && astroP.inStickBombRange(capsulerBomb)){
                        astroP.stickBomb(capsulerBomb);
                    }
                    else {
                        for(Pirate camp : available){
                            Mothership dest = Engine.getClosestEnemyMothership(camp);
                            Location loc = dest.location.towards(Engine.getClosestLivingAsteroid(dest), 300);
                            if(loc.col < 2000) loc = loc.add(new Location(0, 500));
                            else loc = loc.subtract(new Location(0, 500));
                            if(camp.location.equals(loc)){
                                Asteroid a = Engine.getClosestLivingAsteroid(dest);
                                if(astroP.canPush(a)){
                                    astroP.push(a, dest);
                                    break;
                                }
                            }
                        }
                        
                    }
                }
                else astroP.sail(astroLoc);
                for(Pirate camp : available){
                    Mothership dest = Engine.getClosestEnemyMothership(camp);
                    Asteroid a = Engine.getClosestLivingAsteroid(dest);
                    Location loc = dest.location.towards(a, 300);
                    if(loc.col < 2000) loc = loc.add(new Location(0, 500));
                    else loc = loc.subtract(new Location(0, 500));
                    if(camp.location.inRange(loc, 40)){
                        if(a.inRange(dest, 600) && camp.canPush(a)) camp.push(a, a.location);
                    }
                    else {
                        camp.sail(loc);
                    }
                }
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