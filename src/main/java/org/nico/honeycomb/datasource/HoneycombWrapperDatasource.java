package org.nico.honeycomb.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.nico.honeycomb.connection.pool.feature.AbstractFeature;

public class HoneycombWrapperDatasource implements DataSource{

    protected PrintWriter printWriter;
    
    protected HoneycombDatasourceConfig config;
    
    public HoneycombWrapperDatasource() {
        this.config = new HoneycombDatasourceConfig();
    }
    
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return printWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.printWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(config.getUrl(), username, password);
    }

    public int getInitialPoolSize() {
        return config.getInitialPoolSize();
    }

    public void setInitialPoolSize(int initialPoolSize) {
        config.setInitialPoolSize(initialPoolSize);
    }

    public int getMaxPoolSize() {
        return config.getMaxPoolSize();
    }

    public void setMaxPoolSize(int maxPoolSize) {
        config.setMaxPoolSize(maxPoolSize);
    }

    public int getMinPoolSize() {
        return config.getMinPoolSize();
    }

    public void setMinPoolSize(int minPoolSize) {
        config.setMinPoolSize(minPoolSize);
    }

    public long getMaxWaitTime() {
        return config.getMaxWaitTime();
    }

    public void setMaxWaitTime(long maxWaitTime) {
        config.setMaxWaitTime(maxWaitTime);
    }

    public long getMaxIdleTime() {
        return config.getMaxIdleTime();
    }

    public void setMaxIdleTime(long maxIdleTime) {
        config.setMaxIdleTime(maxIdleTime);
    }

    public String getUrl() {
        return config.getUrl();
    }

    public void setUrl(String url) {
        config.setUrl(url);
    }

    public String getUser() {
        return config.getUser();
    }

    public void setUser(String user) {
        config.setUser(user);
    }

    public String getPassword() {
        return config.getPassword();
    }

    public void setPassword(String password) {
        config.setPassword(password);
    }

    public String getDriver() {
        return config.getDriver();
    }

    public void setDriver(String driver) {
        config.setDriver(driver);
    }
    
    public AbstractFeature addFeature(AbstractFeature feature) {
        config.getFeatures().add(feature);
        return feature;
    }
    
}
