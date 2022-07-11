package com.scorpio.rest.schedule;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author guosk
 *
 * create at 2022年7月4日, alpha-zurich-rest
 */
@Component
public class Tasks {

  @Scheduled(cron = "0/3 * * * * ?")
  public void run1() {
    System.out.println("run1");
    try {
      TimeUnit.SECONDS.sleep(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Scheduled(cron = "0/3 * * * * ?")
  public void run2() {
    System.out.println("run2");
    try {
      TimeUnit.SECONDS.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Scheduled(cron = "0/3 * * * * ?")
  public void run3() {
    System.out.println("run3");
    try {
      TimeUnit.SECONDS.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Scheduled(cron = "0/3 * * * * ?")
  public void run4() {
    System.out.println("run4");
    System.currentTimeMillis();
  }

  @Scheduled(cron = "0/3 * * * * ?")
  public void run5() {
    System.out.println("run5");
    System.currentTimeMillis();
  }
  
  @Scheduled(fixedRateString = "3000")
  public void run6() {
    System.out.println("run6");
    System.currentTimeMillis();
  }

}
