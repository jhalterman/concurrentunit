package org.jodah.concurrentunit;

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
  public void threadAssertEquals(Object x, Object y) {
    waiter.assertEquals(x, y);
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
  protected void await() throws Throwable {
    waiter.await();
  }

  /**
   * @see Waiter#await(long)
   */
  protected void await(long waitDuration) throws Throwable {
    waiter.await(waitDuration);
  }

  /**
   * @see Waiter#wait(long, int)
   */
  protected void await(long waitDuration, int resumeThreshold) throws Throwable {
    waiter.await(waitDuration, resumeThreshold);
  }

  /**
   * @see Waiter#resume()
   */
  protected void resume() {
    waiter.resume();
  }

  /**
   * @see Waiter#resume(Thread)
   */
  protected void resume(Thread thread) {
    waiter.resume(thread);
  }

  /**
   * @see Waiter#sleep(long)
   */
  protected void sleep(long sleepDuration) throws Throwable {
    waiter.sleep(sleepDuration);
  }

  /**
   * @see Waiter#sleep(long, int)
   */
  protected void sleep(long sleepDuration, int resumeThreshold) throws Throwable {
    waiter.sleep(sleepDuration, resumeThreshold);
  }
}
