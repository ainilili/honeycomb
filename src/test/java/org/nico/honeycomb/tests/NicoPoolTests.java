package org.nico.honeycomb.tests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.junit.Test;
import org.nico.honeycomb.connection.HoneycombConnection;
import org.nico.honeycomb.connection.pool.feature.CleanerFeature;
import org.nico.honeycomb.datasource.HoneycombDataSource;

public class NicoPoolTests {

    static ThreadPoolExecutor tpe = new ThreadPoolExecutor(1000, 1000, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    
    @Test
    public void testConcurrence() throws SQLException, InterruptedException{
        long start = System.currentTimeMillis();
        HoneycombDataSource dataSource = new HoneycombDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&useSSL=false&transformedBitIsBoolean=true&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Asia/Shanghai");
        dataSource.setUser("root");
        dataSource.setPassword("root");
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        dataSource.setMaxPoolSize(50);
        dataSource.setInitialPoolSize(10);
        dataSource.setMinPoolSize(10);
        dataSource.setMaxWaitTime(60 * 1000);
        dataSource.setMaxIdleTime(10 * 1000);
        dataSource.addFeature(new CleanerFeature(true, 5 * 1000));
        
        test(dataSource, 10000);
        System.out.println(System.currentTimeMillis() - start + " ms");
    }
    
    public static void test(DataSource dataSource, int count) throws SQLException, InterruptedException {
        CountDownLatch cdl = new CountDownLatch(count);
        for(int i = 0; i < count; i ++) {
            tpe.execute(() -> {
                try {
                    HoneycombConnection connection = (HoneycombConnection) dataSource.getConnection();
                    Statement s = connection.createStatement();
                    s.executeQuery("select * from test limit 1");
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
    
    public static void test(DataSource dataSource) throws SQLException, InterruptedException {
        Random random = new Random();
        while(true) {
            try {
                Thread.sleep(random.nextInt(10) + 10);   
            }catch(Exception e) {}

            tpe.execute(() -> {
                try {
                    HoneycombConnection connection = (HoneycombConnection) dataSource.getConnection();
                    Statement s = connection.createStatement();
                    ResultSet rs = s.executeQuery("select * from test limit 0,1");
                    rs.next();
//                    System.out.println("连接ID " + connection.getIndex());
                    try {
                        Thread.sleep(random.nextInt(100) + 10);   
                    }catch(Exception e) {
                    	e.printStackTrace();
                    }
                    connection.close();
                }catch(Exception e) {
                	e.printStackTrace();
                }finally {
                }
            });
        }
    }
    
}
