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
import org.nico.honeycomb.connection.pool.HoneycombConnectionPool;
import org.nico.honeycomb.datasource.HoneycombDataSource;

public class NicoPoolCleanerTests {

    static ThreadPoolExecutor tpe = new ThreadPoolExecutor(200, 2000, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    
    @Test
    public void testConcurrence() throws SQLException, InterruptedException{
        HoneycombDataSource dataSource = new HoneycombDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&useSSL=false&transformedBitIsBoolean=true&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        dataSource.setMaxPoolSize(300);
        dataSource.setInitialPoolSize(0);
        dataSource.setMaxWaitTime(Long.MAX_VALUE);
        dataSource.setMinPoolSize(0);
        dataSource.setMaxIdleTime(5000);
        dataSource.enableLRU(false);
        
//        test(dataSource, 10000);
        test(dataSource);
    }
    
    static boolean s = true;
    
    public static void test(HoneycombDataSource dataSource) throws SQLException, InterruptedException {
        Random random = new Random();
        
        
        new Thread() {
            @Override
            public void run() {
                while(s) {
                    try {
                        Thread.sleep(random.nextInt(2));   
                    }catch(Exception e) {}
                    tpe.execute(() -> {
                        HoneycombConnection connection = null;
                        try {
                            connection = (HoneycombConnection) dataSource.getConnection();
                            Statement s = connection.createStatement();
                            ResultSet rs = s.executeQuery("select * from test limit 0,1");
                            rs.next();
                            try {
                                Thread.sleep(random.nextInt(5) + 5);   
                            }catch(Exception e) {}
                        }catch(Exception e) {
//                            e.printStackTrace();
                        }finally {
                            if(connection != null) connection.close();
                        }
                    });
                }
                System.out.println("结束添加");
            }
        }.start();
       
        
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    HoneycombConnectionPool pool = dataSource.getPool();
                    System.out.println(">> working：" + pool.getWorkQueue().size());
                    System.out.println(">> idle：" + pool.getIdleQueue().size());
                    System.out.println(">> freeze：" + pool.getFreezeQueue().size());
                }
            }
        }.start();
        
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000 * 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    s = false;
                }
            }
        }.start();
        
//        try {
//            Thread.sleep(5 * 1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        
        
//        for(int i = 0; i < 88; i ++) {
//            tpe.execute(() -> {
//                HoneycombConnection connection = null;
//                try {
//                    connection = (HoneycombConnection) dataSource.getConnection();
//                    Statement s = connection.createStatement();
//                    ResultSet rs = s.executeQuery("select * from test limit 0,1");
//                    rs.next();
//                    System.out.println("连接ID " + connection.getIndex());
//                    
//                }catch(Exception e) {
//                }finally {
//                    if(connection != null) connection.close();
//                }
//            });
//        }
//        
        
        
        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tpe.shutdown();
    }
    
}
