package org.jodah.concurrentunit;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Waits on a test, carrying out assertions, until being resumed.
 * 
 * @author Jonathan Halterman
 */
public class Waiter {
  private static final String TIMEOUT_MESSAGE = "Test timed out while waiting for an expected result";
  private final Thread mainThread;
  protected AtomicInteger waitCount;
  protected Throwable failure;

  /**
   * Creates a new Waiter.
   */
  public Waiter() {
    mainThread = Thread.currentThread();
  }

  public void assertEquals(Object actual, Object expected) {
    if (expected == null && actual == null)
      return;
    if (expected != null && expected.equals(actual))
      return;
    fail(format(expected, actual));
  }

  /**
   * Asserts that the {@code condition} is false.
   */
  public void assertFalse(boolean condition) {
    if (condition)
      fail();
  }

  /**
   * Asserts that the {@code object} is not null.
   */
  public void assertNotNull(Object object) {
    if (object == null)
      fail();
  }

  /**
   * Asserts that the {@code object} is null.
   */
  public void assertNull(Object object) {
    if (object != null)
      fail();
  }

  /**
   * Asserts that the {@code condition} is true.
   */
  public void assertTrue(boolean condition) {
    if (!condition)
      fail();
  }

  /**
   * Waits until {@link #resume()} is called, or the test is failed.
   * 
   * @throws IllegalStateException if called from outside the main test thread
   * @throws Throwable the last reported test failure
   */
  public void await() throws Throwable {
    if (Thread.currentThread() != mainThread)
      throw new IllegalStateException("Must be called from within the main test thread");

    synchronized (this) {
      while (true) {
        try {
          wait();
          throw new TimeoutException(TIMEOUT_MESSAGE);
        } catch (InterruptedException e) {
          if (failure != null) {
            Throwable f = failure;
            failure = null;
            throw f;
          }
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
  public void await(long waitDuration) throws Throwable {
    if (waitDuration == 0)
      await();
    else
      sleep(waitDuration);
  }

  /**
   * Waits until the {@code waitDuration} has elapsed, {@link #resume()} is called
   * {@code resumeThreshold} times, or the test is failed. Delegates to {@link #sleep(long, int)} to
   * avoid spurious wakeups.
   * 
   * @param waitDuration Duration to wait
   * @param resumeThreshold Number of times {@link #resume()} must be called before wait is
   *          interrupted
   * @throws IllegalStateException if called from outside the main test thread
   * @throws TimeoutException if the operation times out while waiting for a result
   * @throws Throwable the last reported test failure
   */
  public void await(long waitDuration, int resumeThreshold) throws Throwable {
    if (waitDuration == 0) {
      waitCount = new AtomicInteger(resumeThreshold);
      await();
      waitCount = null;
    } else
      sleep(waitDuration, resumeThreshold);
  }

  /**
   * Fails the current test.
   */
  public void fail() {
    fail(new AssertionError());
  }

  /**
   * Fails the current test for the given {@code reason}.
   */
  public void fail(String reason) {
    fail(new AssertionError(reason));
  }

  /**
   * Fails the current test with the given {@code reason}.
   */
  public void fail(Throwable reason) {
    failure = reason;
    resume(mainThread);
  }

  /**
   * Decrements the wait count, resuming the test thread when {@link #resume()} calls have exceeded
   * the resume threshold given when the test was made to wait.
   */
  public void resume() {
    resume(mainThread);
  }

  /**
   * Resumes the waiter if {@code thread} is not the mainThread, the waitCount is null or the
   * decremented waitCount is 0.
   * 
   * <p>
   * Note: This method is likely not very useful to call directly since a concurrent run of a test
   * case resulting in the need to resume from a separate thread would yield no correlation between
   * the initiating thread and the thread where the resume call takes place.
   * 
   * @param thread Thread to resume
   */
  public void resume(Thread thread) {
    if (thread != mainThread || waitCount == null || waitCount.decrementAndGet() == 0)
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
  public void sleep(long sleepDuration) throws Throwable {
    try {
      Thread.sleep(sleepDuration);
      throw new TimeoutException(TIMEOUT_MESSAGE);
    } catch (InterruptedException ignored) {
    } finally {
      if (failure != null) {
        Throwable f = failure;
        failure = null;
        throw f;
      }
    }
  }

  /**
   * Sleeps until the {@code sleepDuration} has elapsed, {@link #resume()} is called
   * {@code resumeThreshold} times, or the test is failed.
   * 
   * @see #await(long, int)
   */
  public void sleep(long sleepDuration, int resumeThreshold) throws Throwable {
    if (Thread.currentThread() != mainThread)
      throw new IllegalStateException("Must be called from within the main test thread");

    waitCount = new AtomicInteger(resumeThreshold);
    sleep(sleepDuration);
    waitCount = null;
  }

  private String format(Object actual, Object expected) {
    return "expected:<" + expected + "> but was:<" + actual + ">";
  }
}
