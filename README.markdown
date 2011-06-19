# ConcurrentUnit 0.1.0

A simple concurrent JUnit test case extension.

## Introduction

ConcurrentUnit allows you to write test cases capable of performing concurrent assertions or waiting for expected operations across multiple threads, with failures being properly reported back to the main test thread.

## Usage

* Use `threadWait` or `sleep` calls to block the main test thread while waiting for worker threads to perform assertions. 
* Use `threadAssert` calls from any thread to perform concurrent assertions. Assertion failures will result in the main thread being interrupted and the failure thrown.
* Once expected assertions are completed, use a `resume` call to unblock the main thread.

If a blocking operation times out before all expected `resume` calls occur, the test is failed with a TimeoutException.

## Examples

Block the main thread while waiting for an assertion in a worker thread and resume after completion:

    @Test
    public void shouldSucceed() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
                threadAssertTrue(true);
                resume();
            }
        }).start();
        threadWait(100);
    }

Handle a failed assertion:

    @Test(expected = AssertionError.class)
    public void shouldFail() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
                threadAssertTrue(false);
            }
        }).start();
        threadWait(0);
    }

TimeoutException occurs if resume is not called before the wait duration is exceeded:

    @Test(expected = TimeoutException.class)
    public void sleepShouldSupportTimeouts() throws Throwable {
        new Thread(new Runnable() {
            public void run() {
            }
        }).start();
        threadWait(1);
    }

Block the main thread while waiting for n number of resume calls:

    @Test
    public void shouldSupportMultipleResumes() throws Throwable {
        final int resumeThreshold = 5;
        new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < resumeThreshold; i++)
                    resume();
            }
        }).start();
        threadWait(500, resumeThreshold);
    }

## References

Thanks to the JSR-166 TCK authors for the initial inspiration.

## License

Copyright 2010-2011 Jonathan Halterman - Released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
