package de.unikl.bcverifier.specification;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class LocalPlaceDefinitions{
    private Map<Integer, List<Place>> oldMap;
    private Map<Integer, List<Place>> newMap;
    
    public LocalPlaceDefinitions(Map<Integer, List<Place>> map, Map<Integer, List<Place>> map2){
        this.oldMap = map;
        this.newMap = map2;
    }
    
    public List<Place> getPlaceInOld(int line1, int line2){
        return findPlaceBetween(oldMap, line1, line2);
    }
    
    public List<Place> getPlaceInNew(int line1, int line2){
        return findPlaceBetween(newMap, line1, line2);
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
}