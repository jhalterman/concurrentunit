package org.concurrentunit;

import java.util.concurrent.ExecutorService;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Concurrent test case implementation.
 * 
 * <p>
 * Call {@link #threadWait(long)} or {@link #sleep(long)} from the main unit test thread to wait for
 * some other thread to perform assertions. These operations will block until {@link #resume()}, the
 * operation times out, or a threadAssert call fails.
 * 
 * <p>
 * The threadAssert methods can be used from any thread to perform concurrent assertions. Assertion
 * failures will result in the main thread being interrupted and the failure thrown.
 * 
 * <p>
 * Usage:
 * 
 * <pre>
 * @Test
 * public void sleepShouldSupportAssertionErrors() throws Throwable {
 *     new Thread(new Runnable() {
 *       public void run() {
 *           threadAssertTrue(true);
 *           resume();
 *       }
 *     }).start();
 *     threadWait(500);
 * }
 * </pre>
 * 
 * @author Jonathan Halterman
 */
public abstract class ConcurrentTestCase {
  private static final String TIMEOUT_MESSAGE = "Test timed out while waiting for an expected result";
  private final Thread mainThread;
  private AtomicInteger waitCount;
  private Throwable failure;

  /**
   * Creates a new ConcurrentTestCase object.
   */
  public ConcurrentTestCase() {
    mainThread = Thread.currentThread();
  }

  /**
   * Resumes the main thread.
   */
  protected void resume() {
    resume(mainThread);
  }

  /**
   * Resumes a waiting test case.
   * 
   * <p>
   * Note: This method is likely not very useful since a concurrent run of a test case resulting in
   * the need to resume from a separate thread would yield no correlation between the initiating
   * thread and the thread where the resume call takes place.
   * 
   * @param thread Thread to resume
   */
  protected void resume(Thread thread) {
    if (thread != mainThread || waitCount == null || waitCount.decrementAndGet() == 0)
      thread.interrupt();
  }

  /**
   * Fails the current test for the given reason.
   */
  public void threadFail(String reason) {
    threadFail(new AssertionError(reason));
  }

  /**
   * Fails the current test for the given exception.
   */
  public void threadFail(Throwable e) {
    failure = e;
    resume();
  }

  /**
   * If expression not true, set status to indicate current testcase should fail
   */
  public void threadAssertTrue(boolean b) {
    try {
      assertTrue(b);
    } catch (AssertionError e) {
      threadFail(e);
    }
  }

  /**
   * If expression not false, set status to indicate current testcase should fail
   */
  public void threadAssertFalse(boolean b) {
    try {
      assertFalse(b);
    } catch (AssertionError e) {
      threadFail(e);
    }
  }

  /**
   * If argument not null, set status to indicate current testcase should fail
   */
  public void threadAssertNull(Object x) {
    try {
      assertNull(x);
    } catch (AssertionError e) {
      threadFail(e);
    }
  }

  /**
   * @see org.junit.Assert#assertNotNull(Object)
   */
  public void threadAssertNotNull(Object object) {
    try {
      assertNotNull(object);
    } catch (AssertionError e) {
      threadFail(e);
    }
  }

  /**
   * If arguments not equal, set status to indicate current testcase should fail
   */
  public void threadAssertEquals(Object x, Object y) {
    try {
      assertEquals(x, y);
    } catch (AssertionError e) {
      threadFail(e);
    }
  }

  /**
   * Wait out termination of a thread pool or fail doing so. Waits 2500 ms for executor termination.
   * 
   * @param executor
   */
  public void joinPool(ExecutorService executor) {
    try {
      executor.shutdown();
      assertTrue(executor.awaitTermination(2500, MILLISECONDS));
    } catch (SecurityException ok) {
    } catch (InterruptedException ie) {
      fail("Unexpected InterruptedException");
    }
  }

  /**
   * Sleep until the timeout has elapsed or interrupted and throws any exception that is set by any
   * other thread running within the context of this test.
   * 
   * <p>
   * Call {@link #resume()} to interrupt the sleep.
   * 
   * <p>
   * Note: A sleep time of 0 will sleep indefinitely. This is only recommended to use if you are
   * absolutely sure that {@link #resume()} will be called by some thread.
   * 
   * @param sleepTime
   * @throws Throwable If any exception occurs while sleeping
   * @throws TimeoutException If the sleep operation times out while waiting for a result
   */
  protected void sleep(long sleepTime) throws Throwable, TimeoutException {
    try {
      Thread.sleep(sleepTime);
      throw new TimeoutException(TIMEOUT_MESSAGE);
    } catch (InterruptedException e) {
    } finally {
      if (failure != null)
        throw failure;
    }
  }

  /**
   * Waits until resume is called {@code pResumeCount} times.
   * 
   * @param waitTime Time to wait
   * @param resumeCount Number of times resume must be called before wait completes
   * @throws IllegalStateException if called from outside the main test thread
   * @throws TimeoutException if the wait operation times out while waiting for a result
   */
  protected void threadWait(long waitTime, int resumeCount) throws Throwable {
    if (Thread.currentThread() != mainThread)
      throw new IllegalStateException("Must be called from within the main test thread");

    waitCount = new AtomicInteger(resumeCount);

    threadWait(waitTime);
    waitCount = null;
  }

  /**
   * Waits till the wait time has elapsed or the test case's monitor is interrupted, and throws any
   * exception that is set by any other thread running within the context of this test.
   * 
   * <p>
   * Call {@link #finish()} to interrupt the wait.
   * 
   * <p>
   * Note: A wait time of 0 will wait indefinitely. This is only recommended to use if you are
   * absolutely sure that {@link #finish()} will be called by some thread.
   * 
   * @param waitTime Time to wait
   * @throws Throwable If any exception occurs while waiting
   * @throws TimeoutException if the wait operation times out while waiting for a result
   */
  protected void threadWait(long waitTime) throws Throwable, TimeoutException {
    synchronized (this) {
      try {
        wait(waitTime);
        throw new TimeoutException(TIMEOUT_MESSAGE);
      } catch (InterruptedException e) {
      } finally {
        if (failure != null)
          throw failure;
      }
    }
  }
}
