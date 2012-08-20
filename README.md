# ConcurrentUnit 0.2.0

A simple tool for performing assertions across threads in JUnit and TestNG.

## Introduction

ConcurrentUnit allows you to write tests capable of performing assertions or waiting for expected operations across multiple threads, with failures being properly reported back to the main test thread. If an assertion fails, your test fails, regardless of which thread the assertion came from.

## Setup

Add ConcurrentUnit as a Maven dependency for either JUnit or TestNG (whatever you use):

```xml
<dependency>
  <groupId>org.jodah</groupId>
  <artifactId>concurrentunit-junit</artifactId>
  <version>0.2.0</version>
</dependency>

<dependency>
  <groupId>org.jodah</groupId>
  <artifactId>concurrentunit-testng</artifactId>
  <version>0.2.0</version>
</dependency>
```

## Usage

* Extend `ConcurrentTestCase`
* Use `threadWait` or `sleep` calls to block the main test thread while waiting for other threads to perform assertions. 
* Use `threadAssert` calls from any thread to perform assertions. Assertion failures will result in the main thread being interrupted and the failure thrown.
* Once expected assertions are completed, use a `resume` call to unblock the main thread.

If a blocking operation times out before all expected `resume` calls occur, the test is failed with a TimeoutException.

## Examples

Block the main thread while waiting for an assertion in a worker thread and resume after completion:

```java
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
```

Handle a failed assertion:

```java
@Test(expected = AssertionError.class)
public void shouldFail() throws Throwable {
  new Thread(new Runnable() {
    public void run() {
      threadAssertTrue(false);
    }
  }).start();
  
  threadWait(0);
}
```

TimeoutException occurs if resume is not called before the wait duration is exceeded:

```java
@Test(expected = TimeoutException.class)
public void sleepShouldSupportTimeouts() throws Throwable {
  new Thread(new Runnable() {
    public void run() {
    }
  }).start();
  
  threadWait(1);
}
```

Block the main thread while waiting for n number of resume calls:

```java
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
```

## References

Thanks to the JSR-166 TCK authors for the initial inspiration.

## License

Copyright 2010-2011 Jonathan Halterman - Released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
