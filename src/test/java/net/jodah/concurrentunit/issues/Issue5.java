package net.jodah.concurrentunit.issues;

import net.jodah.concurrentunit.Waiter;

import org.testng.annotations.Test;

/**
 * https://github.com/jhalterman/concurrentunit/issues/5
 */
@Test
public class Issue5 {
  @Test(expectedExceptions = AssertionError.class)
  public void testOne() throws Throwable {
    performTest();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void testTwo() throws Throwable {
    performTest();
  }

  void performTest() throws Throwable {
    final Waiter waiter = new Waiter();
    new Thread(new Runnable() {
      @Override
      public void run() {
        waiter.assertTrue(false);
      }
    }).start();
    waiter.await(100);
  }
}
