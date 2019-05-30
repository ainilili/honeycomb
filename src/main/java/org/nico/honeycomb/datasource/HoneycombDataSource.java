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
        	//初始化连接池
            init();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        HoneycombConnection cn = null;
        Integer index = null;
        
        if(pool.assignable()) {
        	//空闲池可分配，从空闲池取出
            cn = pool.getIdleConnection();
        }else if(pool.actionable()) {
        	//回收池可分配，从回收池取出
            cn = pool.getFreezeConnection();
        }else if((index =  pool.applyIndex()) != null) {
        	//如果连接数未满，创建新的物理连接
            cn = pool.putOccupiedConnection(createNativeConnection(pool), index);
        }
        
        if(cn == null) {
        	//如果无法获取连接，阻塞等待空闲池连接
            cn = pool.getIdleConnection();
        }
        
        if(cn.isClosedActive()) {
        	//如果物理连接关闭，则获取新的连接
            cn.setConnection(super.getConnection());
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
