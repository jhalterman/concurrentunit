/*
 * Copyright 2010-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
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
  private volatile Throwable failure;

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
   * Asserts that {@code actual} satisfies the condition specified by {@code matcher}.
   * 
   * @throws AssertionError when the assertion fails
   */
  public <T> void assertThat(T actual, org.hamcrest.Matcher<? super T> matcher) {
    try {
      org.hamcrest.MatcherAssert.assertThat(actual, matcher);
    } catch (AssertionError e) {
      fail(e);
    }
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
        Throwable f = failure;
        failure = null;
        sneakyThrow(f);
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
   * {@code reason} as an {@code AssertionError} in the main test thread and in the current thread.
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

  /**
   * Rethrows the {@code failure} in the main test thread and in the current thread. Differs from
   * {@link #fail(Throwable)} which wraps a failure in an AssertionError before throwing.
   * 
   * @throws Throwable the {@code failure}
   */
  public void rethrow(Throwable failure) {
    this.failure = failure;
    circuit.close();
    sneakyThrow(failure);
  }

  private static void sneakyThrow(Throwable t) {
    Waiter.<Error>sneakyThrow2(t);
  }
  
  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void sneakyThrow2(Throwable t) throws T {
    throw (T) t;
  }
  
  private String format(Object expected, Object actual) {
    return "expected:<" + expected + "> but was:<" + actual + ">";
  }
}
