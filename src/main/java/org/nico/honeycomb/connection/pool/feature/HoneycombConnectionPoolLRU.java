package org.nico.honeycomb.connection.pool.feature;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoneycombConnectionPoolLRU extends Thread{

    private long interval;
    
    private HoneycombConnectionPool pool;
    
    private Logger logger = LoggerFactory.getLogger(HoneycombConnectionPoolLRU.class);
    
    public HoneycombConnectionPoolLRU(HoneycombConnectionPool pool, long interval) {
        this.pool = pool;
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
                logger.info("LRU Model To Start：" + idleQueue);
                
                idleQueue.stream().sorted(new Comparator<HoneycombConnection>() {
                    @Override
                    public int compare(HoneycombConnection o1, HoneycombConnection o2) {
                        return 0;
                    }
                });
                
                List<HoneycombConnection> sortedList = idleQueue.stream().sorted(Comparator.comparing(HoneycombConnection::usageFrequency).reversed()).collect(Collectors.toList());
                idleQueue.clear();
                idleQueue.addAll(sortedList);
                logger.info("LRU Model To Finished：" + idleQueue);
            }
        }
    }

}
