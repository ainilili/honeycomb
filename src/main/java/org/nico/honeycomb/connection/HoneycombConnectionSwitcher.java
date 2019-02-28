package org.nico.honeycomb.connection;

public interface HoneycombConnectionSwitcher {

    boolean switchIdle();
    
    boolean switchOccupied();
    
    boolean idle();
    
    boolean occupied();

}
