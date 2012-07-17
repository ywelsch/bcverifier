package de.unikl.bcverifier.specification;

public class Place {
    private String name;
    private String condition;
    
    public Place(String name, String condition){
        this.name = name;
        this.condition = condition;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCondition() {
        return condition;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }
}