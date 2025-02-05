/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * SingleFlight implements call deduplication for equal keys.
 *
 * <p>Example:
 *
 * <pre>
 * public Result expensiveOperation(final Parameters parameters) throws Exception {
 *     return singleFlight.execute(parameters, new Callable&lt;Result&gt;() {
 *         &#64;Override
 *         public Result call() {
 *             return expensiveOperationImpl(parameters);
 *         }
 *     });
 * }
 *
 * private Result expensiveOperationImpl(Parameters parameters) {
 *     // the real implementation
 * }
 * </pre>
 */
public class SingleFlight {

  private final ConcurrentMap<Object, Call> calls = new ConcurrentHashMap<>();

  /**
   * Execute a {@link Callable} if no other calls for the same {@code key} are currently running.
   * Concurrent calls for the same {@code key} result in one caller invoking the {@link Callable}
   * and sharing the result with the other callers.
   *
   * <p>The result of an invocation is not cached, only concurrent calls share the same result.
   *
   * @param key A unique identification of the method call. The {@code key} must be uniquely
   *     identifiable by it's {@link Object#hashCode()} and {@link Object#equals(Object)} methods.
   * @param callable The {@link Callable} where the result can be obtained from.
   * @return The result of invoking the {@link Callable}.
   * @throws Exception The {@link Exception} which was thrown by the {@link Callable}. Alternatively
   *     a {@link InterruptedException} can be thrown if the executing {@link Thread} was
   *     interrupted while waiting for the result.
   * @param key Object
   * @param callable Callable
   * @return V V
   */
  @SuppressWarnings("unchecked")
  public <V> V execute(Object key, Callable<V> callable) throws Exception {
    Call<V> call = calls.get(key);
    if (call == null) {
      call = new Call<>();
      Call<V> other = calls.putIfAbsent(key, call);
      if (other == null) {
        try {
          return call.exec(callable);
        } finally {
          calls.remove(key);
        }
      } else {
        call = other;
      }
    }
    return call.await();
  }

  private static class Call<V> {

    private final Object lock = new Object();
    private boolean finished;
    private V result;
    private Exception exc;

    void finished(V result, Exception exc) {
      synchronized (lock) {
        this.finished = true;
        this.result = result;
        this.exc = exc;
        lock.notifyAll();
      }
    }

    V await() throws Exception {
      synchronized (lock) {
        while (!finished) {
          lock.wait();
        }
        if (exc != null) {
          throw exc;
        }
        return result;
      }
    }

    V exec(Callable<V> callable) throws Exception {
      V result = null;
      Exception exc = null;
      try {
        result = callable.call();
        return result;
      } catch (Exception e) {
        exc = e;
        throw e;
      } finally {
        finished(result, exc);
      }
    }
  }
}
