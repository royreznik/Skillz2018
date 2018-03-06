package bots;

import java.util.Iterator;


/**
 * Represents an A* path that consists of Nodes
 * @author Gal Schwietzer
 *
 * @param <Node> The Path's step type
 */
public class Path<Node extends HasNeighbours<Node>> implements Iterable<Node>{
    private Node firstStep;
    private Node lastStep; 
    private Path<Node> previousSteps;
    private double totalCost;
    
    private Path(Node firstStep, Node lastStep, Path<Node> previousSteps, double totalCost){
        this.firstStep = firstStep;
        this.lastStep = lastStep;
        this.previousSteps = previousSteps;
        this.totalCost = totalCost;
    }
    
    public Path(Node start){
        this(null, start, null, 0);
    }
    
    public Path<Node> addStep(Node step, double stepCost){
        return new Path<Node>(firstStep == null ? step : firstStep, step, this, this.totalCost + stepCost);
    }

    /**
     * @return the firstStep
     */
    public Node getFirstStep() {
        return firstStep;
    }

    /**
     * @return the lastStep
     */
    public Node getLastStep() {
        return lastStep;
    }

    /**
     * @param lastStep the lastStep to set
     */
    public void setLastStep(Node lastStep) {
        this.lastStep = lastStep;
    }

    /**
     * @return the previousSteps
     */
    public Path<Node> getPreviousSteps() {
        return previousSteps;
    }

    /**
     * @param previousSteps the previousSteps to set
     */
    public void setPreviousSteps(Path<Node> previousSteps) {
        this.previousSteps = previousSteps;
    }

    /**
     * @return the totalCost
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * @param totalCost the totalCost to set
     */
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    @Override
    public Iterator<Node> iterator() {
        Path<Node> p = this;
        Iterator<Node> it = new Iterator<Node>(){
            private Path<Node> currentStep = p;
            
            @Override
            public boolean hasNext() {
                return currentStep != null;
            }
            
            @Override
            public Node next(){
                Node n = currentStep.lastStep;
                currentStep = currentStep.previousSteps;
                return n;
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }  
        };
        return it;
    }
}
