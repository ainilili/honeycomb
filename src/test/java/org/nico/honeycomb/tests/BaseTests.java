package org.nico.honeycomb.tests;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.Test;
import org.nico.honeycomb.connection.pool.feature.CleanerFeature;
import org.nico.honeycomb.datasource.HoneycombDataSource;

import com.alibaba.druid.pool.DruidDataSource;

public class BaseTests {

    @Test
    public void testHoneycomb() throws SQLException, InterruptedException{
        HoneycombDataSource dataSource = new HoneycombDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&useSSL=false&transformedBitIsBoolean=true&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Asia/Shanghai");
        dataSource.setUser("root");
        dataSource.setPassword("root");
        dataSource.setDriver("com.mysql.cj.jdbc.Driver");
        dataSource.setMaxPoolSize(300);
        dataSource.setInitialPoolSize(0);
        dataSource.setMaxWaitTime(Long.MAX_VALUE);
        dataSource.setMinPoolSize(0);
        dataSource.setMaxIdleTime(5000);
        dataSource.addFeature(new CleanerFeature(true, 5 * 1000));

        test(dataSource, 1000 * 60);
    }
    
    @Test
    public void testDruid() throws SQLException, InterruptedException{
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&useSSL=false&transformedBitIsBoolean=true&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setMaxActive(300);
        dataSource.setInitialSize(0);
        dataSource.setMaxWait(Long.MAX_VALUE);
        dataSource.setMinIdle(100);
        dataSource.setTimeBetweenEvictionRunsMillis(1000 * 5);

        test(dataSource, 1000 * 60);
    }
    
    

    static volatile boolean s = true;
    static volatile boolean b = true;
    static ThreadPoolExecutor tpe = new ThreadPoolExecutor(200, 200, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    public static void test(DataSource dataSource, long howlong) throws SQLException, InterruptedException {
        Random random = new Random();
        final AtomicInteger atomicInteger = new AtomicInteger();

        new Thread() {
            @Override
            public void run() {
                while(s) {
                    BaseTests.sleep(random.nextInt(1));   
                    tpe.execute(() -> {
                        Connection connection = null;
                        try {
                            connection = dataSource.getConnection();
                            Statement s = connection.createStatement();
                            s.executeUpdate("INSERT INTO `test`.`testpool` VALUES ()");
                            atomicInteger.incrementAndGet();
                        }catch(Exception e) {
                        }finally {
                            if(connection != null)
                                try {
                                    connection.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                        }
                    });

                    BaseTests.sleep(random.nextInt(2));
                }
            }
        }.start();


        new Thread() {
            @Override
            public void run() {
                BaseTests.sleep(howlong);
                s = false;

                while(tpe.getActiveCount() != 0) {
                    BaseTests.sleep(1000);
                }

                System.out.println(atomicInteger.get());
                b = false;
            }
        }.start();

        sleep(10000000);
        tpe.shutdown();
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
    }

}
