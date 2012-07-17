package de.unikl.bcverifier.specification;

import java.util.List;

public interface Generator {
    public List<String> generateInvariant();
    public List<String> generateLocalInvariant();
    public List<String> generatePreconditions();
    public LocalPlaceDefinitions generateLocalPlaces();
}
