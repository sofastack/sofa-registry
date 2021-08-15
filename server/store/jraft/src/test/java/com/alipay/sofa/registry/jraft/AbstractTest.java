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
package com.alipay.sofa.registry.jraft;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.observer.Observable;
import com.alipay.sofa.registry.observer.UnblockingObserver;
import com.alipay.sofa.registry.util.NamedThreadFactory;
import io.netty.util.ResourceLeakDetector;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * @author chen.zhu
 *     <p>Mar 15, 2021
 */
public class AbstractTest {

  protected final Logger logger = LoggerFactory.getLogger(getClass());

  protected ExecutorService executors;

  protected ScheduledExecutorService scheduled;

  @Rule public TestName name = new TestName();

  public static final Random random = new Random();

  @BeforeClass
  public static void beforeAbstractTestClass() {
    System.setProperty("spring.main.show_banner", "false");
  }

  @Before
  public void beforeAbstractTest() throws Exception {

    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    Thread.interrupted(); // clear interrupt

    executors = Executors.newCachedThreadPool(new NamedThreadFactory(name.getMethodName()));
    scheduled =
        Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new NamedThreadFactory("sched-" + name.getMethodName()));
    if (logger.isInfoEnabled()) {
      logger.info(remarkableMessage("[begin test][{}]"), name.getMethodName());
    }
  }

  @After
  public void afterAbstractTest()
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    executors.shutdownNow();
    scheduled.shutdownNow();

    logger.info(remarkableMessage("[end test][{}]"), name.getMethodName());
  }

  protected static void setEnv(Map<String, String> newenv) throws Exception {
    try {
      Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
      Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
      theEnvironmentField.setAccessible(true);
      Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
      env.putAll(newenv);
      Field theCaseInsensitiveEnvironmentField =
          processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
      theCaseInsensitiveEnvironmentField.setAccessible(true);
      Map<String, String> cienv =
          (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
      cienv.putAll(newenv);
    } catch (NoSuchFieldException e) {
      Class[] classes = Collections.class.getDeclaredClasses();
      Map<String, String> env = System.getenv();
      for (Class cl : classes) {
        if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
          Field field = cl.getDeclaredField("m");
          field.setAccessible(true);
          Object obj = field.get(env);
          Map<String, String> map = (Map<String, String>) obj;
          map.clear();
          map.putAll(newenv);
        }
      }
    }
  }

  public static void setFinalStatic(Field field, Object newValue) throws Exception {
    field.setAccessible(true);

    Field modifiersField = Field.class.getDeclaredField("modifiers");
    modifiersField.setAccessible(true);
    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

    field.set(null, newValue);
  }

  protected String remarkableMessage(String msg) {
    return String.format("--------------------------%s--------------------------\r\n", msg);
  }

  protected void waitForAnyKeyToExit() throws IOException {
    logger.info("type any key to exit..................");
    waitForAnyKey();
  }

  protected void waitForAnyKey() throws IOException {
    System.in.read();
  }

  protected void waitConditionUntilTimeOut(BooleanSupplier booleanSupplier)
      throws TimeoutException, InterruptedException {

    waitConditionUntilTimeOut(booleanSupplier, 5000, 2);
  }

  protected static void waitConditionUntilTimeOut(
      BooleanSupplier booleanSupplier, int waitTimeMilli)
      throws TimeoutException, InterruptedException {

    waitConditionUntilTimeOut(booleanSupplier, waitTimeMilli, 2);
  }

  protected static void waitConditionUntilTimeOut(
      BooleanSupplier booleanSupplier, int waitTimeMilli, int intervalMilli)
      throws TimeoutException, InterruptedException {

    long maxTime = System.currentTimeMillis() + waitTimeMilli;

    while (true) {
      boolean result = booleanSupplier.getAsBoolean();
      if (result) {
        return;
      }
      if (System.currentTimeMillis() >= maxTime) {
        throw new TimeoutException("timeout still false:" + waitTimeMilli);
      }
      Thread.sleep(intervalMilli);
    }
  }

  public static int randomPort() {
    return randomPort(10000, 20000, null);
  }

  public static int randomPort(int min, int max, Set<Integer> different) {

    Random random = new Random();

    for (int i = min; i <= max; i++) {
      int port = min + random.nextInt(max - min + 1);
      if ((different == null || !different.contains(port)) && isUsable(port)) {
        return port;
      }
    }

    throw new IllegalStateException(String.format("random port not found:(%d, %d)", min, max));
  }

  public static int netmask = (1 << 8) - 1;

  public static String randomIp() {
    return String.format(
        "%d.%d.%d.%d",
        (Math.abs(random.nextInt()) % netmask + 1),
        (Math.abs(random.nextInt()) % netmask + 1),
        (Math.abs(random.nextInt()) % netmask + 1),
        (Math.abs(random.nextInt()) % netmask + 1));
  }

  protected static boolean isUsable(int port) {

    try (ServerSocket s = new ServerSocket()) {
      s.bind(new InetSocketAddress(port));
      return true;
    } catch (IOException e) {
    }
    return false;
  }

  public static String getDc() {
    return "DEFAULT_DC";
  }

  public static String randomString() {

    return randomString(1 << 10);
  }

  public static String randomString(int length) {

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append((char) ('a' + (int) (26 * Math.random())));
    }

    return sb.toString();
  }

  public static class NotifyObserversCounter implements UnblockingObserver {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void update(Observable source, Object message) {
      counter.getAndIncrement();
    }

    public int getCounter() {
      return counter.get();
    }
  }

  public static class ConcurrentExecutor implements Executor {

    private final int tasks;

    private final CyclicBarrier barrier;

    private final CountDownLatch latch;

    private final ExecutorService executors;

    public ConcurrentExecutor(int tasks, ExecutorService executors) {
      this.tasks = tasks;
      this.barrier = new CyclicBarrier(tasks);
      this.latch = new CountDownLatch(tasks);
      this.executors = executors;
    }

    @Override
    public void execute(final Runnable command) {
      for (int i = 0; i < tasks; i++) {
        executors.execute(
            new Runnable() {
              @Override
              public void run() {
                try {
                  barrier.await();
                  command.run();
                } catch (Exception ignore) {
                }

                latch.countDown();
              }
            });
      }
      try {
        latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
