package org.nico.honeycomb.connection;

public interface HoneycombConnectionSwitcher {

    boolean switchLeisure();
    
    boolean switchRecycled();
    
    boolean switchOccupied();
    
    boolean leisured();
    
    boolean recycled();
    
    boolean occupied();

}
