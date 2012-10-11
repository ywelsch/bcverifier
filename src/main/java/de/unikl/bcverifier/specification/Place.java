package de.unikl.bcverifier.specification;

public class Place {
    private String name;
    private String condition;
    private String oldStallCondition;
    private String oldMeasure;
    private String newStallCondition;
    private String newMeasure;
    private boolean old;
    
    public Place(boolean old, String name, String condition, String oldStallCondition, String oldMeasure, String newStallCondition, String newMeasure){
        this.old = old;
    	this.name = name;
        this.condition = condition;
        this.setOldStallCondition(oldStallCondition);
        this.setOldMeasure(oldMeasure);
        this.setNewStallCondition(newStallCondition);
        this.setNewMeasure(newMeasure);
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append(": ");
        builder.append(condition);
        if(getNewStallCondition() != null) {
        	builder.append(" (stall: ");
            builder.append(getNewStallCondition());
            builder.append(")");
        }
        if(getNewMeasure() != null){
            builder.append(" (measure: ");
            builder.append(getNewMeasure());
            builder.append(")"); 
        }
        return builder.toString(); 
    }

	public String getOldMeasure() {
		return oldMeasure;
	}

	public void setOldMeasure(String oldMeasure) {
		this.oldMeasure = oldMeasure;
	}

	public String getOldStallCondition() {
		return oldStallCondition;
	}

	public void setOldStallCondition(String oldStallCondition) {
		if (oldStallCondition == null) {
			oldStallCondition = "false";
		}
		this.oldStallCondition = oldStallCondition;
	}

	public String getNewStallCondition() {
		return newStallCondition;
	}

	public void setNewStallCondition(String newStallCondition) {
		if (newStallCondition == null) {
			newStallCondition = "false";
		}
		this.newStallCondition = newStallCondition;
	}

	public String getNewMeasure() {
		return newMeasure;
	}

	public void setNewMeasure(String newMeasure) {
		this.newMeasure = newMeasure;
	}

	public boolean isOld() {
		return old;
	}

	public void setOld(boolean old) {
		this.old = old;
	}
}