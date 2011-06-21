package org.concurrentunit;

import java.util.concurrent.TimeoutException;

import org.concurrentunit.testng.ConcurrentTestCase;
import org.testng.annotations.Test;

/**
 * Tests {@link ConcurrentTestCase}.
 */
public class ConcurrentTestCaseTest extends ConcurrentTestCase {
  /**
   * Should throw an exception.
   */
  @Test
  public void waitShouldSupportResume() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        resume();
      }
    }).start();
    threadWait();
  }

  /**
   * Should throw an exception.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void waitShouldSupportExceptions() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        try {
          throw new IllegalArgumentException();
        } catch (Exception e) {
          threadFail(e);
        }
      }
    }).start();
    threadWait();
  }

  /**
   * Should throw an assertion error.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void waitShouldSupportAssertionErrors() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        threadAssertTrue(false);
      }
    }).start();
    threadWait(0);
  }

  /**
   * Should timeout.
   * 
   * @throws Throwable
   */
  @Test(expectedExceptions = TimeoutException.class)
  public void waitShouldSupportTimeouts() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        threadAssertTrue(true);
      }
    }).start();
    threadWait(500);
  }

  /**
   * Should timeout.
   * 
   * @throws Throwable
   */
  @Test(expectedExceptions = TimeoutException.class)
  public void sleepShouldSupportTimeouts() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
      }
    }).start();
    sleep(500);
  }

  /**
   * Should support wake.
   * 
   * @throws Throwable
   */
  @Test
  public void sleepShouldSupportResume() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        resume();
      }
    }).start();
    sleep(500);
  }

  /**
   * Should support assertion errors.
   * 
   * @throws Throwable
   */
  @Test(expectedExceptions = AssertionError.class)
  public void sleepShouldSupportAssertionErrors() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        threadAssertTrue(false);
      }
    }).start();
    sleep(500);
  }

  /**
   * Ensures that waiting for multiple resumes works as expected.
   * 
   * @throws Throwable
   */
  @Test
  public void shouldSupportMultipleResumes() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        for (int i = 0; i < 5; i++)
          resume();
      }
    }).start();
    threadWait(500, 5);
  }

  @Test
  public void shouldSupportThreadWait0WithResumeCount() throws Throwable {
    new Thread(new Runnable() {
      public void run() {
        for (int i = 0; i < 5; i++)
          resume();
      }
    }).start();
    threadWait(0, 5);
  }
}
