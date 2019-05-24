package org.nico.honeycomb.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;

public class HoneycombDataSource extends HoneycombWrapperDatasource{

    private HoneycombConnectionPool pool;

    private volatile boolean initialStarted;

    private volatile boolean initialFinished;

    static final Lock INITIAL_LOCK = new ReentrantLock();

    static final Condition INITIAL_CONDITION = INITIAL_LOCK.newCondition();
    
    static final long serialVersionUID = 616240872756692735L;
    
    @Override
    public Connection getConnection() throws SQLException {
        try {
            init();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        HoneycombConnection cn = null;
        Integer index = null;
        if(pool.assignable()) {
            cn = pool.getIdleConnection();
        }else if(pool.actionable()) {
            cn = pool.getFreezeConnection();
        }else if((index =  pool.applyIndex()) != null) {
            cn = pool.putOccupiedConnection(createNativeConnection(pool), index);
        }
        
        if(cn == null) {
            cn = pool.getIdleConnection();
        }else if(cn.isClosedActive()) {
            cn.setConnection(super.getConnection());
            return cn;
        }
        return cn;
    }

    private void init() throws ClassNotFoundException, SQLException {
        //阻塞其他线程初始化操作，等待初始化完成
        if(initialStarted || ! (initialStarted = ! initialStarted)) {
            if(! initialFinished) {
                try {
                    INITIAL_LOCK.lock();
                    INITIAL_CONDITION.await();
                } catch (InterruptedException e) {
                } finally {
                    INITIAL_LOCK.unlock();
                }
            }
            return;
        }
        
        //config参数校验
        config.assertSelf();
        
        Class.forName(getDriver());
        
        //实例化线程池
        pool = new HoneycombConnectionPool(config);
        
        //初始化最小连接
        Integer index = null;
        for(int i = 0; i < config.getInitialPoolSize(); i ++) {
            if((index =  pool.applyIndex()) != null) {
                pool.putLeisureConnection(createNativeConnection(pool), index);
            }
        }
        
        //触发特性
        pool.touchFeatures();
        
        //完成初始化并唤醒其他阻塞
        initialFinished = true;
        try {
            INITIAL_LOCK.lock();
            INITIAL_CONDITION.signalAll();
        }catch(Exception e) {
        }finally {
            INITIAL_LOCK.unlock();
        }
    }

    public HoneycombConnectionPool getPool() {
        return pool;
    }

    public void setPool(HoneycombConnectionPool pool) {
        this.pool = pool;
    }

    public HoneycombConnection createNativeConnection(HoneycombConnectionPool pool) throws SQLException {
        return new HoneycombConnection(super.getConnection(), pool);
    }

}
