package org.nico.honeycomb.tests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.Test;
import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.datasource.HoneycombDataSource;

public class NicoPoolTests {

    static ThreadPoolExecutor tpe = new ThreadPoolExecutor(1000, 1000, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    
    @Test
    public void testConcurrence() throws SQLException, InterruptedException{
        long start = System.currentTimeMillis();
        HoneycombDataSource dataSource = new HoneycombDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&useSSL=false&transformedBitIsBoolean=true&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        dataSource.setMaxPoolSize(170);
        dataSource.setInitalPoolSize(50);
        dataSource.setMaxWaitTime(Long.MAX_VALUE);
        dataSource.setMinPoolSize(100);
        
        test(dataSource, 100000);
        System.out.println(System.currentTimeMillis() - start + " ms");
    }
    
    public static void test(DataSource dataSource, int count) throws SQLException, InterruptedException {
        
        CountDownLatch cdl = new CountDownLatch(count);
        for(int i = 0; i < count; i ++) {
            tpe.execute(() -> {
                try {
                    HoneycombConnection connection = (HoneycombConnection) dataSource.getConnection();
                    Statement s = connection.createStatement();
                    ResultSet rs = s.executeQuery("select * from test limit 0,1");
                    rs.next();
//                    System.out.println("连接ID " + connection.getIndex());
                    connection.close();
                }catch(Exception e) {
                }finally {
                    cdl.countDown();
                }
            });
        }
        cdl.await();
        
        tpe.shutdown();
    }
    
}
