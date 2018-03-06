package bots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import pirates.Asteroid;
import pirates.Capsule;
import pirates.Location;
import pirates.MapObject;
import pirates.Mothership;
import pirates.Pirate;
import pirates.PirateGame;
import pirates.Wormhole;

import pirates.*;

//Class that represent a single Pirate
public class BotPirate {
    // INIT the game Variable
    private static final PirateGame game;
    public static HashSet<Wormhole> assignedHoles;
    public static HashSet<Capsule> unAvailableCapsules;
    private static Map<Capsule, Integer> capsuleDrops;

    private static int capsulerCounter = -1;
    private static int camperCounter = -1;
    private static int astroCounter = -1;
    private static boolean moveAsteroid = false;
    private static int trySomething = 0;

    static {
        game = MyBot.gameInstance;

    }

    Pirate pirate;
    MapObject destination;
    Job job;
    boolean didPush;
    Location nextStep;

    // Constructor
    public BotPirate(Pirate pirate) {
        this.pirate = pirate;
        this.destination = null;
        this.job = Job.CAPSULER;
        this.didPush = false;
    }

    public BotPirate(BotPirate pirate) {
        this.pirate = pirate.pirate;
        this.destination = pirate.destination;
        this.job = pirate.job;
        this.didPush = pirate.didPush;
    }

    public static void startVariables() {
        assignedHoles = new HashSet<>();
        unAvailableCapsules = new HashSet<>();
        capsulerCounter = -1;
        camperCounter = -1;
        astroCounter = -1;
        moveAsteroid = false;
    }

    public String toString() {
        return "ID: ";
    }

    public void setJobNDest(BotPirate bp) {
        this.destination = bp.destination;
        this.job = bp.job;
    }

    // Getters & Setters
    public Pirate getPirate() {
        return pirate;
    }

    public void setPirate(Pirate pirate) {
        this.pirate = pirate;
    }

    public MapObject getDestination() {
        return destination;
    }

