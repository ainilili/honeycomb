package org.nico.honeycomb.connection.pool;

public interface HoneycombConnectionPoolFeature {

    public void enableCleaner();
    
    public void enableMonitor();
    
    public void enableLRU();
}
