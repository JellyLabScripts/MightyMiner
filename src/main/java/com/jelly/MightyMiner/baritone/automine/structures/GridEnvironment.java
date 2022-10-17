package com.jelly.MightyMiner.baritone.automine.structures;


import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.collections4.map.MultiKeyMap;


public class GridEnvironment<T> {

    MultiKeyMap parameters = MultiKeyMap.multiKeyMap(new LinkedMap());

    public void set(int x, int y, int z, T parameter){
        parameters.put(x, y, z, parameter);
    }
    public T get(int x, int y, int z){
        return (T)parameters.get(x, y, z);
    }
    public void clear(){
        parameters.clear();
    }
}
