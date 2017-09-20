# 0.4.3

### Bug Fixes

* Fixed issue #17 - ConcurrentUnit.await(long, TimeUnit) not passing through TimeUnit

# 0.4.2

### New Features

* Added `Waiter.rethrow` method

# 0.4.1

### Bug Fixes

* Waiter no longer checks that `await` calls are made from the main test thread, since JUnit may construct and run the test from different threads

# 0.4.0

### Improvements

* Replaced internal usage of thread interrrupts with a circuit
* Removed expectResume APIs
* Added support for TimeUnits

# 0.3.4

### Bug Fixes

* Always reset remaining resumes on interrupt

# 0.3.3

### Bug Fixes

* Fixed issue #4 - Use JUnit style argument ordering for for assert equals methods.
* Fixed issue #5 - Unit tests passing after single failed test

# 0.3.2

### Changes

* Add better failure error messages