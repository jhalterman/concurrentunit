package org.concurrentunit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrent test case.
 * 
 * <p>
 * Call {@link #sleep(long)}, {@link #sleep(long, int)}, {@link #threadWait(long)} or
 * {@link #threadWait(long, int)} from the main unit test thread to wait for some other thread to
 * perform assertions. These operations will block until {@link #resume()} is called, the operation
 * times out, or a threadAssert call fails.
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
 * public void assertAndResume() throws Throwable {
 *   new Thread(new Runnable() {
 *     public void run() {
 *       threadAssertTrue(true);
 *       resume();
 *     }
 *   }).start();
 *   
 *   sleep(500);
 * }
 * </pre>
 * 
 * @author Jonathan Halterman
 */
public abstract class ConcurrentTestCase {
  private static final String TIMEOUT_MESSAGE = "Test timed out while waiting for an expected result";
  private final Thread mainThread;
  private AtomicInteger sleepCount;
  private Throwable failure;

  /**
   * Creates a new ConcurrentTestCase object.
   */
  public ConcurrentTestCase() {
    mainThread = Thread.currentThread();
  }

  /**
   * Wait out termination of a thread pool or fail doing so. Waits 2500 ms for executor termination.
   * 
   * @param executor
   */
  public void joinPool(ExecutorService executor, long waitDuration) {
    try {
      executor.shutdown();
      assertTrue(executor.awaitTermination(2500, MILLISECONDS));
    } catch (SecurityException ok) {
    } catch (InterruptedException ie) {
      fail("Unexpected InterruptedException");
    }
  }

  /**
   * @see org.junit.Assert#assertEquals(Object, Object)
   */
  public void threadAssertEquals(Object x, Object y) {
    try {
      assertEquals(x, y);
    } catch (AssertionError e) {
      threadFail(e);
    }
  }

  /**
   * @see org.junit.Assert#assertFalse(boolean)
   */
  public void threadAssertFalse(boolean b) {
    try {
      assertFalse(b);
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
   * @see org.junit.Assert#assertNull(Object)
   */
  public void threadAssertNull(Object x) {
    try {
      assertNull(x);
    } catch (AssertionError e) {
      threadFail(e);
    }
  }

  /**
   * @see org.junit.Assert#assertTrue(boolean)
   */
  public void threadAssertTrue(boolean b) {
    try {
      assertTrue(b);
    } catch (AssertionError e) {
      threadFail(e);
    }
  }

  /**
   * Fails the current test for the given reason.
   */
  public void threadFail(String reason) {
    threadFail(new AssertionError(reason));
  }

  /**
   * Fails the current test with the given Throwable.
   */
  public void threadFail(Throwable e) {
    failure = e;
    resume(mainThread);
  }

  /**
   * Resumes the main test thread.
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
    if (thread != mainThread || sleepCount == null || sleepCount.decrementAndGet() == 0)
      thread.interrupt();
  }

  /**
   * Sleeps until the {@code sleepDuration} has elapsed, {@link #resume()} is called, or the test is
   * failed.
   * 
   * @param sleepDuration
   * @throws TimeoutException if the sleep operation times out while waiting for a result
   * @throws Throwable the last reported test failure
   */
  protected void sleep(long sleepDuration) throws Throwable {
    try {
      Thread.sleep(sleepDuration);
      throw new TimeoutException(TIMEOUT_MESSAGE);
    } catch (InterruptedException ignored) {
    } finally {
      if (failure != null)
        throw failure;
    }
  }

  /**
   * Sleeps until the {@code sleepDuration} has elapsed, {@link #resume()} is called
   * {@code resumeThreshold} times, or the test is failed.
   * 
   * @param sleepDuration Duration to sleep
   * @param resumeThreshold Number of times resume must be called before sleep is interrupted
   * @throws IllegalStateException if called from outside the main test thread
   * @throws TimeoutException if the sleep operation times out while waiting for a result
   * @throws Throwable the last reported test failure
   */
  protected void sleep(long sleepDuration, int resumeThreshold) throws Throwable {
    if (Thread.currentThread() != mainThread)
      throw new IllegalStateException("Must be called from within the main test thread");

    sleepCount = new AtomicInteger(resumeThreshold);
    sleep(sleepDuration);
    sleepCount = null;
  }

  /**
   * Alias for {@link #resume()} for use within the context of a Thread instance.
   */
  protected void threadResume() {
    resume(mainThread);
  }

  /**
   * Waits until {@link #resume()} is called, or the test is failed.
   * 
   * @throws IllegalStateException if called from outside the main test thread
   * @throws Throwable the last reported test failure
   */
  protected void threadWait() throws Throwable {
    if (Thread.currentThread() != mainThread)
      throw new IllegalStateException("Must be called from within the main test thread");

    synchronized (this) {
      while (true) {
        try {
          wait();
          throw new TimeoutException(TIMEOUT_MESSAGE);
        } catch (InterruptedException e) {
          if (failure != null)
            throw failure;
          break;
        }
      }
    }
  }

  /**
   * Waits until the {@code waitDuration} has elapsed, {@link #resume()} is called, or the test is
   * failed. Delegates to {@link #sleep(long)} to avoid spurious wakeups.
   * 
   * @see #sleep(long)
   */
  protected void threadWait(long waitDuration) throws Throwable {
    if (waitDuration == 0)
      threadWait();
    else
      sleep(waitDuration);
  }

  /**
   * Waits until the {@code waitDuration} has elapsed, {@link #resume()} is called
   * {@code resumeThreshold} times, or the test is failed. Delegates to {@link #sleep(long, int)} to
   * avoid spurious wakeups.
   * 
   * @see #sleep(long, int)
   */
  protected void threadWait(long waitDuration, int resumeThreshold) throws Throwable {
    sleep(waitDuration, resumeThreshold);
  }
}
