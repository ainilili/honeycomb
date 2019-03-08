package org.nico.honeycomb.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.MysqlDataSource;

public class HoneycombDataSource extends MysqlDataSource implements DataSource{

    private String url;

    private String username;

    private String password;

    private String driver;

    private int initialPoolSize;

    private int maxPoolSize;

    private int minPoolSize;

    private long maxWaitTime;

    private long maxIdleTime;
    
    private boolean enableLRU = true;
    
    private boolean enableCleaner = true;
    
    private HoneycombConnectionPool pool;

    private volatile boolean initialStarted;

    private volatile boolean initialFinished;

    static final Lock INITIAL_LOCK = new ReentrantLock();

    static final Condition INITIAL_CONDITION = INITIAL_LOCK.newCondition();
    
    private Logger logger = LoggerFactory.getLogger(HoneycombDataSource.class);
    
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
            cn = pool.getIdleConnection(maxWaitTime);
        }else if(pool.actionable()) {
            cn = pool.getFreezeConnection();
        }else if((index =  pool.applyIndex()) != null) {
            cn = pool.putOccupiedConnection(createNativeConnection(pool), index);
        }
        
        if(cn == null) {
            cn = pool.getIdleConnection(maxWaitTime);
        }else if(cn.isClosedActive()) {
//            logger.debug(cn.getIndex() + " connection is already closed, create new !");
            cn.setConnection(super.getConnection());
            return cn;
        }
        return cn;
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
        
        super.setUrl(url);
        super.setUser(username);
        super.setPassword(password);

        pool = new HoneycombConnectionPool(maxPoolSize, maxIdleTime);
        
        if(enableCleaner) {
            pool.enableCleaner();
        }
        if(enableLRU) {
            pool.enableLRU();    
        }

        if(initialPoolSize > maxPoolSize) initialPoolSize = maxPoolSize;

        Integer index = null;
        for(int i = 0; i < initialPoolSize; i ++) {
            if((index =  pool.applyIndex()) != null) {
                pool.putLeisureConnection(createNativeConnection(pool), index);
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
        return new HoneycombConnection(super.getConnection(), pool);
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

    public void enableLRU(boolean enableLRU) {
        this.enableLRU = enableLRU;
    }

    public void enableCleaner(boolean enableCleaner) {
        this.enableCleaner = enableCleaner;
    }

}
