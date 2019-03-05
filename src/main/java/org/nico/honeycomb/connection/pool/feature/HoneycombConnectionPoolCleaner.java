package org.nico.honeycomb.connection.pool.feature;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;

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
                logger.debug("Cleaner Model To Start：" + idleQueue);
                idleQueue.parallelStream().filter(c -> { return c.idleTime() > maxIdleTime; }).forEach(c -> {
                    try {
                        if(! c.isClosedActive()) {
                            c.closeActive();
                        }
                        pool.freeze(c);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                logger.debug("Cleaner Model To Finished：" + idleQueue);
            }
        }
    }

}
