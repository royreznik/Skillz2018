package bots;

import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import bots.BotPirate;
import bots.Engine;
import pirates.Location;
import pirates.Mothership;

public class PirateNavigator2 {
    
    private static final double CAPSULER_DESTINATION_RADIUS = 300; //factor to divide distance by
    private static final int DROP_COST = 15000; //cost of possibly dropping capsule 
    private static final int ASTEROID_COST = DROP_COST; //cost of possibly getting hit by asteroid
    
    /***
     * navigates the botPirate to it's destination using A*
     * note - only supported for capsuler right now
     * @param bp pirate to navigate
     
    public static void Navigate(BotPirate bp){
        switch(bp.getJob()){
        case CAPSULER:
            if(!bp.getPirate().hasCapsule()) bp.getPirate().sail(bp.getDestination());
            else{
                MapNode start = new MapNode(bp.getPirate().location, 0);
                BiFunction<MapNode, MapNode, Double> cost = PirateNavigator2::capsulerCost;
                BiFunction<MapNode, MapNode, Integer> distance = (s, d) -> s.getLocation().distance(d.getLocation());
                SortedMap<Double, Path<MapNode>> paths = new TreeMap<>();
                HashMap<Path<MapNode>, Mothership> mothershipPath = new HashMap<>();
                for(Mothership m : Engine.getMyMotherships()){ //finds the best mothership to travel to cost wise
                    MapNode destination = new MapNode(m.location, -1);
                    Function<MapNode, Double> estimate = n -> (double)(n.getLocation().distance(destination.getLocation()));
                    int destinationRadius = m.unloadRange;
                    Path<MapNode> bestPath = FindPath(start, destination, cost, distance ,estimate, destinationRadius);
                    paths.put(bestPath.getTotalCost(), bestPath);
                    mothershipPath.put(bestPath, m); //doing this so bp.destination can be set to appropriate mothership
                }
                Path<MapNode> shortestPath = paths.get(paths.firstKey());
                bp.setDestination(mothershipPath.get(shortestPath));
                bp.getPirate().sail(shortestPath.getFirstStep().getLocation()); //start sailing to first MapNode in the shortest path
            }
            break;
        default:
            bp.getPirate().sail(bp.getDestination()); //currently not supported for anything other then capsuler -> default sail function
            break;
        }
    }
    
    /**
     * A* algorithm implementation
     * @param start the start node
     * @param destination the destination Node
     * @param cost function of return the cost of going from one node to another 
     * @param distance returns the distance between two nodes
     * @param estimate heuristic function to guess the cost of going from a node to the destination, should NOT overestimate the correct value
     * @param destinationRadius the maximum radius that you need to get from the destination
     * @return the best Path cost wise for getting from start to destination
     */
    public static<Node extends HasNeighbours<Node>> Path<Node> FindPath(
            Node start, Node destination,
            BiFunction<Node, Node, Double> cost,
            BiFunction<Node, Node, Integer> distance,
            Function<Node, Double> estimate,
            int destinationRadius){
        HashSet<Node> closed = new HashSet<Node>();
        PriorityQueue<Double, Path<Node>> queue = new PriorityQueue<Double, Path<Node>>();
        queue.enqueue(0.0, new Path<Node>(start));
        while (!queue.isEmpty())
        {
            Path<Node> path = queue.dequeue();
            System.out.println(path.getLastStep());
            if (closed.contains(path.getLastStep()))
                continue;
            if (distance.apply(path.getLastStep(), destination) <= destinationRadius){
                destination.setIndex(path.getLastStep().getIndex() + 1);
                path = path.addStep(destination, 0);
                return path;
            }
            closed.add(path.getLastStep());
            for(Node n : path.getLastStep().getNeighbours(destination))
            {
                double d = cost.apply(path.getLastStep(), n);
                Path<Node> newPath = path.addStep(n, d);
                queue.enqueue(newPath.getTotalCost() + estimate.apply(n), newPath);
            }
        }
        return null;
    }
    
    /**
     * calculates the cost of going from start to end for the capsuler 
     * @param start the start node
     * @param end the end node
     * @return the cost of the trip
     */
    public static double capsulerCost(MapNode start, MapNode end){
        Location s = start.getLocation(), e = end.getLocation();
        //boolean dropRisk = Engine.capsulerRisk(e); //if a drop is possible at location
        //boolean asteroidRisk = Engine.numTurnsToHit(e) == end.getIndex(); //if an asteroid will be in e at the same time as the capsuler
        return (double)(s.distance(e));// + (dropRisk ? DROP_COST : 0) + (asteroidRisk ? ASTEROID_COST : 0); //calculating the cost to go from start to end   
    }
    
    
}
