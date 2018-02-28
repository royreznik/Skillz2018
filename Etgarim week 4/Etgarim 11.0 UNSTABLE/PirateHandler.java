package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import pirates.Asteroid;
import pirates.Capsule;
import pirates.Location;
import pirates.MapObject;
import pirates.Mothership;
import pirates.Pirate;
import pirates.PirateGame;

//Class that represent the Strategy
public class PirateHandler {
    private static final PirateGame game;
    private static final int CAMPER_RANGE = 1299;
    public static List<BotPirate> pirates;
    public static boolean swapedTrun = false;
    private static Map<Job, List<Pirate>> roles = null;
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

    public static boolean isShuffleNeeded(){
        List<Pirate> campers = PirateHandler.roles.get(Job.CAMPER);
        if(campers.size() < 4) return true;
        int campersOnRadius = 0;
        for(Pirate camper : campers){
            if(camper.distance(Engine.getClosestEnemyMothership(camper)) < 2000) campersOnRadius++;
        }
        return campersOnRadius < 4;
    }

    // Tactic Maker
    // Represent as Job like: Capsuler - Camper - AntiCamper and etc'
    // 4 - 4 means that there is 4 Capulsers and 4 Campers
    public void doWork() {

        switch (tactic) {
        
            case SPLIT2:
                PirateHandler.pirates = new ArrayList<>();
                if(PirateHandler.roles != null){
                    for(Job job : PirateHandler.roles.keySet()){ //remove all dead pirates
                        List<Pirate> actors = PirateHandler.roles.get(job), livingActors = new ArrayList<>();
                        for(Pirate actor : actors){
                            if(actor.isAlive()){
                                if(actor.stickyBombs.length == 0) livingActors.add(actor);
                                else {
                                    actor.sail(Engine.getBombingLocation(actor.location, actor.maxSpeed * actor.stickyBombs[0].countdown));
                                }
                            }
                        }
                        PirateHandler.roles.put(job, livingActors);
                    }
                    
                }
                if(PirateHandler.roles == null || PirateHandler.isShuffleNeeded()){ //initialize roles at the start of game or if shuffle is needed
                    PirateHandler.roles = new HashMap<>();
                    PirateHandler.roles.put(Job.CAMPER, new ArrayList<Pirate>());
                    PirateHandler.roles.put(Job.CAPSULER, new ArrayList<Pirate>());
                    PirateHandler.roles.put(Job.ANTICAMPER, new ArrayList<Pirate>());
                    PirateHandler.roles.put(Job.ASTRO, new ArrayList<Pirate>());
                }
                
                List<Pirate> capsulers = PirateHandler.roles.get(Job.CAPSULER);
                List<Pirate> antiCampers = PirateHandler.roles.get(Job.ANTICAMPER);
                List<Pirate> campers = PirateHandler.roles.get(Job.CAMPER);
                List<Pirate> astro = PirateHandler.roles.get(Job.ASTRO);
                if(capsulers.isEmpty()){ //if no capsulers
                    if(antiCampers.isEmpty()){  //first try to take anti campers
                        if(campers.size() <= 4){ //then one campers if there are over 4
                            if(!astro.isEmpty()){ //then astro
                                PirateHandler.roles.put(Job.ASTRO, new ArrayList<>());
                                capsulers.addAll(astro);
                            }
                        } else {
                            Pirate removed = campers.remove(campers.size() - 1);
                            capsulers.add(removed);
                        }
                    } else {
                        capsulers.add(antiCampers.remove(antiCampers.size() - 1));
                    }
                }
                capsulers = PirateHandler.roles.get(Job.CAPSULER);
                antiCampers = PirateHandler.roles.get(Job.ANTICAMPER);
                campers = PirateHandler.roles.get(Job.CAMPER);
                astro = PirateHandler.roles.get(Job.ASTRO);
                if(antiCampers.isEmpty() && !capsulers.isEmpty()){
                   if(campers.size() > 4){
                       antiCampers.add(campers.remove(campers.size() - 1));
                   }
                   else if(capsulers.size() > 1){
                       for(int i = capsulers.size() - 1; i >= 0; i--){
                           if(!capsulers.get(i).hasCapsule()){
                               antiCampers.add(capsulers.remove(i));
                               break;
                           }
                       }
                   }
                }
                
                List<Pirate> availablePirates = new ArrayList<>(), livingPirates = Arrays.asList(game.getMyLivingPirates());
                List<Pirate> assigned = new ArrayList<>();
                for(List<Pirate> assginedJobs : PirateHandler.roles.values()){
                    assigned.addAll(assginedJobs);
                }
                for(Pirate livingPirate : livingPirates){ //add all living pirates that don't have roles
                    if(!assigned.contains(livingPirate)){
                        availablePirates.add(livingPirate);
                    }
                }
                int numCapsulers = PirateHandler.roles.get(Job.CAPSULER).size(), numCapsules = game.getMyCapsules().length - numCapsulers, maxCampers = 0;
                int numAsteroids = game.getAllAsteroids().length;
                int numMotherships = game.getEnemyMotherships().length, numEnemyCapsules = game.getEnemyCapsules().length;
                int numCampers = PirateHandler.roles.get(Job.CAMPER).size(), numAntiCampers = PirateHandler.roles.get(Job.ANTICAMPER).size();
                boolean hasAstro = PirateHandler.roles.get(Job.CAPSULER).size() > 0;
                if(numMotherships != 0 && numEnemyCapsules != 0){
                    maxCampers = game.getMyLivingPirates().length - numCapsules; //acounting for capsulers
                    if(maxCampers > 4){ 
                        maxCampers-= numCapsules; //accounting for antcampers NOTE-no anti campers used now because they are bad
                        if(maxCampers < 4) maxCampers = 4;
                        if(numAsteroids > 0 && maxCampers > 4) maxCampers-= 1; //accounting for astro
                        
                    }
                }
                
                List<Pirate> bombers = new ArrayList<>();
                for(Pirate p : availablePirates){
                    if(p.stickyBombs.length > 0){
                        if(p.hasCapsule()){
                            if(Engine.getClosestMyMothership(p).inRange(p, p.maxSpeed * p.stickyBombs[0].countdown)){
                                p.sail(Engine.getClosestMyMothership(p));
                            }
                        }
                        else p.sail(Engine.getBombingLocation(p.location, p.maxSpeed * p.stickyBombs[0].countdown));
                        bombers.add(p);
                    }
                }
                availablePirates.removeAll(bombers);
                while(!availablePirates.isEmpty()){
                    Comparator<Pirate> sorter = null, roleSorter = null;
                    Job job = Job.CAMPER;
                    if(numCapsules > 0){ //sorting according to distance to capsule
                        sorter = Comparator.comparing(p -> p.hasCapsule() ? 0 : 1);
                        sorter = sorter.thenComparing(p -> p.distance(Engine.getClosestAvailableMyCapsule(p, BotPirate.unAvailableCapsules)));
                        roleSorter = sorter;
                        job = Job.CAPSULER;
                        numCapsulers++;
                        numCapsules--;
                    }
                    else if(numCampers != maxCampers){ //sorting according to distance and push reload turns
                        List<Mothership> enemyMotherShips = Arrays.asList(game.getEnemyMotherships());
                        List<Capsule> enemyCapsules = Arrays.asList(game.getEnemyCapsules());
                        Mothership enemyBase = Engine.getBestEnemyBase(enemyMotherShips, enemyCapsules).get(0);
                        // already campers should sort according to push reload turns
                        roleSorter = Comparator.comparing(p -> p.pushReloadTurns);
                        roleSorter = roleSorter.thenComparing(enemyBase::distance);
                        // non campers sort by distance
                        sorter = Comparator.comparing(enemyBase::distance);
                        sorter = sorter.thenComparing(p -> p.pushReloadTurns);
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
                    else{ //this is in case of no motherships
                        sorter = Comparator.comparing(p -> p.hasCapsule() ? 0 : 1);
                        sorter = sorter.thenComparing(p -> p.distance(Engine.getClosestAvailableMyCapsule(p, BotPirate.unAvailableCapsules)));
                        roleSorter = sorter;
                        job = Job.CAPSULER;
                        numCapsulers++;
                        numCapsules--;
                    }
                    if(sorter != null){
                        availablePirates = availablePirates.stream().sorted(sorter).collect(Collectors.toList());
                    }
                    Pirate bestPirate = availablePirates.get(0);
                    List<Pirate> actors = PirateHandler.roles.get(job);
                    actors.add(bestPirate);
                    if(roleSorter != null){
                        Collections.sort(actors, roleSorter);
                    }
                    PirateHandler.roles.put(job, actors);
                    availablePirates.remove(bestPirate);
                }
                
                Job[] jobs = new Job[] {Job.CAPSULER, Job.CAMPER, Job.ANTICAMPER, Job.ASTRO};
                for(Job job : jobs){
                    List<Pirate> actors = PirateHandler.roles.get(job);
                    for(Pirate actor : actors){
                        BotPirate bp = new BotPirate(actor);
                        bp.setJob(job);
                        PirateHandler.pirates.add(bp);
                    }
                }
                PirateHandler.pirates.forEach(BotPirate::work);
                break;
                
            case SPLIT:
                PirateHandler.pirates = new ArrayList<>();
                availablePirates = new ArrayList<>();
                availablePirates.addAll(Arrays.asList(game.getMyLivingPirates())); //doing this to be able to change list
                numCapsules = game.getMyCapsules().length;
                numCapsulers = 0;
                maxCampers = 0;
                numAsteroids = game.getAllAsteroids().length;
                numMotherships = game.getEnemyMotherships().length;
                numEnemyCapsules = game.getEnemyCapsules().length;
                numCampers = 0;
                numAntiCampers = 0;
                hasAstro = false;
                if(numMotherships != 0 && numEnemyCapsules != 0){
                    maxCampers = game.getMyLivingPirates().length - numCapsules; //acounting for capsulers
                    if(maxCampers > 4){ 
                        maxCampers-= numCapsules; //accounting for antcampers NOTE-no anti campers used now because they are bad
                        if(maxCampers < 4) maxCampers = 4;
                        if(numAsteroids > 0 && maxCampers > 4) maxCampers-= 1; //accounting for astro
                        
                    }
                }
                
                capsulers = Arrays.asList(game.getMyLivingPirates()).stream().filter(Pirate::hasCapsule).collect(Collectors.toList());
                for(Pirate capsuler : capsulers){ //setting all pirates with capsule to capsulers
                    BotPirate bp = new BotPirate(capsuler);
                    bp.setJob(Job.CAPSULER);
                    PirateHandler.pirates.add(bp);
                    numCapsules--;
                    numCapsulers++;
                }
                availablePirates.removeAll(capsulers);
                
                bombers = new ArrayList<>();
                for(Pirate p : availablePirates){
                    if(p.stickyBombs.length > 0){
                        if(p.hasCapsule()){
                            if(Engine.getClosestMyMothership(p).inRange(p, p.maxSpeed * p.stickyBombs[0].countdown)){
                                p.sail(Engine.getClosestMyMothership(p));
                            }
                        }
                        else p.sail(Engine.getBombingLocation(p.location, p.maxSpeed * p.stickyBombs[0].countdown));
                        bombers.add(p);
                    }
                }
                
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