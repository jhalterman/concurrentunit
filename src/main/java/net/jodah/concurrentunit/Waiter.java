package net.jodah.concurrentunit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import net.jodah.concurrentunit.internal.ReentrantCircuit;

/**
 * Waits on a test, carrying out assertions, until being resumed.
 * 
 * @author Jonathan Halterman
 */
public class Waiter {
  private static final String TIMEOUT_MESSAGE = "Test timed out while waiting for an expected result";
  private AtomicInteger remainingResumes = new AtomicInteger(0);
  private final ReentrantCircuit circuit = new ReentrantCircuit();
  private volatile AssertionError failure;

  /**
   * Creates a new Waiter.
   */
  public Waiter() {
    circuit.open();
  }

  /**
   * Asserts that the {@code expected} values equals the {@code actual} value
   * 
   * @throws AssertionError when the assertion fails
   */
  public void assertEquals(Object expected, Object actual) {
    if (expected == null && actual == null)
      return;
    if (expected != null && expected.equals(actual))
      return;
    fail(format(expected, actual));
  }

  /**
   * Asserts that the {@code condition} is false.
   * 
   * @throws AssertionError when the assertion fails
   */
  public void assertFalse(boolean condition) {
    if (condition)
      fail("expected false");
  }

  /**
   * Asserts that the {@code object} is not null.
   * 
   * @throws AssertionError when the assertion fails
   */
  public void assertNotNull(Object object) {
    if (object == null)
      fail("expected not null");
  }

  /**
   * Asserts that the {@code object} is null.
   * 
   * @throws AssertionError when the assertion fails
   */
  public void assertNull(Object object) {
    if (object != null)
      fail(format("null", object));
  }

  /**
   * Asserts that the {@code condition} is true.
   * 
   * @throws AssertionError when the assertion fails
   */
  public void assertTrue(boolean condition) {
    if (!condition)
      fail("expected true");
  }

  /**
   * Waits until {@link #resume()} is called the expected number of times, or the test is failed.
   * 
   * @throws TimeoutException if the operation times out while waiting
   * @throws AssertionError if any assertion fails while waiting
   */
  public void await() throws TimeoutException {
    await(0, TimeUnit.MILLISECONDS, 1);
  }

  /**
   * Waits until the {@code delay} has elapsed, {@link #resume()} is called the expected number of times, or the test is
   * failed.
   * 
   * @param delay Delay to wait in milliseconds
   * @throws TimeoutException if the operation times out while waiting
   * @throws AssertionError if any assertion fails while waiting
   */
  public void await(long delay) throws TimeoutException {
    await(delay, TimeUnit.MILLISECONDS, 1);
  }

  /**
   * Waits until the {@code delay} has elapsed, {@link #resume()} is called the expected number of times, or the test is
   * failed.
   * 
   * @param delay Delay to wait for
   * @param timeUnit TimeUnit to delay for
   * @throws TimeoutException if the operation times out while waiting
   * @throws AssertionError if any assertion fails while waiting
   */
  public void await(long delay, TimeUnit timeUnit) throws TimeoutException {
    await(delay, timeUnit, 1);
  }

  /**
   * Waits until the {@code delay} has elapsed, {@link #resume()} is called {@code expectedResumes} times, or the test
   * is failed.
   * 
   * @param delay Delay to wait for in milliseconds
   * @param expectedResumes Number of times {@link #resume()} is expected to be called before the awaiting thread is
   *          resumed
   * @throws TimeoutException if the operation times out while waiting
   * @throws AssertionError if any assertion fails while waiting
   */
  public void await(long delay, int expectedResumes) throws TimeoutException {
    await(delay, TimeUnit.MILLISECONDS, expectedResumes);
  }

  /**
   * Waits until the {@code delay} has elapsed, {@link #resume()} is called {@code expectedResumes} times, or the test
   * is failed.
   * 
   * @param delay Delay to wait for
   * @param timeUnit TimeUnit to delay for
   * @param expectedResumes Number of times {@link #resume()} is expected to be called before the awaiting thread is
   *          resumed
   * @throws TimeoutException if the operation times out while waiting
   * @throws AssertionError if any assertion fails while waiting
   */
  public void await(long delay, TimeUnit timeUnit, int expectedResumes) throws TimeoutException {
    try {
      if (failure == null) {
        synchronized (this) {
          int remaining = remainingResumes.addAndGet(expectedResumes);
          if (remaining > 0)
            circuit.open();
        }

        if (delay == 0)
          circuit.await();
        else if (!circuit.await(delay, timeUnit))
          throw new TimeoutException(TIMEOUT_MESSAGE);
      }
    } catch (InterruptedException e) {
    } finally {
      remainingResumes.set(0);
      circuit.open();
      if (failure != null) {
        AssertionError f = failure;
        failure = null;
        throw f;
      }
    }
  }

  /**
   * Resumes the waiter when the expected number of {@link #resume()} calls have occurred.
   */
  public synchronized void resume() {
    if (remainingResumes.decrementAndGet() <= 0)
      circuit.close();
  }

  /**
   * Fails the current test.
   * 
   * @throws AssertionError
   */
  public void fail() {
    fail(new AssertionError());
  }

  /**
   * Fails the current test for the given {@code reason}.
   * 
   * @throws AssertionError
   */
  public void fail(String reason) {
    fail(new AssertionError(reason));
  }

  /**
   * Fails the current test with the given {@code reason}, sets the number of expected resumes to 0, and throws the
   * {@code reason} in the current thread and the main test thread.
   * 
   * @throws AssertionError wrapping the {@code reason}
   */
  public void fail(Throwable reason) {
    AssertionError ae = null;
    if (reason instanceof AssertionError)
      ae = (AssertionError) reason;
    else {
      ae = new AssertionError();
      ae.initCause(reason);
    }

    failure = ae;
    circuit.close();
    throw ae;
  }

  private String format(Object expected, Object actual) {
    return "expected:<" + expected + "> but was:<" + actual + ">";
  }
}
