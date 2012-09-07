package de.unikl.bcverifier.exceptionhandling;

public class SimulationStep {
    public enum Action {
        METHOD_CALL,
        METHOD_RETURN,
        CONSTRUCTOR_CALL,
        LOCAL_CONTINUE,
        LOCAL_CHECK
    }
    
    public enum Direction {
        IN,
        OUT,
        INTERN
    }
    
    private Action action;
    private Direction direction;
    private String inMethod;
    private String calledMethod;
    private String localPlace;
    
    public SimulationStep(Action action, Direction direction, String inMethod) {
        super();
        this.action = action;
        this.direction = direction;
        this.inMethod = inMethod;
    }

    public SimulationStep(Action action, Direction direction, String inMethod,
            String calledMethod, String localPlace) {
        super();
        this.action = action;
        this.direction = direction;
        this.inMethod = inMethod;
        this.calledMethod = calledMethod;
        this.localPlace = localPlace;
    }

    public Action getAction() {
        return action;
    }
    public void setAction(Action action) {
        this.action = action;
    }
    public Direction getDirection() {
        return direction;
    }
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    public String getInMethod() {
        return inMethod;
    }
    public void setInMethod(String inMethod) {
        this.inMethod = inMethod;
    }
    public String getCalledMethod() {
        return calledMethod;
    }
    public void setCalledMethod(String calledMethod) {
        this.calledMethod = calledMethod;
    }
    public String getLocalPlace() {
        return localPlace;
    }
    public void setLocalPlace(String localPlace) {
        this.localPlace = localPlace;
    }
    
    
}
