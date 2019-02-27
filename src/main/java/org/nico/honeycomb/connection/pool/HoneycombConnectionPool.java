package org.nico.honeycomb.connection.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nico.honeycomb.connection.HoneycombConnection;


public class HoneycombConnectionPool{

    private HoneycombConnection[] pools;

    private AtomicInteger poolCursor;

    private int maxSize;

    private List<Integer> leisureIds;

    final Lock addLock = new ReentrantLock();
    final Lock leisureLock = new ReentrantLock();
    
    final Condition leisureCondition = leisureLock.newCondition();  

    public HoneycombConnectionPool(int max) {
        maxSize = max;
        pools = new HoneycombConnection[max];
        leisureIds = new ArrayList<Integer>();
        poolCursor = new AtomicInteger(-1);
    }

    public Lock getLeisureLock() {
        return leisureLock;
    }

    public boolean isFull() {
        return poolCursor.get() == maxSize - 1;
    }

    public boolean hasLeisure() {
        return leisureIds.size() > 0;
    }

    public Condition getLeisureCondition() {
        return leisureCondition;
    }

    public HoneycombConnection getConnection(long wait) {
        try {
            leisureLock.lock();
            if(! leisureIds.isEmpty()) {
                Integer index = leisureIds.get(0);
                if(index != null) {
                    HoneycombConnection nc = pools[index];
                    if(nc.isClosed() && nc.switchOccupied()) {
                        leisureIds.remove(nc.getIndex());
                        return nc;
                    }
                }
            }
            long time = leisureCondition.awaitNanos(wait);
            if(time > 0) {
                return getConnection(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            leisureLock.unlock();
        }
        throw new RuntimeException("获取连接超时");
    }

    public HoneycombConnection addUsedConnection(HoneycombConnection nc) {
        addLock.lock();
        Integer id = poolCursor.incrementAndGet();
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
        Integer id = poolCursor.incrementAndGet();
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
    
   
}
