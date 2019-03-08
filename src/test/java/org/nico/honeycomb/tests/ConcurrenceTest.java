package org.nico.honeycomb.tests;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ConcurrenceTest {

    @Test
    public void test1() throws InterruptedException {
        final LinkedBlockingQueue<Object> link = new LinkedBlockingQueue<Object>();

        new Thread() {
            @Override
            public void run() {
                System.out.println("link开始poll");
                Object o = null;
                try {
                    o = link.poll(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("link.get：" + o);
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                synchronized (link) {
                    link.stream().forEach(System.out::println);
                    System.out.println("ok");
                }
            }
        }.start();
        
        Thread.sleep(Long.MAX_VALUE);
    }
}
