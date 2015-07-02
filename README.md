# ConcurrentUnit
[![Build Status](https://travis-ci.org/jhalterman/concurrentunit.svg)](https://travis-ci.org/jhalterman/concurrentunit)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.jodah/concurrentunit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.jodah/concurrentunit) 

A simple, zero-dependency toolkit for testing multi-threaded code.

## Introduction

ConcurrentUnit was created to help developers test multi-threaded code. It allows you to perform assertions and wait for operations across multiple threads, with failures being properly reported back to the main test thread. If an assertion fails, your test fails, regardless of which thread the assertion came from.

## Usage

1. Create a `Waiter`
2. Use `Waiter.await` to block the main test thread while waiting for other threads to perform assertions.
3. Use the `Waiter.assert` calls from any thread to perform assertions. 
4. Once expected assertions are completed, use `Waiter.resume` call to unblock the main thread.

Optionally:

* Use `Waiter.expectResumes` to indicate the number of `resume` calls the waiter should expect. This is useful when `resume` may be called by some thread prior to `await`.

When your test runs, assertion failures will result in the main thread being interrupted and the failure thrown. If an `await` call times out before all expected `resume` calls occur, the test is failed with a `TimeoutException`.

## Examples

Perform an assertion from a worker thread while blocking the main thread until `resume` is called:

```java
@Test
public void shouldWaitForResume() throws Throwable {
  final Waiter waiter = new Waiter();

  // Start worker thread that performs an assertion after some delay, then resumes the waiter
  new Thread(new Runnable() {
    public void run() {
      doSomeWork();
      waiter.assertTrue(true);
      waiter.resume();
    }
  }).start();
  
  // Waits for resume to be called
  waiter.await(1000);
}
```

Multiple threads can be used along with any number of expected `resume` calls:

```java
@Test
public void shouldWaitForResumes() throws Throwable {
  final Waiter waiter = new Waiter();
  int expectedResumes = 5;
  waiter.expectResumes(expectedResumes);

  for (int i = 0; i < expectedResumes; i++) {
    new Thread(new Runnable() {
      public void run() {
        waiter.assertTrue(true);
        waiter.resume();
      }
    }).start();
  }
  
  waiter.await(1000);
}
```

Failed assertions from a worker thread are thrown by the main test thread as expected:

```java
@Test(expected = AssertionError.class)
public void shouldFail() throws Throwable {
  final Waiter waiter = new Waiter();

  new Thread(new Runnable() {
    public void run() {
      delayFor(100);
      waiter.assertTrue(false);
    }
  }).start();
  
  waiter.await();
}
```

TimeoutException is thrown if `resume` is not called before the await time is exceeded:

```java
@Test(expected = TimeoutException.class)
public void shouldTimeout() throws Throwable {
  new Waiter().await(1);
}
```

### Alternatively

As a more concise alternative to using the `Waiter` class, you can extend the `ConcurrentTestCase`:

```java
class SomeTest extends ConcurrentTestCase {
	@Test
	public void shouldSucceed() throws Throwable {
	  new Thread(new Runnable() {
	    public void run() {
	      delayFor(100);
	      threadAssertTrue(true);
	      resume();
	    }
	  }).start();
	  
	  await(1000);
	}
}
```

### Other Examples

More example usages can be found in the [WaiterTest](https://github.com/jhalterman/concurrentunit/blob/master/src/test/java/net/jodah/concurrentunit/WaiterTest.java) or in the following projects:

* [Lyra](https://github.com/jhalterman/lyra/tree/master/src/test/java/net/jodah/lyra/internal/util/concurrent)
* [Recurrent](https://github.com/jhalterman/recurrent/blob/master/src/test/java/net/jodah/recurrent/RecurrentTest.java)
* [ExpiringMap](https://github.com/jhalterman/expiringmap/blob/master/src/test/java/net/jodah/expiringmap/ExpiringMapTest.java)
* [Copycat](https://github.com/kuujo/copycat)

## Docs

JavaDocs are available [here](https://jhalterman.github.com/concurrentunit/javadoc).

## License

Copyright 2010-2015 Jonathan Halterman - Released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
