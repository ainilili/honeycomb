package org.nico.honeycomb.connection.pool.feature;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneycombConnectionPoolCleaner extends Thread{

    private long maxIdleTime;
    
    private long interval;
    
    private HoneycombConnectionPool pool;
    
    private Logger logger = LoggerFactory.getLogger(HoneycombConnectionPoolLRU.class);
    
    public HoneycombConnectionPoolCleaner(HoneycombConnectionPool pool, long maxIdleTime, long interval) {
        this.pool = pool;
        this.maxIdleTime = maxIdleTime;
        this.interval = interval;
        this.setDaemon(true);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
            }
            synchronized (pool) {
                ArrayBlockingQueue<HoneycombConnection> idleQueue = pool.getIdleQueue();
                logger.info("Cleaner Model To Start：" + idleQueue);
                
                logger.info("Cleaner Model To Finished：" + idleQueue);
            }
        }
    }

}