    public void setDestination(MapObject destination) {
        this.destination = destination;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public boolean isDidPush() {
        return didPush;
    }

    public void setDidPush(boolean didPush) {
        this.didPush = didPush;
    }
    
    public Location getNextStep(){
        if(this.nextStep != null) return nextStep;
        if(destination != null) return this.pirate.location.towards(destination, this.pirate.maxSpeed);
        if(job.equals(Job.CAPSULER)){
            MapObject m;
            if(this.pirate.hasCapsule()){
                m = Engine.getBestCapsulerMothership(this.pirate).location;
            }
            else m = Engine.getClosestAvailableMyCapsule(this.pirate, BotPirate.unAvailableCapsules).location;
            return this.pirate.location.towards(m, this.pirate.maxSpeed);
            
        }
        return null;
    }    

    /**
     * @param p   - AnyPirate
     * @param obj - AnyMapObject
     *            <p>
     *            This methods will try to push p in the obj location
     */
    public void tryPushPirate(Pirate p, MapObject obj) {
        if (p != null && p.isAlive() && !this.didPush && this.pirate.canPush(p))  {
            // Push the enemy!
            this.pirate.push(p, obj.getLocation());
            // Did push.
            this.didPush = true;
        }
    }

    public void tryPushAsteroid(Asteroid a, MapObject obj) {
        if (a != null && a.isAlive() && !this.didPush && pirate.canPush(a) && a.turnsToRevive == 0 && game.pushDistance > 0) {
            // Push the enemy!
            //game.debug("pushAsteroidTo " + obj.getLocation());
            pirate.push(a, obj.getLocation());
            // Did push.
            didPush = true;
            PirateNavigator.alreadyPushed.add(a);

            if (job.equals(Job.CAMPER))
                moveAsteroid = true;
        }
    }

    public void tryPushWormhole(Wormhole w, MapObject obj) {
        if (w != null && !this.didPush && pirate.canPush(w)) {
            // Push the Wormhole!
            //game.debug("Pushing Wormhole to " + obj.getLocation());
            pirate.push(w, obj.getLocation());
            // Did push.ca
            didPush = true;
            if (job.equals(Job.CAMPER)) assignedHoles.add(w);
        }
    }

    public void tryStickyBomb(Pirate p) {
        if (game.getMyself().turnsToStickyBomb < 1 && this.pirate.inStickBombRange(p) && !didPush) {
            this.pirate.stickBomb(p);
            didPush = true;
        }
    }



    // Sail to the Destination
    public void sailToDest() {
        if (!pirate.isAlive()) {
            this.job = Job.DEAD;
            return;
        }
        if (this.didPush) {
            //game.debug("Already push");
            return;
        }
        if (game.getLivingAsteroids().length > 0) {
//			if (Engine.areThereCampers()) {
//				tryPushAsteroid(game.getLivingAsteroids()[0], game.getMyMotherships()[0]);
//			}
//			tryPushAsteroid(game.getLivingAsteroids()[0], game.getEnemyCapsules()[0]);
            tryPushAsteroid(Engine.getClosestAsteroid(this.pirate), Engine.pushAsteroidTo(Engine.getClosestAsteroid(this.pirate)));
            if (didPush) {
                return;
            }
        }
        if (this.destination == null) {
            //game.debug("No Dest");
            return;
        }
        if (this.destination.getLocation().row < 0 || this.destination.getLocation().row > game.rows
                || this.destination.getLocation().col < 0 || this.destination.getLocation().col > game.cols) {
            //game.debug("This dest Invalid");
            return;
        }
        // should add: if (pirate.getLocation() != destination) ???
        //pirate.sail(destination);
        PirateNavigator.navigate(this);

    }

    /**
     * ~ DO NOT USE ~
     * Method is written in class BotPirate because it is going to be used in
     * Anti-Camper Strategy
     *
     * @return the location of the nearest wall/border to the pirate. Example - If
     * the method runs on a Pirate at (100,4600) the method will return
     * (0,4600) - a straight line towards the nearest wall.
     */
    public Location nearestWall() {
        int maxRows = game.rows;
        int maxCols = game.cols;

        int row = this.pirate.location.row;
        int col = this.pirate.location.col;

        if (row < (maxRows / 2.0)) {
            if (col < (maxCols / 2.0)) {
                if (col < row)
                    return new Location(row, -1);
                return new Location(-1, col);
            }
            if (row < (maxCols - col))
                return new Location(-1, col);
            return new Location(row, maxCols + 1);
        }
        if (col < (maxCols / 2.0)) {
            if (col < (maxRows - row))
                return new Location(row, -1);
            return new Location(maxRows + 1, col);
        }

        if ((maxCols - col) < (maxRows - row))
            return new Location(row, maxCols + 1);
        return new Location(maxRows + 1, col);
    }
    
    public void tryCapsulerSwapStates(){
        if (this.pirate.stateName.equals("heavy") && !this.pirate.hasCapsule() && game.turn > 10) {
            List<Pirate> normals = Engine.getNormalPirates();
            if (normals.size() > 0) {
                if (game.getEnemyMotherships().length > 0) {
                    Collections.sort(normals, (e1, e2) -> e1.distance(game.getEnemyMotherships()[0]) - e2.distance(game.getEnemyMotherships()[0]));
                }
                if (!PirateHandler.getSwap()) {
                    this.pirate.swapStates(normals.get(0));
                    PirateHandler.setSwap(true);
                    return;
                }
            }
        }
        

        if (this.pirate.stateName.equals("normal") && this.pirate.hasCapsule()) {
            if (game.getMyMotherships().length > 0) {
                if (pirate.distance(game.getMyMotherships()[0]) < 1500) {

                    List<Pirate> normals = Engine.getHeavyPirates();
                    if (normals.size() > 0) {
                        if (game.getEnemyMotherships().length > 0) {
                            Collections.sort(normals, (e1, e2) -> e1.distance(game.getEnemyMotherships()[0]) - e2.distance(game.getEnemyMotherships()[0]));
                        }
                        if (!PirateHandler.getSwap()) {
                            if (!normals.get(0).hasCapsule()) {
                                boolean isClose = false;
                                for (Pirate p : game.getEnemyLivingPirates()) {
                                    if (p.distance(pirate) < 302) {
                                        isClose = true;
                                    }
                                }
                                if (isClose) {
                                    this.pirate.swapStates(normals.get(0));
                                    PirateHandler.setSwap(true);
                                    return;
                                }

                            }
                        }
                    }
                }
            }

        }
    }
    
    public boolean camperTryPushTarget(Pirate target){
        Pirate shahid = Engine.getClosestShahid(this.pirate);
        Asteroid asteroid = Engine.getClosestLivingAsteroid(target);
        if (asteroid != null &&
                (target.distance(asteroid) - asteroid.size <= 600 ||
                        (shahid != null && shahid.distance(asteroid) - asteroid.size <= 600))) {
            tryPushPirate(target, asteroid);
            tryPushPirate(shahid, asteroid);
            tryPushAsteroid(asteroid, shahid);
            tryPushAsteroid(asteroid, target);
        } else if (target.inRange(Engine.nearestWall(target), this.pirate.pushDistance)
                || (shahid != null && shahid.inRange(Engine.nearestWall(shahid), this.pirate.pushDistance))) {
            if (!target.hasCapsule() && shahid != null && shahid.inRange(Engine.nearestWall(shahid), this.pirate.pushDistance))
                tryPushPirate(shahid, Engine.nearestWall(shahid));
            else
                tryPushPirate(target, Engine.nearestWall(target));
        } else {

            shahid = Engine.getClosestShahid(this.pirate);

            if (shahid != null && Engine.willPushTeleport(this.pirate, shahid, Engine.getClosestWormhole(shahid)))
                tryPushPirate(shahid, Engine.getClosestWormhole(shahid));

            else if (Engine.friendNoMore(this.pirate) != null) {

                Pirate ex = Engine.friendNoMore(this.pirate);
                Pirate exCap = Engine.getClosestEnemyCapsuler(ex);

                if (ex != null && exCap != null
                        && Engine.boomIn(ex) <
                        Engine.minTurnsToDest(exCap, Engine.getClosestEnemyMothership(exCap))
                        && Engine.friendsInRange(exCap, Engine.boomIn(ex)) < 2)
                    tryPushPirate(ex, exCap);
                else if (shahid != null)
                    if (((shahid.stickyBombs.length > 0 && Engine.friendsInRange(shahid,
                            Engine.firstBombToBlowUp(shahid).explosionRange) > 0) || shahid.stickyBombs.length == 0)
                            && Engine.willPushToWallKill(this.pirate, shahid))
                        tryPushPirate(shahid, Engine.nearestWall(shahid));
            }
        }
        return this.didPush;
    }
    
    public boolean tryCloseWormholePush(){
        for(Wormhole w : game.getAllWormholes()){
            if (w != null && game.getMyMotherships().length > 0) {
                if (!didPush && w.distance(Engine.getClosestMyMothership(w)) < w.partner.distance(Engine.getClosestMyMothership(w.partner))) {
                    tryPushWormhole(w, Engine.getClosestMyMothership(w));
                }
            } else if (w != null && !didPush) {
                tryPushWormhole(w, Engine.getClosestMyCapsule(this.pirate));
            }
        }
        return this.didPush;
    }
    
    /**
     * get the best enemy capsuler to defend aginst, currently is just the closest one to pirate
     */
    public Pirate getCapsulerToDefend(Mothership base){
        Pirate capsuler = Engine.getClosestEnemyCapsuler(this.pirate);
        if(!capsuler.hasCapsule() || true) return capsuler;
        Pirate otherCapsuler = Engine.getClosestEnemyCapsuler(base);
        int turnsToBase = Engine.intTurnsTo(otherCapsuler, base);
        int turnsToOther = Engine.intTurnsTo(this.pirate, otherCapsuler);
        if(turnsToOther < turnsToBase) return otherCapsuler;
        return capsuler;
    }

    /**
     * returns the best target for this camper to push
     *
     * @return pirate to push
     */
    public Pirate getEnemyTarget() {
        Capsule[] capsuleArr = BotPirate.capsuleDrops.keySet().toArray(new Capsule[capsuleDrops.size()]);
        List<Capsule> capsules = Arrays.asList(capsuleArr);
        Comparator<Capsule> sorter = Comparator.comparing(c -> c.distance(Engine.getClosestEnemyMothership(c)));
        Collections.sort(capsules, sorter);
        for (Capsule capsule : capsules) {
            if (!this.pirate.canPush(capsule.holder) || BotPirate.capsuleDrops.get(capsule) == 0) continue;
            return capsule.holder;
        }
        return null;
    }
    
    /**
     * current wormhole defence copied for camper logic V2
     */
    public void tryDefendWormhole(Pirate enemyCapsuler){
        Mothership enemyMom = Engine.getClosestEnemyMothership(this.pirate);
        Wormhole w = Engine.getClosestWormhole(enemyMom);
        if (!didPush && game.getAllWormholes().length > 0 && this.pirate.distance(w) < 800) {
//            Wormhole w = Engine.getClosestEnemyWormhole(this.pirate);
//            Mothership enemyMom = Engine.getClosestEnemyMothership(this.pirate);
            //game.debug("Wormhole distance from enemy mothership = " + w.distance(enemyMom));
//            if (enemyMom != null && w != null
////                    && this.pirate.distance(enemyMom) > 700
////                    && (((enemyMom.distance(w)) - game.wormholeRange) - game.mothershipUnloadRange) <= 600) {
//                    && enemyMom.distance(w) <= 1200) {
//                destination = w.getLocation().towards(this.pirate, game.wormholeRange + 1);
            //game.debug("dest = " + destination);
            Mothership partnerMothership = Engine.getClosestAnyMothership(w.partner);
            //game.debug("Partnermothership id = " + partnerMothership.owner.id + " My id = " + game.getMyself().id);
            if (w.isActive && Engine.getClosestEnemy(w).distance(w) - game.wormholeRange <= 600)
                tryPushWormhole(w, Engine.getClosestEnemy(w));
            else if (partnerMothership != null && partnerMothership.owner.id == game.getMyself().id)
                tryPushWormhole(w, Engine.getClosestMyCapsuler(this.pirate));
            else if ((w.partner.distance(w) - (game.wormholeRange * 2)) <= 600)
                tryPushWormhole(w, w.partner);
            else
                tryPushWormhole(w, Engine.getClosestMyMothership(w));
        }
        try {
            for (Wormhole worm : game.getAllWormholes()) {
                if (worm.distance(game.getEnemyMotherships()[0]) < 1001 && pirate.distance(enemyCapsuler) > 1000 && pirate.pushReloadTurns < 2) {
                    destination = worm.getLocation().add(new Location(worm.wormholeRange, worm.wormholeRange));
                }
                if (game.getEnemyMotherships().length > 1) {
                    if (worm.distance(game.getEnemyMotherships()[1]) < 1001 && pirate.distance(enemyCapsuler) > 1000 && pirate.pushReloadTurns < 2) {
                        destination = worm.getLocation().add(new Location(worm.wormholeRange, worm.wormholeRange));
                    }
                }
            }

        } catch (Exception e) {
            game.debug("Bug");
        }
    }
    
    /**
     * sets the destination for campers as it was and returns the enmey capsuler
     */ 
    public Pirate setCamperPosition(){
        Pirate enemyCapsuler;
        int numOfEnemyShips = game.getEnemyMotherships().length;
        Mothership enemyMothership = game.getEnemyMotherships()[0];
        int firstLayer = 0, maxPush = game.pushDistance, layer1 = maxPush;
        int layer2 = (maxPush * 2) - 1;
        if (numOfEnemyShips < 2 && numOfEnemyShips > 0) {
            enemyCapsuler = getCapsulerToDefend(game.getEnemyMotherships()[0]);
            if (enemyCapsuler != null && enemyCapsuler.stateName.equals("heavy")) {
                firstLayer = 1;
            } else {
                firstLayer = 2;
            }
            if (camperCounter < firstLayer) {
                if (enemyCapsuler == null)
                    destination = enemyMothership;
                else
                    destination = enemyMothership.getLocation().towards(enemyCapsuler, layer1);
            } else {
                if (enemyCapsuler == null)
                    destination = enemyMothership.getLocation().towards(game.getEnemyCapsules()[0], 800);
                else
                    destination = enemyMothership.getLocation().towards(enemyCapsuler, layer2);
            }
            if (enemyCapsuler != null) {
                if (enemyCapsuler.distance(this.pirate) < 600 && this.pirate.pushReloadTurns < 1 && enemyCapsuler.hasCapsule()) {
                    destination = enemyCapsuler;
                }
            }

        } else {
            List<Mothership> enemyMotherShips = Arrays.asList(game.getEnemyMotherships());
            List<Capsule> enemyCapsules = Arrays.asList(game.getEnemyCapsules());
            List<Mothership> best = Engine.getBestEnemyBase(enemyMotherShips, enemyCapsules);
            enemyCapsuler = getCapsulerToDefend(best.get(0));
            if (camperCounter < 2) {
                if (enemyCapsuler == null) {
                    destination = best.get(0);
                } else {
                    destination = best.get(0).getLocation().towards(enemyCapsuler, layer1);
                }
            } else if (camperCounter < 5) {
                if (enemyCapsuler == null) {
                    destination = best.get(0).getLocation().towards(game.getEnemyCapsules()[0], 1000);
                } else {
                    destination = best.get(0).getLocation().towards(enemyCapsuler, layer2);
                }

            } else if (best.size() > 1) {
                enemyCapsuler = getCapsulerToDefend(best.get(1));
                if (enemyCapsuler == null) {
                    destination = best.get(1).getLocation().towards(game.getEnemyCapsules()[0], 1000);
                } else {
                    destination = best.get(1).getLocation().towards(enemyCapsuler, layer2);
                }
            } else {
                destination = best.get(0).getLocation().towards(enemyCapsuler, 1400);
            }
            if (enemyCapsuler != null) {
                if (enemyCapsuler.distance(this.pirate) < 601) {
                    destination = enemyCapsuler;
                }
            }

        }
        return enemyCapsuler;
    }
    
    /**
     * try and drop an enemy capsule
     */ 
    public void tryDropEnemyCapsule(){
        Pirate target = Engine.getClosestEnemyCapsuler(this.pirate);
        if(camperTryPushTarget(target)) return;
        
        target = Engine.getClosestEnemyCapsuler(this.pirate);
        Asteroid asteroid = Engine.getClosestLivingAsteroid(target);
        if(asteroid != null && !Engine.isAsteroidMoving(asteroid) && target.distance(asteroid) - asteroid.size < 600){
            tryPushPirate(target, asteroid);
        }
        else if(target.inRange(Engine.nearestWall(target), this.pirate.pushDistance)){
            tryPushPirate(target, Engine.nearestWall(target));
        }
        else{
            target = getEnemyTarget();
            if(target != null){
                tryPushPirate(target, Engine.nearestWall(target));
            }
        }
    }

    public void camperLogicV2() {
        if (camperCounter == -1) {
            BotPirate.capsuleDrops = Engine.getPossibleEnemyDrops(); //refresh drops every turn
        }
        camperCounter++;
    
        Pirate enemyCapsuler = setCamperPosition();
        
        tryDropEnemyCapsule();
        
        for (Asteroid astro : game.getLivingAsteroids()) {
            if (!moveAsteroid) {
                //tryPushAsteroid(astro, game.getMyMotherships()[0]);
                tryPushAsteroid(astro, Engine.pushAsteroidTo(astro));
            }
        }
        if(didPush) return;
        tryDefendWormhole(enemyCapsuler);
        tryCloseWormholePush();
        
    }

    // Capsuler Strategy
    public void capsulerLogic() {
        ++capsulerCounter;
        /* convert to anti camper, dont use its not that good
        Comparator<BotPirate> sorter = Comparator.comparing(bp -> bp.pirate.distance(this.pirate));
        BotPirate[] assginedCapsulers = PirateHandler.pirates.stream().filter(bp -> bp.pirate.id != this.pirate.id && bp.job.equals(Job.CAPSULER)).sorted(sorter).toArray(BotPirate[]::new);
        MapObject m;
        if(this.pirate.hasCapsule()){
            m = Engine.getClosestMyMothership(this.pirate).location;
        } else m = Engine.getClosestAvailableMyCapsule(this.pirate, BotPirate.unAvailableCapsules).location;
        int distanceToDest = this.pirate.distance(m);
        if(assginedCapsulers.length > 0 && assginedCapsulers[0].pirate.hasCapsule() && this.pirate.distance(assginedCapsulers[0].pirate) < distanceToDest){
            MapObject dest = this.destination;
            antiCamperLogicV2();
            System.out.println("lk");
            if(didPush || !this.destination.equals(dest)) return;
        }
        */
        
        List<Pirate> enemyBombers = Engine.enemyPiratesWithBomb();
        if (enemyBombers.size() > 0) {
            Collections.sort(enemyBombers, (p1, p2) -> p1.distance(this.pirate) - p2.distance(this.pirate));
            Pirate close = enemyBombers.get(0);
            if (close.distance(this.pirate) < game.stickyBombExplosionRange) {
                tryPushPirate(close, Engine.nearestWall(close));
                if (didPush) {
                    return;
                }
            }
        }
        if (game.getMyself().turnsToStickyBomb < 1) {
            if (game.getMyMotherships().length > 0) {
                //if (this.pirate.distance(game.getMyMotherships()[0]) < 1500) {
                for (Pirate enem : game.getEnemyLivingPirates()) {
                    if (!didPush && !PirateHandler.getBombed()) {
                        tryStickyBomb(enem);
                        PirateHandler.setBombed(true);
                    }
                }
                if (didPush) {
                    return;
                }
                //}
            }
        }
        boolean swapped = PirateHandler.getSwap();
        if(!swapped) {
            tryCapsulerSwapStates();
            if(PirateHandler.getSwap()) return;
        }

        for (Asteroid astro : game.getLivingAsteroids()) {
            tryPushAsteroid(astro, Engine.pushAsteroidTo(astro));
        }

        if (didPush) {
            //game.debug("Already pushed");
            return;
        }

        if (this.pirate.stickyBombs.length > 0) {
            if (Engine.turnsToDest(this.pirate, Engine.getClosestEnemyCapsuler(this.pirate)) < this.pirate.stickyBombs[0].countdown) {
                destination = Engine.getClosestEnemyCapsuler(this.pirate);
            } else if ((!this.pirate.hasCapsule() || Engine.minTurnsToDest(this.pirate, Engine.getClosestMyMothership(this.pirate)) > Engine.boomIn(this.pirate))
                    && ((Engine.turnsToDest(this.pirate, Engine.getClosestEnemy(this.pirate)) < this.pirate.stickyBombs[0].countdown
                    && Engine.firstBombToBlowUp(this.pirate) != null && Engine.friendsInRange(Engine.getClosestEnemy(this.pirate), Engine.firstBombToBlowUp(this.pirate).explosionRange) <= 1)))
                destination = Engine.getClosestEnemy(this.pirate);
            else
                destination = Engine.nearestWall(this.pirate);
            return;
//            bombOnMe();
//            return;
        }
        if (!this.pirate.hasCapsule()) {
            destination = Engine.getClosestAvailableMyCapsule(this.pirate, BotPirate.unAvailableCapsules);
            unAvailableCapsules.add((Capsule) destination);
            return;
        }

        Pirate fellowCapsuler = Engine.getClosestMyCapsuler(this.pirate);

        if (fellowCapsuler != null) {
            if (fellowCapsuler.hasCapsule() && Engine.willPushScore(this.pirate, fellowCapsuler)) {
                this.tryPushPirate(fellowCapsuler, Engine.getClosestMyMothership(fellowCapsuler));
            }
        }
        destination = Engine.getBestCapsulerMothership(this.pirate);
        tryCloseWormholePush();
    }

    public void astroLogic() {
        tryDropEnemyCapsule();
        if(didPush) return;
        boolean found = false;
        for (Asteroid a : game.getAllAsteroids()) {
            if (a != null)
                found = true;
        }
        if (game.getLivingAsteroids().length == 0 || (game.getLivingAsteroids().length == 1 && Engine.isAsteroidMoving(game.getLivingAsteroids()[0]))) {
            Wormhole w = Engine.getClosestWormhole(Engine.getClosestMyMothership(this.pirate));
            destination = w;
            if (destination != null && w.distance(Engine.getClosestMyMothership(w)) > 600)
                this.tryPushWormhole(w, Engine.getClosestMyMothership(w));
            return;
        }
        Asteroid asteroid = Engine.getClosestLivingAsteroid(this.pirate);
        Pirate[] enemys = game.getEnemyLivingPirates();
        ArrayList<Pirate> targets = new ArrayList<>();
        for (int i = 0; i < enemys.length; i++) {
            if (asteroid != null && Engine.getClosestEnemy(enemys[i]).distance(enemys[i]) <= asteroid.size)
                //  && Engine.getClosestEnemyMothership(enemys[i]).distance(enemys[i]) <= 3000)
                /*
                 * Added this provides the same result for now. check it aswell in the future.
                 * && Engine.getClosestFriend(enemys[i]).distance(enemys[i]) > asteroid.size)
                 */
                targets.add(enemys[i]);
        }
        if (targets.size() > 1)
            Collections.sort(targets, (t1, t2) -> t1.distance(this.pirate) - t2.distance(this.pirate));
        if (asteroid != null)
            this.destination = asteroid.getLocation().towards(this.pirate.getLocation(), asteroid.size + 1);
        else
            this.destination = game.getAllAsteroids()[0].initialLocation.towards(this.pirate.getLocation(),
                    game.getAllAsteroids()[0].size + 1);
        if (destination != null) {
            try {
                this.tryPushAsteroid(asteroid, Engine.pushAsteroidTo(asteroid));
            } catch (Exception e) {
                for (Mothership x : game.getMyMotherships()) {
                    try {
                        if (Engine.areThereCampers(x))
                            this.tryPushAsteroid(asteroid, Engine.getClosestEnemy(x));
                    } catch (Exception e2) {
                    }
                }
            }

        }
        tryCloseWormholePush();
    }

    public void bombedLogic() {
        Mothership enemyMom = Engine.getClosestEnemyMothership(this.pirate);
        Pirate enemyCapsuler = Engine.getClosestEnemyCapsuler(enemyMom);
        if (enemyCapsuler != null) {
            if (game.getEnemyMotherships().length > 0) {
                if (enemyMom.distance(enemyCapsuler) > 1200 + game.stickyBombExplosionRange) {
                    destination = enemyCapsuler;
                } else {
                    destination = Engine.getClosestEnemy(this.pirate);
                }
            }
        }

    }

    public boolean tryHelp() {
        if (game.getMyMotherships().length > 0) {
            if (this.pirate.hasCapsule()) {
                return false;
            }
            if (this.pirate.distance(game.getMyMotherships()[0]) > 2000) {
                return false;
            }
            Pirate p = Engine.getClosestMyCapsuler(this.pirate);
            if (p == null) {
                return false;
            }
            if (!p.hasCapsule()) {
                return false;
            }
            if (p.distance(game.getMyMotherships()[0]) > 2000) {
                return false;
            }
            if (p.distance(this.pirate) > 1000) {
                return false;
            }
            destination = p;
            if (this.pirate.canPush(p)) {

                int distance = p.distance(game.getMyMotherships()[0]);
                tryPushPirate(p, game.getMyMotherships()[0]);
                if (didPush) {
                    return true;
                }
            }
            return true;

        }
        return false;

    }


    public void antiCamperLogicV2(){
        
        Comparator<BotPirate> sorter = Comparator.comparing(bp -> bp.pirate.distance(this.pirate));
        BotPirate assginedCapsuler = PirateHandler.pirates.stream().filter(bp -> bp.pirate.id != this.pirate.id && bp.job.equals(Job.CAPSULER)).min(sorter).get();
        if(job.equals(Job.ANTICAMPER)) this.destination = assginedCapsuler.pirate;
        if(!assginedCapsuler.pirate.hasCapsule() || !this.pirate.canPush(assginedCapsuler.pirate) || assginedCapsuler.pirate.numPushesForCapsuleLoss < 2){
            this.destination = assginedCapsuler.pirate;
            if(!assginedCapsuler.pirate.hasCapsule()){
                Pirate camper = Engine.getClosestEnemy(Engine.getClosestMyMothership(this.pirate));
                if(Engine.willPushToWallKill(this.pirate, camper)) tryPushPirate(camper, Engine.nearestWall(camper));
                tryStickyBomb(camper);
                if(this.pirate.maxSpeed >= camper.maxSpeed && this.pirate.inRange(camper, game.stickyBombExplosionRange)){
                    MapObject bombingLocation = Engine.getBombingLocation(this.pirate.getLocation(), this.pirate.maxSpeed * game.stickyBombCountdown);
                    if(bombingLocation.inRange(camper, game.stickyBombExplosionRange)){
                        tryStickyBomb(this.pirate);
                    }
                }
                tryCloseWormholePush();
                if(didPush) return;
                if(!assginedCapsuler.pirate.hasCapsule() && this.pirate.distance(camper) < this.pirate.distance(assginedCapsuler.pirate)) destination = camper;
            }
            return;
        }
        
        Set<Location> pushLocations = PirateNavigator.getCircleLocations(assginedCapsuler.pirate.location, this.pirate.pushDistance);
        Set<Predicate<Location>> canHit = PirateNavigator.getHittersPredicate(game.getAllAsteroids()); 
        Predicate<Location> canPush = (l -> !canHit.stream().anyMatch(p -> p.test(l)));
        pushLocations = pushLocations.stream().filter(l -> l.distance(assginedCapsuler.destination) <= assginedCapsuler.pirate.distance(assginedCapsuler.destination)).filter(canPush).collect(Collectors.toSet());
        if(pushLocations.isEmpty()) return;
        Set<Location> instantPoint = pushLocations.stream().filter(l -> assginedCapsuler.destination.inRange(l, game.mothershipUnloadRange)).collect(Collectors.toSet());
        Comparator<Location> pushSorter = Comparator.comparing(Engine::getNumPushers);
        pushSorter = pushSorter.thenComparing(assginedCapsuler.destination::distance);
        if(!instantPoint.isEmpty()){
            instantPoint = pushLocations.stream().sorted(pushSorter).collect(Collectors.toSet());
            tryPushPirate(assginedCapsuler.pirate, instantPoint.iterator().next());
            return;
        }
        
        if(assginedCapsuler.getNextStep() != null && Engine.capsulerRisk(assginedCapsuler.getNextStep(), assginedCapsuler.pirate.numPushesForCapsuleLoss)){
            pushLocations = pushLocations.stream().filter(l -> Engine.getNumPushers(l) >= assginedCapsuler.pirate.numPushesForCapsuleLoss).collect(Collectors.toSet());
            if(pushLocations.isEmpty()) return;
            Location pushLocation = pushLocations.stream().min(pushSorter).get();
            tryPushPirate(assginedCapsuler.pirate, pushLocation);
        }
        
    }


    // INIT every job
    public void work() {
        try {
            switch (this.job) {
                case DEAD:
                    //game.debug("Pirate #" + this.pirate.id + " is DEAD");
                    break;
                case CAPSULER:
                    //game.debug("Pirate #" + this.pirate.id + " is a CAPSULER");
                    if (!tryHelp()) {
                        capsulerLogic();
                    }
                    sailToDest();
                    break;
                case CAMPER:
                    //game.debug("Pirate #" + this.pirate.id + " is a CAMPER");
                    camperLogicV2();
                    sailToDest();
                    break;
                case ANTICAMPER:
                    //game.debug("Pirate #" + this.pirate.id + " is an ANTICAMPER");
                    antiCamperLogicV2();
                    sailToDest();
                    break;

                case BOMBED:
                    bombedLogic();
                    sailToDest();
                    break;

                case ASTRO:
                    //game.debug("Pirate #" + this.pirate.id + " is an ASTRO");
                    if (!tryHelp()) {
                        astroLogic();
                    }
                    sailToDest();
                    break;
                default:
                    //game.debug("No Job Found");
                    break;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
