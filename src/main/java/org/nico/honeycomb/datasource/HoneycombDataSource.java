package org.nico.honeycomb.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;

public class HoneycombDataSource extends HoneycombDataSourceWrapper implements DataSource{

    private String url;
    
    private String username;
    
    private String password;
    
    private String driver;
    
    private int initalPoolSize;
    
    private int maxPoolSize;
    
    private int minPoolSize;
    
    private long maxWaitTime;
    
    private long maxIdleTime;
    
    private HoneycombConnectionPool pool;
    
    private volatile boolean isInit;
    private volatile boolean inited;
    
    
    final Lock initLock = new ReentrantLock();
    final Condition initCondition = initLock.newCondition();
    
    @Override
    public Connection getConnection() throws SQLException {
        try {
            initLock.lock();
            init();
            if(! inited) {
                initCondition.await();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            initLock.unlock();
        }
        
        HoneycombConnection cn = null;
        Integer index = null;
        if(pool.hasLeisure()) {
            cn = pool.getConnection(maxWaitTime);
        }else if((index =  pool.applyIndex()) != null) {
            cn = pool.addUsedConnection(createNativeConnection(pool), index);
        }else {
            cn = pool.getConnection(maxWaitTime);
        }
        
        if(cn.isClosed()) {
            return pool.putUsedConnection(createNativeConnection(pool), cn.getIndex());
        }
        return cn;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }
    
    private void init() throws ClassNotFoundException, SQLException {
        if(isInit) {
            return;
        }
        isInit = true;
        
        Class.forName(driver);
        
        pool = new HoneycombConnectionPool(maxPoolSize);
        
        if(initalPoolSize > maxPoolSize) {
            initalPoolSize = maxPoolSize;
        }
        
        for(int i = 0; i < initalPoolSize; i ++) {
            pool.addUnUsedConnection(createNativeConnection(pool));
        }
        inited = true;
        initCondition.signalAll();
    }
    
    public HoneycombConnectionPool getPool() {
        return pool;
    }

    public void setPool(HoneycombConnectionPool pool) {
        this.pool = pool;
    }

    public HoneycombConnection createNativeConnection(HoneycombConnectionPool pool) throws SQLException {
        return new HoneycombConnection(DriverManager.getConnection(url, username, password), pool);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public int getInitalPoolSize() {
        return initalPoolSize;
    }

    public void setInitalPoolSize(int initalPoolSize) {
        this.initalPoolSize = initalPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public long getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(long maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

}
