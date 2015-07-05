package net.jodah.concurrentunit;

import java.util.concurrent.TimeoutException;

import org.testng.annotations.Test;

/**
 * Tests {@link Waiter}.
 */
@Test
public class WaiterTest {
  @Test
  public void shouldSupportMultipleThreads() throws Throwable {
    final Waiter waiter = new Waiter();

    for (int i = 0; i < 5; i++)
      new Thread(new Runnable() {
        public void run() {
          waiter.assertTrue(true);
          waiter.resume();
        }
      }).start();

    waiter.await(0);
  }

  /**
   * Should throw an exception.
   */
  public void waitShouldSupportResume() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        waitForMainThread();
        w.resume();
      }
    }).start();

    w.await();
  }

  /**
   * Should throw an exception.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void waitShouldSupportFail() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        w.fail(new IllegalArgumentException());
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
        waitForMainThread();
        w.assertTrue(false);
      }
    }).start();

    w.await();
  }

  /**
   * Should timeout.
   * 
   * @throws Throwable
   */
  @Test(expectedExceptions = TimeoutException.class)
  public void waitShouldSupportTimeouts() throws Throwable {
    final Waiter w = new Waiter();
    w.await(10);
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
        waitForMainThread();
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
        waitForMainThread();
        for (int i = 0; i < 5; i++)
          w.resume();
      }
    }).start();

    w.await(0, 5);
  }

  @Test(expectedExceptions = AssertionError.class)
  public void shouldFailNullAssertionWithReason() throws Throwable {
    Waiter w = new Waiter();
    w.assertNull("test");
  }

  @Test(expectedExceptions = AssertionError.class)
  public void shouldFailNullAssertionFromWorkerThreadWithReason() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        waitForMainThread();
        w.assertNull("test");
      }
    }).start();

    w.await();
  }

  public void shouldHandleResumeThenAwait() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        w.resume();
      }
    }).start();

    Thread.sleep(400);
    w.await();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void shouldHandleFailThenAwait() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        w.fail();
      }
    }).start();

    Thread.sleep(400);
    w.await();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void shouldHandleResumeFailAwait() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        try {
          w.resume();
          w.fail();
        } catch (Throwable ignore) {
        }
      }
    }).start();

    Thread.sleep(400);
    w.await();
  }
  
  @Test(expectedExceptions = AssertionError.class)
  public void shouldHandleFailResumeAwait() throws Throwable {
    final Waiter w = new Waiter();

    new Thread(new Runnable() {
      public void run() {
        try {
          w.fail();
        } catch (Throwable ignore) {
        }
        w.resume();
      }
    }).start();

    Thread.sleep(400);
    w.await();
  }

  public void shouldSupportSuccessiveResumeAwaits() throws Throwable {
    final Waiter w = new Waiter();
    w.resume();
    w.await();
    w.resume();
    w.await();
  }

  public void shouldSupportSuccessiveResumes() throws Throwable {
    final Waiter w = new Waiter();
    w.resume();
    w.resume();
    w.resume();
    w.await();
  }

  /**
   * Waits momentarily to allow the main thread to start blocking.
   */
  private void waitForMainThread() {
    try {
      Thread.sleep(250);
    } catch (InterruptedException e) {
    }
  }
}
