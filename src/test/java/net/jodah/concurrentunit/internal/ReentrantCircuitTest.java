package net.jodah.concurrentunit.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import net.jodah.concurrentunit.Waiter;

@Test
public class ReentrantCircuitTest {
  ReentrantCircuit circuit;

  @BeforeMethod
  protected void beforeMethod() {
    circuit = new ReentrantCircuit();
  }

  public void shouldInitiallyBeClosed() {
    assertThat(circuit.isClosed()).isTrue();
  }

  public void shouldHandleOpenCloseCycles() {
    for (int i = 0; i < 3; i++) {
      circuit.open();
      circuit.close();
    }

    assertThat(circuit.isClosed()).isTrue();
  }

  public void shouldHandleRepeatedOpens() {
    for (int i = 0; i < 3; i++)
      circuit.open();

    assertThat(circuit.isClosed()).isFalse();
  }

  public void shouldHandleRepeatedClosed() {
    for (int i = 0; i < 3; i++)
      circuit.close();

    assertThat(circuit.isClosed()).isTrue();
  }

  public void shouldReturnWhenAwaitAndAlreadyClosed() throws Throwable {
    long t = System.currentTimeMillis();
    circuit.await();
    circuit.await(3, TimeUnit.MINUTES);

    // Awaits should return immediately
    assertThat(System.currentTimeMillis() - t).isLessThan(500);
  }

  public void shouldHandleSequentialWaiters() throws Throwable {
    final Waiter waiter = new Waiter();
    for (int i = 0; i < 1; i++) {
      circuit.open();

      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            circuit.await();
            waiter.resume();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }).start();

      Thread.sleep(500);
      circuit.close();
      waiter.await(500);
    }
  }

  public void shouldHandleConcurrentWaiters() throws Throwable {
    circuit.open();

    final Waiter waiter = new Waiter();
    for (int i = 0; i < 3; i++)
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            circuit.await();
            waiter.resume();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }).start();

    Thread.sleep(1000);
    circuit.close();
    waiter.await(500);
  }

  public void shouldInterruptWaiters() throws Throwable {
    circuit.open();

    final Waiter waiter = new Waiter();
    for (int i = 0; i < 3; i++)
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            circuit.await();
          } catch (InterruptedException e) {
            waiter.resume();
          }
        }
      }).start();

    Thread.sleep(300);
    circuit.interruptWaiters();
    waiter.await(500);
  }

  public void shouldNotBlockOpenWhenSyncAcquired() throws Throwable {
    circuit.open();

    final Waiter waiter = new Waiter();
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          circuit.await();
          waiter.resume();
        } catch (InterruptedException e) {
        }
      }
    }).start();

    Thread.sleep(300);
    circuit.open();
    circuit.close();
    waiter.await(500);
  }
}
