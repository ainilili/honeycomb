package org.nico.honeycomb.connection.pool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.feature.HoneycombConnectionPoolCleaner;
import org.nico.honeycomb.connection.pool.feature.HoneycombConnectionPoolLRU;


public class HoneycombConnectionPool implements HoneycombConnectionPoolFeature{

    private int maxPoolSize;
    
    private long maxIdleTime;

    private HoneycombConnection[] pools;

    private AtomicInteger poolIndex;

    private LinkedBlockingQueue<HoneycombConnection> workQueue;
    
    private LinkedBlockingDeque<HoneycombConnection> idleQueue;
    
    private LinkedBlockingQueue<HoneycombConnection> freezeQueue;
    
    private Thread cleaner;
    
    private Thread lru;

    public HoneycombConnectionPool(int maxPoolSize, long maxIdleTime) {
        pools = new HoneycombConnection[this.maxPoolSize = maxPoolSize];
        idleQueue = new LinkedBlockingDeque<HoneycombConnection>();
        freezeQueue = new LinkedBlockingQueue<HoneycombConnection>();
        workQueue = new LinkedBlockingQueue<HoneycombConnection>();
        poolIndex = new AtomicInteger(-1);
        cleaner = new HoneycombConnectionPoolCleaner(this, this.maxIdleTime = maxIdleTime, 1000 * 5l);
        lru = new HoneycombConnectionPoolLRU(this, 1000 * 5l);
    }

    public Integer applyIndex() {
        if(poolIndex.get() < maxPoolSize) {
            Integer index = poolIndex.incrementAndGet();
            if(index < maxPoolSize) {
                return index;
            }
        }
        return null;
    }

    public boolean assignable() {
        return ! idleQueue.isEmpty();
    }
    
    public boolean actionable() {
        return ! freezeQueue.isEmpty();
    }
    
    public HoneycombConnection getIdleConnection(long wait) {
        try {
            while(wait > 0) {
                long beginPollNanoTime = System.nanoTime();
                HoneycombConnection nc = idleQueue.poll(wait, TimeUnit.MILLISECONDS);
                if(nc != null) {
                    if(nc.isClosed() && nc.switchOccupied() && working(nc)) {
                        return nc;
                    }
                }
                long timeConsuming = (System.nanoTime() - beginPollNanoTime) / (1000 * 1000);
                wait -= timeConsuming;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
        }
        throw new RuntimeException("获取连接超时");
    }
    
    public HoneycombConnection getFreezeConnection() {
        HoneycombConnection cn = freezeQueue.poll();
        if(cn != null) working(cn);
        return cn;
    }

    public HoneycombConnection putOccupiedConnection(HoneycombConnection nc, Integer id) {
        nc.switchOccupied();
        nc.setIndex(id);
        pools[id] = nc;
        idleQueue.remove(nc);
        workQueue.add(nc);
        return nc;
    }

    public HoneycombConnection putLeisureConnection(HoneycombConnection nc, Integer id) {
        nc.switchIdle();
        nc.setIndex(id);
        pools[id] = nc;
        idleQueue.add(nc);
        workQueue.remove(nc);
        return nc;
    }

    public boolean recycle(HoneycombConnection nc) {
        workQueue.remove(nc);
        idleQueue.addFirst(nc);
        return true;
    }
    
    public boolean working(HoneycombConnection nc) {
        return workQueue.add(nc);
    }
    
    public boolean freeze(HoneycombConnection nc) {
        return freezeQueue.add(nc) && idleQueue.remove(nc);
    }

    public HoneycombConnection[] getPools() {
        return pools;
    }

    public void setPools(HoneycombConnection[] pools) {
        this.pools = pools;
    }

    public AtomicInteger getPoolIndex() {
        return poolIndex;
    }

    public void setPoolIndex(AtomicInteger poolIndex) {
        this.poolIndex = poolIndex;
    }
    
    @Override
    public void enableCleaner() {
        cleaner.start();
    }

    @Override
    public void enableMonitor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enableLRU() {
        lru.start();
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public LinkedBlockingDeque<HoneycombConnection> getIdleQueue() {
        return idleQueue;
    }

    public void setIdleQueue(LinkedBlockingDeque<HoneycombConnection> idleQueue) {
        this.idleQueue = idleQueue;
    }

    public Thread getCleaner() {
        return cleaner;
    }

    public void setCleaner(Thread cleaner) {
        this.cleaner = cleaner;
    }

    public LinkedBlockingQueue<HoneycombConnection> getFreezeQueue() {
        return freezeQueue;
    }

    public void setFreezeQueue(LinkedBlockingQueue<HoneycombConnection> freezeQueue) {
        this.freezeQueue = freezeQueue;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public Thread getLru() {
        return lru;
    }

    public void setLru(Thread lru) {
        this.lru = lru;
    }

    public LinkedBlockingQueue<HoneycombConnection> getWorkQueue() {
        return workQueue;
    }

    public void setWorkQueue(LinkedBlockingQueue<HoneycombConnection> workQueue) {
        this.workQueue = workQueue;
    }
    
}
