package org.nico.honeycomb.connection.pool.feature;

import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;

public abstract class AbstractFeature{

    protected boolean enable = false;
    
    protected long interval = 20 * 1000;
    
    public AbstractFeature(boolean enable, long interval) {
        this.enable = enable;
        if(interval >= 1 * 1000) this.interval = interval;
    }
    
    public abstract void doing(HoneycombConnectionPool pool);

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    
}
