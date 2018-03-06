package bots;

import java.util.LinkedList;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * a Priority Queue for A*
 * @author Gal Schwietzer
 *
 * @param <P> The priority of each pair
 * @param <V> The Value of each pair
 */
public class PriorityQueue<P, V>{
    private SortedMap<P, Queue<V>> list = new TreeMap<P, Queue<V>>();
    
    /**
     * inserts a new element to the queue
     * @param priority
     * @param value
     */
    public void enqueue(P priority, V value){
        Queue<V> q = list.get(priority);
        if(q == null){
            q = new LinkedList<V>();
            list.put(priority,q);
        }
        q.add(value);
    }
    
    /**
     * 
     * @return the first element of the lowest priority in this queue
     */
    public V dequeue(){
        P priority = list.firstKey();
        V value = list.get(priority).remove();
        if(list.get(priority).isEmpty()) list.remove(priority);
        return value;
    }
    
    /**
     * 
     * @return true if the queue is empty
     */
    public boolean isEmpty(){
        return list.isEmpty();
    }
     
}
