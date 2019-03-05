package org.nico.honeycomb.connection;

import java.sql.Connection;
import java.sql.SQLException;

import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;
import org.nico.honeycomb.consts.ConnectionStatus;
import org.nico.honeycomb.utils.TimeUtils;
import org.nico.honeycomb.utils.UnsafeUtils;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class HoneycombConnection extends HoneycombConnectionDecorator implements HoneycombConnectionSwitcher{

    private Integer index;
    
    private HoneycombConnectionPool pool;

    private long idleStartTime;
    
    private long usageTime;
    
    private long usageCount;
    
    private volatile ConnectionStatus status;
    
    private static Unsafe unsafe = UnsafeUtils.UNSAFE;
    
    private static long statusOffset = UnsafeUtils.getFieldOffset(HoneycombConnection.class, "status");
    
    private HoneycombConnection(Connection connection) throws SQLException {
        super(connection);
        this.usageTime = TimeUtils.getRealTime();
        if(connection.isClosed()) throw new SQLException();
    }
    
    public HoneycombConnection(Connection connection, HoneycombConnectionPool pool) throws SQLException {
        this(connection);
        this.pool = pool;
    }
    
    public HoneycombConnection(Connection connection, HoneycombConnectionPool pool, int index) throws SQLException {
        this(connection, pool);
        this.index = index;
    }

    @Override
    public void close() {
        if(switchIdle()) {
            pool.recycle(this);   
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return status == ConnectionStatus.IDLE;
    }
    
    public boolean isClosedActive() throws SQLException {
        return connection.isClosed();
    }
    
    public void closeActive() throws SQLException {
        connection.close();
    }

    @Override
    public boolean switchIdle() {
        return unsafe.compareAndSwapObject(this, statusOffset, status, ConnectionStatus.IDLE) && flushIdleStartTime();
    }

    @Override
    public boolean switchOccupied() {
        return unsafe.compareAndSwapObject(this, statusOffset, status, ConnectionStatus.OCCUPIED) && flushUsageCount();
    }

    @Override
    public boolean idle() {
        return status == ConnectionStatus.IDLE;
    }

    @Override
    public boolean occupied() {
        return status == ConnectionStatus.OCCUPIED;
    }
    
    public double usageFrequency() {
        return (TimeUtils.getRealTime() - usageTime) / (double) usageCount;
    }
    
    public double idleTime() {
        return TimeUtils.getRealTime() - idleStartTime;
    }
    
    public boolean flushIdleStartTime() {
        idleStartTime = TimeUtils.getRealTime();
        return true;
    }
    
    public boolean flushUsageCount() {
        usageCount += 1;
        return true;
    }

    public long getIdleStartTime() {
        return idleStartTime;
    }

    public void setIdleStartTime(long idleStartTime) {
        this.idleStartTime = idleStartTime;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public long getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(long usageTime) {
        this.usageTime = usageTime;
    }

    public long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(long usageCount) {
        this.usageCount = usageCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((index == null) ? 0 : index.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HoneycombConnection other = (HoneycombConnection) obj;
        if (index == null) {
            if (other.index != null)
                return false;
        } else if (!index.equals(other.index))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return index + "|" + status + "|" + usageFrequency();
    }

}
