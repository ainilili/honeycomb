package org.nico.honeycomb.connection.pool.feature;

import java.util.concurrent.ArrayBlockingQueue;

import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;

public class HoneycombConnectionPoolLRU extends Thread{

    private HoneycombConnectionPool pool;
    
    public HoneycombConnectionPoolLRU(HoneycombConnectionPool pool) {
        this.pool = pool;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        synchronized (pool) {
            ArrayBlockingQueue<HoneycombConnection> idleQueue = pool.getIdleQueue();
            
        }
    }

}
