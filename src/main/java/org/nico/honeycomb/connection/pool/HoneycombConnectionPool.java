package org.nico.honeycomb.connection.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nico.honeycomb.connection.HoneycombConnection;


public class HoneycombConnectionPool{

    private HoneycombConnection[] pools;

    private AtomicInteger poolIndex;

    private int maxSize;

    private LinkedBlockingQueue<Integer> leisureIds;

    final Lock addLock = new ReentrantLock();
    final Lock leisureLock = new ReentrantLock();

    final Condition leisureCondition = leisureLock.newCondition();  

    public HoneycombConnectionPool(int max) {
        maxSize = max;
        pools = new HoneycombConnection[max];
        leisureIds = new LinkedBlockingQueue<Integer>();
        poolIndex = new AtomicInteger(-1);
    }

    public Lock getLeisureLock() {
        return leisureLock;
    }

    public Integer applyIndex() {
        if(poolIndex.get() < maxSize) {
            Integer index = poolIndex.incrementAndGet();
            if(index < maxSize) {
                return index;
            }
        }
        return null;
    }

    public boolean hasLeisure() {
        return leisureIds.size() > 0;
    }

    public Condition getLeisureCondition() {
        return leisureCondition;
    }

    public HoneycombConnection getConnection(long wait) {
        try {
            Integer index = leisureIds.poll(wait, TimeUnit.MILLISECONDS);
            if(index != null) {
                HoneycombConnection nc = pools[index];
                if(nc.isClosed() && nc.switchOccupied()) {
                    leisureIds.remove(nc.getIndex());
                    return nc;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
        }
        throw new RuntimeException("获取连接超时");
    }

    public HoneycombConnection addUsedConnection(HoneycombConnection nc, Integer id) {
        addLock.lock();
        pools[id] = nc;
        nc.switchOccupied();
        nc.setIndex(id);
        addLock.unlock();
        return nc;
    }

    public HoneycombConnection putUsedConnection(HoneycombConnection nc, Integer id) {
        addLock.lock();
        pools[id] = nc;
        nc.switchOccupied();
        nc.setIndex(id);
        leisureIds.add(id);
        addLock.unlock();
        return nc;
    }

    public HoneycombConnection addUnUsedConnection(HoneycombConnection nc) {
        addLock.lock();
        Integer id = poolIndex.incrementAndGet();
        pools[id] = nc;
        nc.switchLeisure();
        nc.setIndex(id);
        leisureIds.add(id);
        addLock.unlock();
        return nc;
    }

    public void addToLeisureIds(HoneycombConnection nc) {
        try {
            leisureLock.lock();
            leisureIds.add(nc.getIndex());
            leisureCondition.signal();   
        }catch(Exception e) {
            e.printStackTrace();
        }finally {
            leisureLock.unlock();
        }

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
