package net.jodah.concurrentunit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hamcrest.Matcher;

/**
 * Convenience support class, wrapping a {@link Waiter}.
 * 
 * @author Jonathan Halterman
 */
public abstract class ConcurrentTestCase {
  private final Waiter waiter = new Waiter();

  /**
   * @see Waiter#assertEquals(Object, Object)
   */
  public void threadAssertEquals(Object expected, Object actual) {
    waiter.assertEquals(expected, actual);
  }

  /**
   * @see Waiter#assertTrue(boolean)
   */
  public void threadAssertFalse(boolean b) {
    waiter.assertFalse(b);
  }

  /**
   * @see Waiter#assertNotNull(Object)
   */
  public void threadAssertNotNull(Object object) {
    waiter.assertNotNull(object);
  }

  /**
   * @see Waiter#assertNull(Object)
   */
  public void threadAssertNull(Object x) {
    waiter.assertNull(x);
  }

  /**
   * @see Waiter#assertTrue(boolean)
   */
  public void threadAssertTrue(boolean b) {
    waiter.assertTrue(b);
  }

  /**
   * @see Waiter#assertThat(Object, Matcher)
   */
  public <T> void threadAssertThat(T actual, Matcher<? super T> matcher) {
    waiter.assertThat(actual, matcher);
  }

  /**
   * @see Waiter#fail()
   */
  public void threadFail() {
    threadFail(new AssertionError());
  }

  /**
   * @see Waiter#fail(String)
   */
  public void threadFail(String reason) {
    threadFail(new AssertionError(reason));
  }

  /**
   * @see Waiter#fail(Throwable)
   */
  public void threadFail(Throwable reason) {
    waiter.fail(reason);
  }

  /**
   * @see Waiter#await()
   */
  protected void await() throws TimeoutException {
    waiter.await();
  }

  /**
   * @see Waiter#await(long)
   */
  protected void await(long delay) throws TimeoutException {
    waiter.await(delay);
  }

  /**
   * @see Waiter#await(long, int)
   */
  protected void await(long delay, int expectedResumes) throws TimeoutException {
    waiter.await(delay, expectedResumes);
  }

  /**
   * @see Waiter#await(long, TimeUnit)
   */
  protected void await(long delay, TimeUnit timeUnit) throws TimeoutException {
    waiter.await(delay);
  }

  /**
   * @see Waiter#wait(long, TimeUnit, int)
   */
  protected void await(long delay, TimeUnit timeUnit, int expectedResumes) throws TimeoutException {
    waiter.await(delay, timeUnit, expectedResumes);
  }

  /**
   * @see Waiter#resume()
   */
  protected void resume() {
    waiter.resume();
  }
}
