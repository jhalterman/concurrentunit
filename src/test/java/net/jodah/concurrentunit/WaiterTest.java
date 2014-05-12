package net.jodah.concurrentunit;

import java.util.concurrent.TimeoutException;

import net.jodah.concurrentunit.Waiter;

import org.testng.annotations.Test;

/**
 * Tests {@link Waiter}.
 */
@Test
public class WaiterTest {
  @Test
  public void shouldSupportMultipleThreads() throws Throwable {
    final Waiter waiter = new Waiter();
    int expectedResumes = 5;

    for (int i = 0; i < expectedResumes; i++)
      new Thread(new Runnable() {
        public void run() {
          waiter.assertTrue(true);
          waiter.resume();
        }
      }).start();
    
    waiter.await(0, expectedResumes);
  }
  
  /**
   * Should throw an exception.
   */
  public void waitShouldSupportResume() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        w.resume();
      }
    }).start();

    w.await();
  }

  /**
   * Should throw an exception.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void waitShouldSupportExceptions() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        try {
          throw new IllegalArgumentException();
        } catch (Exception e) {
          w.fail(e);
        }
      }
    }).start();

    w.await();
  }

  /**
   * Should throw an assertion error.
   */
  @Test(expectedExceptions = AssertionError.class)
  public void waitShouldSupportAssertionErrors() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        w.assertTrue(false);
      }
    }).start();

    w.await(0);
  }

  /**
   * Should timeout.
   * 
   * @throws Throwable
   */
  @Test(expectedExceptions = TimeoutException.class)
  public void waitShouldSupportTimeouts() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        w.assertTrue(true);
      }
    }).start();

    w.await(500);
  }

  /**
   * Should timeout.
   * 
   * @throws Throwable
   */
  @Test(expectedExceptions = TimeoutException.class)
  public void sleepShouldSupportTimeouts() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
      }
    }).start();

    w.sleep(10);
  }

  /**
   * Should support resume.
   * 
   * @throws Throwable
   */
  public void sleepShouldSupportResume() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        w.resume();
      }
    }).start();

    w.sleep(50000);
  }

  /**
   * Should support assertion errors.
   * 
   * @throws Throwable
   */
  @Test(expectedExceptions = AssertionError.class)
  public void sleepShouldSupportAssertionErrors() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        w.assertTrue(false);
      }
    }).start();

    w.sleep(500);
  }

  /**
   * Ensures that waiting for multiple resumes works as expected.
   * 
   * @throws Throwable
   */
  public void shouldSupportMultipleResumes() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        for (int i = 0; i < 5; i++)
          w.resume();
      }
    }).start();

    w.await(500, 5);
  }

  public void shouldSupportThreadWait0WithResumeCount() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        for (int i = 0; i < 5; i++)
          w.resume();
      }
    }).start();

    w.await(0, 5);
  }
}
