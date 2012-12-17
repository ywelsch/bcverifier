package de.unikl.bcverifier.specification;

import java.util.List;

import b2bpl.bpl.ast.BPLBoolLiteral;

public class Place {
    private final String name;
    private final SpecExpr condition;
    private final SpecExpr oldStallCondition;
    private final SpecExpr oldMeasure;
    private final SpecExpr newStallCondition;
    private final SpecExpr newMeasure;
    private final boolean old;
    private final boolean nosplit;
    private final boolean nosync;
    private final List<SpecAssignment> assignments;
    private final String className;
    
    public Place(boolean old, String name, String className, boolean nosplit, boolean nosync, SpecExpr condition, SpecExpr oldStallCondition, SpecExpr oldMeasure, SpecExpr newStallCondition, SpecExpr newMeasure, List<SpecAssignment> assignments){
    	this.old = old;
    	this.name = name;
        this.condition = condition;
        this.className = className;
        this.nosplit = nosplit;
        this.nosync = nosync;
        
        if (oldStallCondition == null) {
			oldStallCondition = new SpecExpr(BPLBoolLiteral.FALSE, BPLBoolLiteral.TRUE);
		}
        if (newStallCondition == null) {
			newStallCondition = new SpecExpr(BPLBoolLiteral.FALSE, BPLBoolLiteral.TRUE);
		}
        
        this.oldStallCondition = oldStallCondition;
        this.oldMeasure = oldMeasure;
        this.newStallCondition = newStallCondition;
        this.newMeasure = newMeasure;
        this.assignments = assignments;
        
    }
    
    public String getName() {
        return name;
    }

    public SpecExpr getCondition() {
        return condition;
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

	public SpecExpr getOldMeasure() {
		return oldMeasure;
	}

	public SpecExpr getOldStallCondition() {
		return oldStallCondition;
	}

	public SpecExpr getNewStallCondition() {
		return newStallCondition;
	}

	public SpecExpr getNewMeasure() {
		return newMeasure;
	}

	public boolean isOld() {
		return old;
	}

	public List<SpecAssignment> getAssignments() {
		return assignments;
	}

	public String getClassName() {
		return className;
	}

	public boolean isNosplit() {
		return nosplit;
	}

	public boolean isNosync() {
		return nosync;
	}

}