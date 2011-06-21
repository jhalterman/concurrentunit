package org.concurrentunit;

import java.util.concurrent.TimeoutException;

import org.testng.annotations.Test;

/**
 * ConcurrentUnit examples.
 */

public class Examples extends ConcurrentTestCase {
  /**
   * Block the main thread while waiting for an assertion in a worker thread and resume after
   * completion:
   * 
   * @throws Throwable
   */
  
  public void shouldSucceed() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        threadAssertTrue(true);
        resume();
      }
    }).start();
    threadWait(100);
  }

  /**
   * Handle a failed assertion:
   * 
   * @throws Throwable
   */
  @Test(expectedExceptions = AssertionError.class)
  public void shouldFail() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        threadAssertTrue(false);
      }
    }).start();
    threadWait(0);
  }

  /**
   * The main thread will throw a TimeoutException if resume is not called before the wait duration
   * is exceeded.
   * 
   * @throws Throwable
   */
  @Test(expectedExceptions = TimeoutException.class)
  public void sleepShouldSupportTimeouts() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
      }
    }).start();
    threadWait(1);
  }

  /**
   * The main thread can also be told to wait for n number of resume calls.
   * 
   * @throws Throwable
   */
  @Test
  public void shouldSupportMultipleResumes() throws Throwable {
    final int resumeThreshold = 5;
    new Thread(new Runnable() {
      public void run() {
        for (int i = 0; i < resumeThreshold; i++)
          resume();
      }
    }).start();
    threadWait(500, resumeThreshold);
  }
}
