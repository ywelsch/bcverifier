package de.unikl.bcverifier.specification;

import java.util.List;


public interface Generator {
    public List<SpecInvariant> generateInvariant() throws GenerationException;
    public List<String> generateLocalInvariant() throws GenerationException;
    public List<String> generatePreconditions() throws GenerationException;
    public LocalPlaceDefinitions generateLocalPlaces() throws GenerationException;
    public List<String> generatePreludeAddition() throws GenerationException;
}
