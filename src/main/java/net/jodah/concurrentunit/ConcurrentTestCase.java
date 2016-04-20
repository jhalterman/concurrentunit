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
   * @see Waiter#rethrow(Throwable)
   */
  public void rethrow(Throwable reason) {
    waiter.rethrow(reason);
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
