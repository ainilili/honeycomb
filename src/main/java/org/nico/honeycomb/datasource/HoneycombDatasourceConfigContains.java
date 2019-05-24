package org.nico.honeycomb.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HoneycombDatasourceConfigContains {

    private static volatile HoneycombDatasourceConfigContains instance = null;
    
    private static volatile boolean inited = false;
    
    private Map<String, HoneycombDatasourceConfig> configMap;
    
    private HoneycombDatasourceConfigContains() {}
    
    public static HoneycombDatasourceConfigContains getInstance() {
        if(! inited) {
            synchronized (HoneycombDatasourceConfigContains.class) {
                if(! inited) {
                    inited = true;
                    try {
                        instance = new HoneycombDatasourceConfigContains();
                        instance.configMap = new ConcurrentHashMap<String, HoneycombDatasourceConfig>(20);
                    }catch(Throwable e) {
                        inited = false;
                    }
                }
            }
        }
        return instance;
    }
    
    public HoneycombDatasourceConfig put(String name, HoneycombDatasourceConfig config) {
        configMap.putIfAbsent(name, config);
        return config;
    }
    
    public HoneycombDatasourceConfig get(String name) {
        return configMap.get(name);
    }
}
