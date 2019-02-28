package org.nico.honeycomb.connection.pool.feature;

import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;

public class HoneycombConnectionPoolCleaner extends Thread{

    private HoneycombConnectionPool pool;
    
    public HoneycombConnectionPoolCleaner(HoneycombConnectionPool pool) {
        this.pool = pool;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        pool.getIdleQueue();
        
    }

}
