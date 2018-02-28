package bots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pirates.Location;

/**
 * Represents a basic MapNode
 * @author Gal Schwietzer
 *
 */
public class MapNode implements HasNeighbours<MapNode> {
    private static Map<MapNode, List<MapNode>> neighboursCache = new HashMap<>(); //cache for A* Node neighbours
    private static final int diagonalFactor = 141; //~200 / sqrt(2)
    private static final int straightFactor = 200;

    private int index;
    private Location location; 
    
    /**
     * returns the possible row element of neighbours, hardcoded for runtime
     * @param row
     * @return the arrays with the possible rows
     */
    private static int[] getPossibleRows(int row){
        return new int[]{row + straightFactor, row - straightFactor, row, row, row + diagonalFactor, row - diagonalFactor, row - diagonalFactor, row + diagonalFactor};
                //row + straightFactor / 2, row - straightFactor / 2, row, row, row + diagonalFactor / 2, row - diagonalFactor / 2, row - diagonalFactor / 2, row + diagonalFactor / 2, 
                //row + straightFactor / 4, row - straightFactor / 4, row, row, row + diagonalFactor / 4, row - diagonalFactor / 4, row - diagonalFactor / 4, row + diagonalFactor / 4}; //doing this to save computation time

    }
    
    /**
     * returns the possible col element of neighbours, hardcoded for runtime
     * @param col
     * @return the arrays wth the possible cols
     */
    private static int[] getPossibleCols(int col){
        return new int[]{col, col, col + straightFactor, col - straightFactor, col + diagonalFactor, col - diagonalFactor, col + diagonalFactor, col - diagonalFactor};
                //col, col, col + straightFactor / 2, col - straightFactor / 2, col + diagonalFactor / 2, col - diagonalFactor / 2, col + diagonalFactor / 2, col - diagonalFactor / 2, 
                //col, col, col + straightFactor / 4, col - straightFactor / 4, col + diagonalFactor / 4, col - diagonalFactor / 4, col + diagonalFactor / 4, col - diagonalFactor / 4};
    }
    
    public MapNode(Location location, int index){
        this.location = location;
        this.index = index;
    }
    
    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }
    
    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object thatObject){
        if(!(thatObject instanceof MapNode)) return false;
        MapNode that = (MapNode)thatObject;
        return this.location.equals(that.location);
    }
    
    @Override
    public String toString(){
        return location.row + " " + location.col;
    }


    public Iterable<MapNode> getNeighbours(MapNode destination){
        List<MapNode> neighbours = neighboursCache.get(this);
        if(neighbours != null) return neighbours;
        neighbours = new ArrayList<>();            
        int[] rows = getPossibleRows(location.row);
        int[] cols = getPossibleCols(location.col); 
        for(int i = 0; i < rows.length; i++){
            int row = rows[i], col = cols[i];
            if(row < 0 || col < 0 || row > 6400 || col > 6400) continue;
            Location location = new Location(row, col);
            neighbours.add(new MapNode(location, index + 1));
        }
        Location diagonal1 = location.towards(destination.location, straightFactor); //doing this to get perfect diagonal to the destination
        //Location diagonal2 = location.towards(destination.location, straightFactor / 2);
        //Location diagonal3 = location.towards(destination.location, straightFactor / 4);
        neighbours.add(new MapNode(diagonal1, index + 1));
        //neighbours.add(new MapNode(diagonal2, index + 1));
        //neighbours.add(new MapNode(diagonal3, index + 1));
        neighboursCache.put(this, neighbours);
        return neighbours;
    }
}
