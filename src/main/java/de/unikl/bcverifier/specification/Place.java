package de.unikl.bcverifier.specification;

public class Place {
    private String name;
    private String condition;
    private String measure;
    
    public Place(String name, String condition, String measure){
        this.name = name;
        this.condition = condition;
        this.measure = measure;
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

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append(": ");
        builder.append(condition);
        if(measure != null){
            builder.append(" (measure: ");
            builder.append(measure);
            builder.append(")"); 
        }
        return builder.toString(); 
    }
}