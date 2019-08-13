# ConcurrentUnit
[![Build Status](https://travis-ci.org/jhalterman/concurrentunit.svg)](https://travis-ci.org/jhalterman/concurrentunit)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.jodah/concurrentunit/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.jodah/concurrentunit) 
[![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![JavaDoc](http://javadoc-badge.appspot.com/net.jodah/concurrentunit.svg?label=javadoc)](https://jhalterman.github.com/concurrentunit/javadoc)

A simple, zero-dependency toolkit for testing multi-threaded code. Supports Java 1.6+.

## Introduction

ConcurrentUnit was created to help developers test multi-threaded or asynchronous code. It allows you to perform assertions and wait for operations in any thread, with failures being properly reported back to the main test thread. If an assertion fails, your test fails, regardless of which thread the assertion came from.

## Usage

1. Create a `Waiter`
2. Use `Waiter.await` to block the main test thread.
3. Use the `Waiter.assert` calls from any thread to perform assertions. 
4. Once expected assertions are completed, use `Waiter.resume` call to unblock the `await`ing thread.

When your test runs, assertion failures will result in the main thread being interrupted and the failure thrown. If an `await` call times out before all expected `resume` calls occur, the test will fail with a `TimeoutException`.

## Examples

Consider a test for a message bus that delivers messages asynchronously:

```java
@Test
public void shouldDeliverMessage() throws Throwable {
  final Waiter waiter = new Waiter();

  messageBus.registerHandler(message -> {
    // Called on separate thread
    waiter.assertEquals(message, "foo");
    waiter.resume();
  };
  
  messageBus.send("foo");
  
  // Wait for resume() to be called
  waiter.await(1000);
}
```

We can also handle wait for multiple `resume` calls:

```java
@Test
public void shouldDeliverMessages() throws Throwable {
  final Waiter waiter = new Waiter();

  messageBus.registerHandler(message -> {
    waiter.assertEquals(message, "foo");
    waiter.resume();
  };
  
  messageBus.send("foo");
  messageBus.send("foo");
  messageBus.send("foo");
  
  // Wait for resume() to be called 3 times
  waiter.await(1000, 3);
}
```

If an assertion fails in any thread, the test will fail as expected:

```java
@Test(expected = AssertionError.class)
public void shouldFail() throws Throwable {
  final Waiter waiter = new Waiter();

  new Thread(() -> {
    waiter.assertTrue(false);
  }).start();
  
  waiter.await();
}
```

TimeoutException is thrown if `resume` is not called before the `await` time is exceeded:

```java
@Test(expected = TimeoutException.class)
public void shouldTimeout() throws Throwable {
  new Waiter().await(1);
}
```

#### Alternatively

As a more concise alternative to using the `Waiter` class, you can extend the `ConcurrentTestCase`:

```java
class SomeTest extends ConcurrentTestCase {
  @Test
  public void shouldSucceed() throws Throwable {
    new Thread(() -> {
      doSomeWork();
      threadAssertTrue(true);
      resume();
    }).start();

    await(1000);
  }
}
```

#### Assertions

ConcurrentUnit's `Waiter` supports the standard assertions along with [Hamcrest Matcher](http://hamcrest.org/JavaHamcrest/javadoc/) assertions:

```java
waiter.assertEquals(expected, result);
waiter.assertThat(result, is(equalTo(expected)));
```

Since Hamcrest is an optional dependency, users need to explicitly add it to their classpath (via Maven/Gradle/etc).

#### Other Examples

More example usages can be found in the [WaiterTest](https://github.com/jhalterman/concurrentunit/blob/master/src/test/java/net/jodah/concurrentunit/WaiterTest.java) or in the following projects:

* [Atomix](https://github.com/atomix/atomix/tree/master/core/src/test/java/io/atomix)
* [Copycat](https://github.com/atomix/copycat/tree/master/server/src/test/java/io/atomix/copycat/server/state)
* [Failsafe](https://github.com/jhalterman/failsafe/blob/master/src/test/java/net/jodah/failsafe/AsyncFailsafeTest.java)
* [ExpiringMap](https://github.com/jhalterman/expiringmap/tree/master/src/test/java/net/jodah/expiringmap/functional)
* [Lyra](https://github.com/jhalterman/lyra/tree/master/src/test/java/net/jodah/lyra/internal/util/concurrent)

## Additional Notes

#### On `await` / `resume` Timing

Since it is not always possible to ensure that `resume` is called after `await` in multi-threaded tests, ConcurrentUnit allows them to be called in either order. If `resume` is called before `await`, the resume calls are recorded and `await` will return immediately if the expected number of resumes have already occurred. This ability comes with a caveat though: it is not possible to detect when additional unexpected `resume` calls are made since ConcurrentUnit allows an `await` call to follow.

## Additional Resources

- [Javadocs](https://jodah.net/concurrentunit/javadoc)
- [An article](https://jodah.net/testing-multi-threaded-code) describing the motivation for ConcurrentUnit

## License

Copyright 2011-2016 Jonathan Halterman - Released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
