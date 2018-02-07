package bots;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

import pirates.Asteroid;
import pirates.Location;

public class PirateNavigator {
    
    public static HashSet<Asteroid> alreadyPushed = new HashSet<>();
    
    /**
     * an equation that given a circle's center point, the circle's radius and an x value thats on said value, will return 2 y values for x
     */
    private static final BiFunction<Integer, Integer, IntFunction<IntFunction<int[]>>> GET_CIRCLE_LOCATION = (h, k) ->  r-> x -> {
        double sqrt = 4 * (r - (x - h)) * (r + (x - h));
        sqrt = Math.sqrt(sqrt);
        int y1 = (int)((2 * k + sqrt) / 2.0), y2 = (int)((2 * k - sqrt) / 2.0);
        return new int[] {y1, y2};
    };
    private static final int SALING_RADIUS = 200;
    
    
    public static void navigate(BotPirate bp){
        Location currentPath = bp.pirate.location.towards(bp.destination, 200);
        Asteroid[] hitters = Engine.willAsteroidHit(currentPath);
        hitters = Arrays.asList(hitters).stream().filter(a -> !alreadyPushed.contains(a)).toArray(Asteroid[]::new);
        Asteroid[] canPush = Arrays.asList(hitters).stream().filter(a -> bp.pirate.canPush(a)).toArray(Asteroid[]::new);
        if(hitters.length == 0) {
            bp.pirate.sail(currentPath); //no threat to pirate
            return;
        }
        else if(canPush.length == 1 && bp.pirate.canPush(canPush[0])){
            bp.pirate.push(canPush[0], Engine.pushAsteroidTo(canPush[0]));
            alreadyPushed.add(canPush[0]);
            bp.didPush = true;
            return;
        }
        else {
            Set<Predicate<Location>> canHit = getHittersPredicate(hitters); 
            Predicate<Location> canMove = (l -> !canHit.stream().anyMatch(p -> p.test(l))); //checks if any asteroid are going to hit location
            ToIntBiFunction<Location, Location> locationValue = (l1, l2) -> 0; //default function is that all location have the same preference
            
            switch(bp.job){
                case CAPSULER:
                   // locationValue = (l1, l2) -> Engine.getNumPushers(l1) - Engine.getNumPushers(l2); //capsuler will rather go to the location with the least enemies
                default:
                    break;
            }
            
            Location deviation = getDeviation(bp, canMove, locationValue, SALING_RADIUS);
            if(deviation != null){ //if a deviation was found
                bp.pirate.sail(deviation);
            }
            //else... currently nothing is done, can check different movement radiuses(100, 50, etc) to see if a deviation is possible  
        }
        
    }

    
    /**
     * returns the deviation with the minimal impact to the course 
     * @param bp pirate making the course
     * @param canMove a predicate to tell if bp can move to said location
     * @param locationValue a comparator that tells which location is better traveled to
     * @param movementRadius the movement radius for the deviation
     * @return the deviation or null if none were found
     */
    private static Location getDeviation(BotPirate bp, Predicate<Location> canMove, ToIntBiFunction<Location, Location> locationValue, int movementRadius){
        Location center = bp.pirate.location, destination = bp.destination.getLocation();
        IntFunction<int[]> getY = GET_CIRCLE_LOCATION.apply(center.col, center.row).apply(movementRadius);
        Predicate<Location> canAdd = l -> l.inMap() && canMove.test(l);
        HashSet<Location> possibleDeviations = new HashSet<Location>() {
            private static final long serialVersionUID = 1L; //shows warning without this, Eclipse did it and I have no idea what it does

            public boolean add(Location l){  
                if(!canAdd.test(l)) return false;  //doing this so only good deviations will be added to the set
                return super.add(l);
            }
        };
        for(int i = 0; i < movementRadius; i++){ 
            int x1 = center.col + i, x2 = center.col - 0;
            int[] y1 = getY.apply(x1), y2 = getY.apply(x2);
            for(int j = 0; j < y1.length; j++){
                Location l1 = new Location(y1[j], x1), l2 = new Location(y2[j], x2);
                l1 = center.towards(l1, movementRadius); // doing this to avoid rounding errors 
                l2 = center.towards(l2, movementRadius); // ^^^
                possibleDeviations.add(l1);
                possibleDeviations.add(l2);
            }
        }
        if(possibleDeviations.isEmpty()) return null; //return null if no deviations were found, extremely rare
        int lowestDeviation = possibleDeviations.stream().min( (l1, l2) -> Integer.compare(l1.distance(destination), l2.distance(destination))).get().distance(destination);
        Location[] minimalDeviations = possibleDeviations.stream().filter(l -> l.distance(destination) == lowestDeviation).toArray(Location[]::new); //gets all the deviations with minimal impact
        Location deviation = Arrays.asList(minimalDeviations).stream().min( (l1, l2) -> locationValue.applyAsInt(l1, l2)).get(); //gets the best deviation according to locValue 
        return deviation;
    }
    
    /**
     * returns an array of predicate of each asteroid in hitters
     * that returns true if the asteroid will hit the location next turn
     * @param hitters
     * @return
     */
    private static Set<Predicate<Location>> getHittersPredicate(Asteroid[] hitters) {
        HashSet<Predicate<Location>> p = new HashSet<>();
        for(Asteroid a : hitters){
            Location loc;
            if(a.turnsToRevive == 0){
                loc = a.location.add(a.direction);
            } else {
                loc = a.initialLocation;
            }
            p.add(l -> loc.inRange(l, a.size));
        }
        return p;
    }

}
