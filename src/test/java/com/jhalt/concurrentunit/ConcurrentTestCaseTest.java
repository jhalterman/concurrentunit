package com.jhalt.concurrentunit;

import java.util.concurrent.TimeoutException;

import org.junit.Test;

/**
 * Tests {@link ConcurrentTestCase}.
 * 
 * @author Jonathan Halterman
 */
public class ConcurrentTestCaseTest extends ConcurrentTestCase {
    /**
     * Should throw an exception.
     */
    @Test
    public void waitShouldSupportResume() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
                resume();
            }
        }).start();

        threadWait(0);
    }

    /**
     * Should throw an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void waitShouldSupportExceptions() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
                try {
                    throw new IllegalArgumentException();
                } catch (Exception e) {
                    threadFail(e);
                }
            }
        }).start();

        threadWait(0);
    }

    /**
     * Should throw an assertion error.
     */
    @Test(expected = AssertionError.class)
    public void waitShouldSupportAssertionErrors() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
                threadAssertTrue(false);
            }
        }).start();

        threadWait(0);
    }

    /**
     * Should timeout.
     * 
     * @throws Throwable
     */
    @Test(expected = TimeoutException.class)
    public void waitShouldSupportTimeouts() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
                threadAssertTrue(true);
            }
        }).start();

        threadWait(500);
    }

    /**
     * Should timeout.
     * 
     * @throws Throwable
     */
    @Test(expected = TimeoutException.class)
    public void sleepShouldSupportTimeouts() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
            }
        }).start();

        sleep(500);
    }

    /**
     * Should support wake.
     * 
     * @throws Throwable
     */
    @Test
    public void sleepShouldSupportResume() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
                resume();
            }
        }).start();

        sleep(500);
    }

    /**
     * Should support assertion errors.
     * 
     * @throws Throwable
     */
    @Test(expected = AssertionError.class)
    public void sleepShouldSupportAssertionErrors() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
                threadAssertTrue(false);
            }
        }).start();

        sleep(500);
    }
}
