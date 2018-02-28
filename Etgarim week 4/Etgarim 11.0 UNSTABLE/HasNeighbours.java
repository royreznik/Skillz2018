package bots;

/**
 * 
 * @author Gal Schwietzer
 *
 * @param <N> Type of neighbours
 */
public interface HasNeighbours<N> {
    /**
     * returns the index of the Node in the path
     * @return index
     */
    public int getIndex(); 
    
    /**
     * sets the node's index in the pat
     * @param index index to set
     */
    public void setIndex(int index); 
    
    /**
     * returns an iterable of the Node's neighbours
     * @param destination
     * @return the neighbours
     */
    public Iterable<N> getNeighbours(N destination);
}
