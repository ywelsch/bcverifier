package de.unikl.bcverifier.specification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class LocalPlaceDefinitions{
	private Map<Integer, List<Place>> oldMap;
    private Map<Integer, List<Place>> newMap;
    private Map<Integer, List<Place>> oldMapRemaining;
    private Map<Integer, List<Place>> newMapRemaining;
    
    public LocalPlaceDefinitions(Map<Integer, List<Place>> map, Map<Integer, List<Place>> map2){
        this.oldMap = map;
        this.newMap = map2;
        oldMapRemaining = new HashMap<Integer, List<Place>>(oldMap);
        newMapRemaining = new HashMap<Integer, List<Place>>(newMap);
    }
    
    public List<Place> getPlaceInOld(int line1, int line2){
        return findPlaceBetween(oldMapRemaining, line1, line2);
    }
    
    public List<Place> getPlaceInNew(int line1, int line2){
        return findPlaceBetween(newMapRemaining, line1, line2);
    }
    
    private static List<Place> findPlaceBetween(Map<Integer,List<Place>> placeMap, int line1, int line2){
        int pairLine;
        for(Entry<Integer, List<Place>> pair : placeMap.entrySet()){
            pairLine = pair.getKey();
            if(line1 < pairLine && pairLine <= line2){
                placeMap.remove(pair.getKey());
                return pair.getValue();
            }
        }
        return null;
    }
    
    public List<Place> oldPlaces() {
    	return collectPlaces(oldMap);
    }
    
    public List<Place> newPlaces() {
    	return collectPlaces(newMap);
    }
    
    private List<Place> collectPlaces(Map<Integer, List<Place>> map) {
    	List<Place> result = new ArrayList<Place>();
    	for (List<Place> places : map.values()) {
    		result.addAll(places);
    	}
    	return result;
    }
}