package org.nico.honeycomb.datasource;

import java.util.ArrayList;
import java.util.List;

import org.nico.honeycomb.connection.pool.feature.AbstractFeature;
import org.nico.honeycomb.utils.AssertUtils;

public class HoneycombDatasourceConfig {
    
    //db url
    private String url;

    //db user
    private String user;

    //db password
    private String password;

    //driver驱动
    private String driver;

    //初始化连接数，默认为2
    private int initialPoolSize = 2;

    //最大连接数，默认为10
    private int maxPoolSize = 10;

    //最小连接数，默认为2
    private int minPoolSize = 2;
    
    //获取连接时，最大等待时长，默认为60s
    private long maxWaitTime = 60 * 1000;

    //最大空闲时长，超出要被回收，默认为20s
    private long maxIdleTime = 20 * 1000;
    
    //特性列表
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

}
