package org.nico.honeycomb.connection.pool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.nico.honeycomb.connection.HoneycombConnection;


public class HoneycombConnectionPool{

    private int maxPoolSize;

    private HoneycombConnection[] pools;

    private AtomicInteger poolIndex;

    private LinkedBlockingQueue<Integer> idleQueue;

    public HoneycombConnectionPool(int maxPoolSize) {
        pools = new HoneycombConnection[this.maxPoolSize = maxPoolSize];
        idleQueue = new LinkedBlockingQueue<Integer>();
        poolIndex = new AtomicInteger(-1);
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
        return idleQueue.size() > 0;
    }

    public HoneycombConnection getConnection(long wait) {
        try {
            while(wait > 0) {
                long beginPollNanoTime = System.nanoTime();
                Integer index = idleQueue.poll(wait, TimeUnit.MILLISECONDS);
                if(index != null) {
                    HoneycombConnection nc = pools[index];
                    if(nc.isClosed() && nc.switchOccupied()) {
                        idleQueue.remove(nc.getIndex());
                        return nc;
                    }
                }
                long timeConsuming = (System.nanoTime() - beginPollNanoTime) / 1000 * 1000 ;
                wait -= timeConsuming;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
        }
        throw new RuntimeException("获取连接超时");
    }

    public HoneycombConnection putUsedConnection(HoneycombConnection nc, Integer id) {
        nc.switchOccupied();
        nc.setIndex(id);
        pools[id] = nc;
        idleQueue.remove(id);
        return nc;
    }

    public HoneycombConnection putUnUsedConnection(HoneycombConnection nc, Integer id) {
        nc.switchLeisure();
        nc.setIndex(id);
        pools[id] = nc;
        idleQueue.add(id);
        return nc;
    }

    public void recycle(HoneycombConnection nc) {
        idleQueue.add(nc.getIndex());
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


}
