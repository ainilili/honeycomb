package org.nico.honeycomb.datasource;

import java.util.ArrayList;
import java.util.List;

import org.nico.honeycomb.connection.pool.feature.AbstractFeature;
import org.nico.honeycomb.utils.AssertUtils;

public class HoneycombDatasourceConfig {

    private String url;

    private String user;

    private String password;

    private String driver;

    private int initialPoolSize = 2;

    private int maxPoolSize = 10;

    private int minPoolSize = 2;
    
    private int maxConnectTime = 10 * 1000; 

    private long maxWaitTime = 60 * 1000;

    private long maxIdleTime = 20 * 1000;
    
    private List<AbstractFeature> features;
    
    public HoneycombDatasourceConfig() {
        features = new ArrayList<AbstractFeature>(5);
    }

    public void assertSelf() {
        AssertUtils.assertBlank("url", url);
        AssertUtils.assertBlank("driver", driver);
        if(initialPoolSize > maxPoolSize) initialPoolSize = maxPoolSize;
    }

    public List<AbstractFeature> getFeatures() {
        return features;
    }

    public void setFeatures(List<AbstractFeature> features) {
        this.features = features;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public int getMaxConnectTime() {
        return maxConnectTime;
    }

    public void setMaxConnectTime(int maxConnectTime) {
        this.maxConnectTime = maxConnectTime;
    }

}
