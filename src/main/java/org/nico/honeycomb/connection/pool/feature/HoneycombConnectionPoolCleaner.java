package org.nico.honeycomb.connection.pool.feature;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingDeque;

import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneycombConnectionPoolCleaner extends Thread{

    private long maxIdleTime;

    private long interval;

    private HoneycombConnectionPool pool;

    private Logger logger = LoggerFactory.getLogger(HoneycombConnectionPoolCleaner.class);

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
            LinkedBlockingDeque<HoneycombConnection> idleQueue = pool.getIdleQueue();
            synchronized (idleQueue) {
                logger.debug("Cleaner Model To Start {}", idleQueue.size());
                idleQueue.stream().filter(c -> { return c.idleTime() > maxIdleTime; }).forEach(c -> {
                    try {
                        if(! c.isClosedActive() && c.idle()) {
                            c.closeActive();
                            pool.freeze(c);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } 
                });
                logger.debug("Cleaner Model To Finished {}", idleQueue.size());
            }
        }
    }

}
