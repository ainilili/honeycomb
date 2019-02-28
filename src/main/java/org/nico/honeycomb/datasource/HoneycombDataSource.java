package org.nico.honeycomb.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

    private int initialPoolSize;

    private int maxPoolSize;

    private int minPoolSize;

    private long maxWaitTime;

    private long maxIdleTime;

    private HoneycombConnectionPool pool;

    private volatile boolean initialStarted;

    private volatile boolean initialFinished;

    final static Lock INITIAL_LOCK = new ReentrantLock();

    final static Condition INITIAL_CONDITION = INITIAL_LOCK.newCondition();

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
            cn = pool.getConnection(maxWaitTime);
        }else if((index =  pool.applyIndex()) != null) {
            cn = pool.putUsedConnection(createNativeConnection(pool), index);
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
        
        Class.forName(driver);

        pool = new HoneycombConnectionPool(maxPoolSize);

        if(initialPoolSize > maxPoolSize) {
            initialPoolSize = maxPoolSize;
        }

        Integer index = null;
        for(int i = 0; i < initialPoolSize; i ++) {
            if((index =  pool.applyIndex()) != null) {
                pool.putUnUsedConnection(createNativeConnection(pool), index);
            }
        }
        
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

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
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
