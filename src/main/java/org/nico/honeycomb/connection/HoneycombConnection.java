package org.nico.honeycomb.connection;

import java.sql.Connection;
import java.sql.SQLException;

import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;
import org.nico.honeycomb.consts.ConnectionStatus;
import org.nico.honeycomb.utils.UnsafeUtils;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class HoneycombConnection extends HoneycombConnectionBridge implements HoneycombConnectionSwitcher{

    private Integer index;
    
    private HoneycombConnectionPool pool;
    
    private volatile ConnectionStatus status;
    
    private static Unsafe unsafe = UnsafeUtils.UNSAFE;
    
    private static long statusOffset = UnsafeUtils.getFieldOffset(HoneycombConnection.class, "status");
    
    private HoneycombConnection(Connection connection) {
        super(connection);
    }
    
    public HoneycombConnection(Connection connection, HoneycombConnectionPool pool) {
        this(connection);
        this.pool = pool;
    }
    
    public HoneycombConnection(Connection connection, HoneycombConnectionPool pool, int index) {
        this(connection, pool);
        this.index = index;
    }

    @Override
    public void close() {
        if(switchLeisure()) {
            pool.recycle(this);   
        }
    }

    @Override
    public boolean isClosed() throws SQLException {
        return status == ConnectionStatus.LEISURE || status == ConnectionStatus.RECYCLED;
    }

    @Override
    public boolean switchLeisure() {
        return unsafe.compareAndSwapObject(this, statusOffset, status, ConnectionStatus.LEISURE);
    }

    @Override
    public boolean switchRecycled() {
        return unsafe.compareAndSwapObject(this, statusOffset, status, ConnectionStatus.RECYCLED);
    }

    @Override
    public boolean switchOccupied() {
        return unsafe.compareAndSwapObject(this, statusOffset, status, ConnectionStatus.OCCUPIED);
    }

    @Override
    public boolean leisured() {
        return status == ConnectionStatus.LEISURE;
    }

    @Override
    public boolean recycled() {
        return status == ConnectionStatus.RECYCLED;
    }

    @Override
    public boolean occupied() {
        return status == ConnectionStatus.OCCUPIED;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

}
