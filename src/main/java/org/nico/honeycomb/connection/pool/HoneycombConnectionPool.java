package org.nico.honeycomb.connection.pool;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.feature.AbstractFeature;
import org.nico.honeycomb.datasource.HoneycombDatasourceConfig;


public class HoneycombConnectionPool implements FeatureTrigger{

    private HoneycombConnection[] pools;

    private AtomicInteger poolIndex;

    private LinkedBlockingQueue<HoneycombConnection> workQueue;
    
    private LinkedBlockingDeque<HoneycombConnection> idleQueue;
    
    private LinkedBlockingQueue<HoneycombConnection> freezeQueue;
    
    private HoneycombDatasourceConfig config;
    
    public HoneycombConnectionPool(HoneycombDatasourceConfig config) {
        this.config = config;
        this.pools = new HoneycombConnection[config.getMaxPoolSize()];
        this.idleQueue = new LinkedBlockingDeque<HoneycombConnection>();
        this.freezeQueue = new LinkedBlockingQueue<HoneycombConnection>();
        this.workQueue = new LinkedBlockingQueue<HoneycombConnection>();
        this.poolIndex = new AtomicInteger(-1);
    }

    public Integer applyIndex() {
        if(poolIndex.get() < config.getMaxPoolSize()) {
            Integer index = poolIndex.incrementAndGet();
            if(index < config.getMaxPoolSize()) {
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
    
    public HoneycombConnection getIdleConnection() {
        try {
            long waitTime = config.getMaxWaitTime();
            while(waitTime > 0) {
                long beginPollNanoTime = System.nanoTime();
                HoneycombConnection nc = idleQueue.poll(waitTime, TimeUnit.MILLISECONDS);
                if(nc != null) {
                    if(nc.isClosed() && nc.switchOccupied() && working(nc)) {
                        return nc;
                    }else {
                        nc.switchIdle();
                        idleQueue.addLast(nc);
                    }
                }
                long timeConsuming = (System.nanoTime() - beginPollNanoTime) / (1000 * 1000);
                waitTime -= timeConsuming;
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
        if(workQueue.remove(nc)) {
            idleQueue.addFirst(nc);
            return true;
        }
        return false;
    }
    
    public boolean working(HoneycombConnection nc) {
        return workQueue.add(nc);
    }
    
    public boolean freeze(HoneycombConnection nc) {
        return idleQueue.remove(nc) && freezeQueue.add(nc);
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
    
    public LinkedBlockingDeque<HoneycombConnection> getIdleQueue() {
        return idleQueue;
    }

    public void setIdleQueue(LinkedBlockingDeque<HoneycombConnection> idleQueue) {
        this.idleQueue = idleQueue;
    }

    public LinkedBlockingQueue<HoneycombConnection> getFreezeQueue() {
        return freezeQueue;
    }

    public void setFreezeQueue(LinkedBlockingQueue<HoneycombConnection> freezeQueue) {
        this.freezeQueue = freezeQueue;
    }

    public HoneycombDatasourceConfig getConfig() {
        return config;
    }

    public void setConfig(HoneycombDatasourceConfig config) {
        this.config = config;
    }

    public LinkedBlockingQueue<HoneycombConnection> getWorkQueue() {
        return workQueue;
    }

    public void setWorkQueue(LinkedBlockingQueue<HoneycombConnection> workQueue) {
        this.workQueue = workQueue;
    }

    @Override
    public void touchFeatures() {
        if(! config.getFeatures().isEmpty()) {
            config.getFeatures().stream().filter(AbstractFeature::isEnable).forEach(e -> e.doing(this));
        }
    }
    
}
