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
package com.alipay.sofa.registry.client.provider;

import static com.alipay.sofa.registry.client.constants.ValueConstants.DEFAULT_GROUP;

import com.alipay.remoting.ConnectionEventProcessor;
import com.alipay.remoting.ConnectionEventType;
import com.alipay.remoting.rpc.protocol.UserProcessor;
import com.alipay.sofa.registry.client.api.Configurator;
import com.alipay.sofa.registry.client.api.EventBus;
import com.alipay.sofa.registry.client.api.Publisher;
import com.alipay.sofa.registry.client.api.Register;
import com.alipay.sofa.registry.client.api.RegistryClient;
import com.alipay.sofa.registry.client.api.RegistryClientConfig;
import com.alipay.sofa.registry.client.api.Subscriber;
import com.alipay.sofa.registry.client.api.exception.DuplicateException;
import com.alipay.sofa.registry.client.api.exception.RegistryClientException;
import com.alipay.sofa.registry.client.api.model.RegistryType;
import com.alipay.sofa.registry.client.api.registration.ConfiguratorRegistration;
import com.alipay.sofa.registry.client.api.registration.PublisherRegistration;
import com.alipay.sofa.registry.client.api.registration.SubscriberRegistration;
import com.alipay.sofa.registry.client.auth.AuthManager;
import com.alipay.sofa.registry.client.auth.NoopAuthManager;
import com.alipay.sofa.registry.client.event.ConfiguratorProcessEvent;
import com.alipay.sofa.registry.client.event.DefaultEventBus;
import com.alipay.sofa.registry.client.event.LookoutSubscriber;
import com.alipay.sofa.registry.client.event.SubscriberProcessEvent;
import com.alipay.sofa.registry.client.log.LoggerFactory;
import com.alipay.sofa.registry.client.remoting.ClientConnection;
import com.alipay.sofa.registry.client.remoting.ClientConnectionCloseEventProcessor;
import com.alipay.sofa.registry.client.remoting.ClientConnectionOpenEventProcessor;
import com.alipay.sofa.registry.client.remoting.ReceivedConfigDataProcessor;
import com.alipay.sofa.registry.client.remoting.ReceivedDataProcessor;
import com.alipay.sofa.registry.client.remoting.ServerManager;
import com.alipay.sofa.registry.client.task.ObserverHandler;
import com.alipay.sofa.registry.client.task.SyncConfigThread;
import com.alipay.sofa.registry.client.task.TaskEvent;
import com.alipay.sofa.registry.client.task.WorkerThread;
import com.alipay.sofa.registry.client.util.StringUtils;
import com.alipay.sofa.registry.core.model.ReceivedConfigData;
import com.alipay.sofa.registry.core.model.ReceivedData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;

/**
 * The type Default registry client.
 *
 * @author zhuoyu.sjw
 * @version $Id : DefaultRegistryClient.java, v 0.1 2017-11-23 20:07 zhuoyu.sjw Exp $$
 */
public class DefaultRegistryClient implements RegistryClient {

  /** LOGGER */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRegistryClient.class);

  private RegistryClientConfig registryClientConfig;

  private RegisterCache registerCache;

  private ServerManager serverManager;

  private WorkerThread workerThread;

  private ClientConnection client;

  private Map<Class<?>, UserProcessor> userProcessorMap;

  private Map<ConnectionEventType, ConnectionEventProcessor> connectionEventProcessorMap;

  private ConcurrentMap<PublisherRegistration, Publisher> registrationPublisherMap;

  private ConcurrentMap<SubscriberRegistration, Subscriber> registrationSubscriberMap;

  private ConcurrentMap<ConfiguratorRegistration, Configurator> registrationConfiguratorMap;

  private ObserverHandler observerHandler;

  private AuthManager authManager;

  private EventBus eventBus;

  private LookoutSubscriber lookoutSubscriber;

  private AtomicBoolean init = new AtomicBoolean(false);

  /**
   * Instantiates a new Default registry client.
   *
   * @param registryClientConfig the registry client config
   */
  public DefaultRegistryClient(RegistryClientConfig registryClientConfig) {
    // clone config to avoid configuration changes at runtime
    this.registryClientConfig = cloneConfig(registryClientConfig);
    this.registerCache = new RegisterCache();
    this.registrationPublisherMap = new ConcurrentHashMap<PublisherRegistration, Publisher>();
    this.registrationSubscriberMap = new ConcurrentHashMap<SubscriberRegistration, Subscriber>();
    this.registrationConfiguratorMap =
        new ConcurrentHashMap<ConfiguratorRegistration, Configurator>();
  }

  private DefaultRegistryClientConfig cloneConfig(RegistryClientConfig registryClientConfig) {
    DefaultRegistryClientConfig cloneConfig = null;
    DefaultRegistryClientConfigBuilder builder = DefaultRegistryClientConfigBuilder.start();
    if (null != registryClientConfig) {
      cloneConfig =
          builder
              .setEnv(registryClientConfig.getEnv())
              .setAppName(registryClientConfig.getAppName())
              .setInstanceId(registryClientConfig.getInstanceId())
              .setDataCenter(registryClientConfig.getDataCenter())
              .setZone(registryClientConfig.getZone())
              .setRegistryEndpoint(registryClientConfig.getRegistryEndpoint())
              .setRegistryEndpointPort(registryClientConfig.getRegistryEndpointPort())
              .setConnectTimeout(registryClientConfig.getConnectTimeout())
              .setSocketTimeout(registryClientConfig.getSocketTimeout())
              .setInvokeTimeout(registryClientConfig.getInvokeTimeout())
              .setRecheckInterval(registryClientConfig.getRecheckInterval())
              .setObserverThreadCoreSize(registryClientConfig.getObserverThreadCoreSize())
              .setObserverThreadMaxSize(registryClientConfig.getObserverThreadMaxSize())
              .setObserverThreadQueueLength(registryClientConfig.getObserverThreadQueueLength())
              .setObserverCallbackTimeout(registryClientConfig.getObserverCallbackTimeout())
              .setSyncConfigRetryInterval(registryClientConfig.getSyncConfigRetryInterval())
              .setAccessKey(registryClientConfig.getAccessKey())
              .setSecretKey(registryClientConfig.getSecretKey())
              .build();
    }
    return cloneConfig;
  }

  /** Init. */
  public void init() {
    if (!init.compareAndSet(false, true)) {
      return;
    }

    // init lookout subscriber
    if (null == lookoutSubscriber) {
      this.lookoutSubscriber = new LookoutSubscriber();
    }

    // init event bus
    if (null == eventBus) {
      this.eventBus = new DefaultEventBus(registryClientConfig);
      this.eventBus.register(SubscriberProcessEvent.class, lookoutSubscriber);
      this.eventBus.register(ConfiguratorProcessEvent.class, lookoutSubscriber);
    }

    // init server manager
    if (null == serverManager) {
      this.serverManager = new DefaultServerManager(registryClientConfig);
    }

    // init observer handler
    if (null == observerHandler) {
      observerHandler = new DefaultObserverHandler(registryClientConfig, eventBus);
    }

    // init auth manager
    if (null == authManager) {
      authManager = NoopAuthManager.INSTANCE;
    }

    // init user processor
    List<UserProcessor> userProcessorList = new ArrayList<UserProcessor>();
    if (null == userProcessorMap) {
      userProcessorList.add(new ReceivedDataProcessor(registerCache, observerHandler));
      userProcessorList.add(new ReceivedConfigDataProcessor(registerCache, observerHandler));
    } else {
      UserProcessor userProcessor = userProcessorMap.get(ReceivedData.class);
      if (null == userProcessor) {
        userProcessorList.add(new ReceivedDataProcessor(registerCache, observerHandler));
      }
      userProcessor = userProcessorMap.get(ReceivedConfigData.class);
      if (null == userProcessor) {
        userProcessorList.add(new ReceivedConfigDataProcessor(registerCache, observerHandler));
      }
      userProcessorList.addAll(userProcessorMap.values());
    }

    // init connection event processor
    if (null == connectionEventProcessorMap) {
      connectionEventProcessorMap =
          new HashMap<ConnectionEventType, ConnectionEventProcessor>(
              ConnectionEventType.values().length);
    }
    if (null == connectionEventProcessorMap.get(ConnectionEventType.CLOSE)) {
      ClientConnectionCloseEventProcessor connectionCloseEventProcessor =
          new ClientConnectionCloseEventProcessor();
      connectionEventProcessorMap.put(ConnectionEventType.CLOSE, connectionCloseEventProcessor);
    }
    if (null == connectionEventProcessorMap.get(ConnectionEventType.CONNECT)) {
      ClientConnectionOpenEventProcessor connectionOpenEventProcessor =
          new ClientConnectionOpenEventProcessor();
      connectionEventProcessorMap.put(ConnectionEventType.CONNECT, connectionOpenEventProcessor);
    }

    // init client connection and register worker
    client =
        new ClientConnection(
            serverManager,
            userProcessorList,
            connectionEventProcessorMap,
            registerCache,
            registryClientConfig);

    workerThread = new WorkerThread(client, registryClientConfig, registerCache);
    client.setWorker(workerThread);

    client.init();

    // init registry check thread
    new RegistryCheckThread().start();

    // init sync config thread
    new SyncConfigThread(client, registerCache, registryClientConfig, observerHandler).start();
  }

  /** @see RegistryClient#register(PublisherRegistration, String...) */
  @Override
  public Publisher register(PublisherRegistration registration, String... data) {
    if (!init.get()) {
      throw new IllegalStateException("Client needs to be initialized before using.");
    }

    if (null == registration) {
      throw new IllegalArgumentException("Registration can not be null.");
    }

    if (StringUtils.isBlank(registration.getDataId())) {
      throw new IllegalArgumentException("DataId can not be null");
    }

    if (StringUtils.isBlank(registration.getGroup())) {
      registration.setGroup(DEFAULT_GROUP);
    }

    Publisher publisher = registrationPublisherMap.get(registration);

    if (null != publisher) {
      throwDuplicateException(registration, publisher);
    }

    publisher = new DefaultPublisher(registration, workerThread, registryClientConfig);
    ((DefaultPublisher) publisher).setAuthManager(authManager);

    Publisher oldPublisher = registrationPublisherMap.putIfAbsent(registration, publisher);
    if (null != oldPublisher) {
      throwDuplicateException(registration, oldPublisher);
    }

    registerCache.addRegister(publisher);

    publisher.republish(data);

    LOGGER.info(
        "[api] Regist publisher success, dataId: {}, group: {}, registerId: {}",
        publisher.getDataId(),
        publisher.getGroup(),
        publisher.getRegistId());

    return publisher;
  }

  private void throwDuplicateException(PublisherRegistration registration, Publisher publisher) {
    LOGGER.info(
        "[api] Publisher already exists, dataId: {}, group: {}, registerId: {}",
        publisher.getDataId(),
        publisher.getGroup(),
        publisher.getRegistId());

    throw new DuplicateException(
        "Duplicate Publisher registration. (dataId: "
            + registration.getDataId()
            + ", group: "
            + registration.getGroup()
            + ")");
  }

  /** @see RegistryClient#register(SubscriberRegistration) */
  @Override
  public Subscriber register(SubscriberRegistration registration) {
    if (!init.get()) {
      throw new IllegalStateException("Client needs to be initialized before using.");
    }

    if (null == registration) {
      throw new IllegalArgumentException("Registration can not be null.");
    }

    if (StringUtils.isBlank(registration.getDataId())) {
      throw new IllegalArgumentException("DataId can not be null.");
    }

    if (null == registration.getSubscriberDataObserver()) {
      throw new IllegalArgumentException("Subscriber data observer can not be null.");
    }

    if (StringUtils.isBlank(registration.getGroup())) {
      registration.setGroup(DEFAULT_GROUP);
    }

    Subscriber subscriber = registrationSubscriberMap.get(registration);

    if (null != subscriber) {
      throwDuplicateException(registration, subscriber);
    }

    subscriber = new DefaultSubscriber(registration, workerThread, registryClientConfig);
    ((DefaultSubscriber) subscriber).setAuthManager(authManager);

    Subscriber oldSubscriber = registrationSubscriberMap.putIfAbsent(registration, subscriber);
    if (null != oldSubscriber) {
      throwDuplicateException(registration, oldSubscriber);
    }

    registerCache.addRegister(subscriber);
    addRegisterTask(subscriber);

    LOGGER.info(
        "[api] Regist subscriber success, dataId: {}, group: {}, scope: {}, registerId: {}",
        subscriber.getDataId(),
        subscriber.getGroup(),
        subscriber.getScopeEnum(),
        subscriber.getRegistId());

    return subscriber;
  }

  /** @see RegistryClient#register(ConfiguratorRegistration) */
  @Override
  public Configurator register(ConfiguratorRegistration registration) {
    if (!init.get()) {
      throw new IllegalStateException("Client needs to be initialized before using.");
    }

    if (null == registration) {
      throw new IllegalArgumentException("Registration can not be null.");
    }

    if (StringUtils.isBlank(registration.getDataId())) {
      throw new IllegalArgumentException("DataId can not be null.");
    }

    if (null == registration.getConfigDataObserver()) {
      throw new IllegalArgumentException("Config data observer can not be null");
    }

    if (StringUtils.isBlank(registration.getGroup())) {
      registration.setGroup(DEFAULT_GROUP);
    }

    Configurator configurator = registrationConfiguratorMap.get(registration);

    if (null != configurator) {
      throwDuplicateException(configurator);
    }

    configurator = new DefaultConfigurator(registration, registryClientConfig, workerThread);
    ((DefaultConfigurator) configurator).setAuthManager(authManager);

    Configurator oldConfigurator =
        registrationConfiguratorMap.putIfAbsent(registration, configurator);
    if (null != oldConfigurator) {
      throwDuplicateException(configurator);
    }

    registerCache.addRegister(configurator);
    addRegisterTask(configurator);

    LOGGER.info(
        "[api] Regist configurator success, dataId: {}, registerId: {}",
        configurator.getDataId(),
        configurator.getRegistId());

    return configurator;
  }

  private void throwDuplicateException(Configurator configurator) {
    LOGGER.info(
        "[api] Configurator already exists, dataId: {}, registerId: {}",
        configurator.getDataObserver(),
        configurator.getRegistId());
    throw new DuplicateException(
        "Duplicate configurator registration. (dataId: " + configurator.getDataId() + " )");
  }

  /** @see RegistryClient#unregister(String, String, RegistryType) */
  @Override
  public int unregister(String dataId, String group, RegistryType registryType) {
    if (StringUtils.isBlank(dataId)) {
      throw new IllegalArgumentException("dataId can not be empty");
    }

    if (null == registryType) {
      throw new IllegalArgumentException("registry type can not be null");
    }

    if (null == group) {
      group = DEFAULT_GROUP;
    }

    List<Register> registers = new ArrayList<Register>();

    if (RegistryType.PUBLISHER == registryType) {
      Collection<Publisher> publishers = registerCache.getAllPublishers();
      for (Publisher publisher : publishers) {
        if (dataId.equals(publisher.getDataId()) && group.equals(publisher.getGroup())) {
          registers.add(publisher);
        }
      }
    } else if (RegistryType.SUBSCRIBER == registryType) {
      Collection<Subscriber> subscribers = registerCache.getAllSubscribers();
      for (Subscriber subscriber : subscribers) {
        if (dataId.equals(subscriber.getDataId()) && group.equals(subscriber.getGroup())) {
          registers.add(subscriber);
        }
      }
    } else if (RegistryType.CONFIGURATOR == registryType) {
      Collection<Configurator> configurators = registerCache.getAllConfigurator();
      for (Configurator configurator : configurators) {
        if (dataId.equals(configurator.getDataId())) {
          registers.add(configurator);
        }
      }
    }

    for (Register register : registers) {
      register.unregister();
    }
    return registers.size();
  }

  private void throwDuplicateException(SubscriberRegistration registration, Subscriber subscriber) {
    LOGGER.info(
        "[api] Subscriber already exists, dataId: {}, group: {}, scope: {}, registerId: {}",
        subscriber.getDataId(),
        subscriber.getGroup(),
        subscriber.getScopeEnum(),
        subscriber.getRegistId());
    throw new DuplicateException(
        "Duplicate subscriber registration. (dataId: "
            + registration.getDataId()
            + ", group: "
            + registration.getGroup()
            + ")");
  }

  /**
   * Add register task.
   *
   * @param register the register
   * @throws RegistryClientException the registry client exception
   */
  private void addRegisterTask(Register register) throws RegistryClientException {
    try {
      TaskEvent event = new TaskEvent(register);
      workerThread.schedule(event);
    } catch (Exception e) {
      LOGGER.error("Register task schedule error, {}", register, e);
      throw new RegistryClientException("Register task schedule error", e);
    }
  }

  /**
   * Getter method for property <tt>registerCache</tt>.
   *
   * @return property value of registerCache
   */
  public RegisterCache getRegisterCache() {
    return registerCache;
  }

  /**
   * Setter method for property <tt>userProcessorMap</tt>.
   *
   * @param userProcessorMap value to be assigned to property userProcessorMap
   */
  public void setUserProcessorMap(Map<Class<?>, UserProcessor> userProcessorMap) {
    this.userProcessorMap = userProcessorMap;
  }

  /**
   * Setter method for property <tt>connectionEventProcessorMap</tt>.
   *
   * @param connectionEventProcessorMap value to be assigned to property connectionEventProcessorMap
   */
  public void setConnectionEventProcessorMap(
      Map<ConnectionEventType, ConnectionEventProcessor> connectionEventProcessorMap) {
    this.connectionEventProcessorMap = connectionEventProcessorMap;
  }

  /**
   * Setter method for property <tt>observerHandler</tt>.
   *
   * @param observerHandler value to be assigned to property observerHandler
   */
  public void setObserverHandler(ObserverHandler observerHandler) {
    this.observerHandler = observerHandler;
  }

  /**
   * Setter method for property <tt>serverManager</tt>.
   *
   * @param serverManager value to be assigned to property serverManager
   */
  public void setServerManager(ServerManager serverManager) {
    this.serverManager = serverManager;
  }

  /**
   * Setter method for property <tt>authManager</tt>.
   *
   * @param authManager value to be assigned to property authManager
   */
  public void setAuthManager(AuthManager authManager) {
    this.authManager = authManager;
  }

  /**
   * Setter method for property <tt>lookoutSubscriber</tt>.
   *
   * @param lookoutSubscriber value to be assigned to property lookoutSubscriber
   */
  public void setLookoutSubscriber(LookoutSubscriber lookoutSubscriber) {
    this.lookoutSubscriber = lookoutSubscriber;
  }

  /**
   * Setter method for property <tt>eventBus</tt>.
   *
   * @param eventBus value to be assigned to property eventBus
   */
  public void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  /** The type Registry check thread. */
  class RegistryCheckThread extends Thread {

    /** Instantiates a new Registry check thread. */
    public RegistryCheckThread() {
      super("RegistryClientCheckThread");
      setDaemon(true);
    }

    /** @see Thread#run() */
    @Override
    public void run() {

      //noinspection InfiniteLoopStatement
      while (true) {
        try {
          Thread.sleep(registryClientConfig.getRecheckInterval());

          Collection<Publisher> allPublishers = registerCache.getAllPublishers();

          for (Publisher publisher : allPublishers) {
            try {
              if (null != publisher && !((AbstractInternalRegister) publisher).isDone()) {
                addRegisterTask(publisher);
              }
            } catch (Exception e) {
              LOGGER.error("Sync publisher error, {}", publisher, e);
            }
          }

          Collection<Subscriber> allSubscribers = registerCache.getAllSubscribers();

          for (Subscriber subscriber : allSubscribers) {
            try {
              if (null != subscriber && !((AbstractInternalRegister) subscriber).isDone()) {
                addRegisterTask(subscriber);
              }
            } catch (Exception e) {
              LOGGER.error("Sync subscriber error, {}", subscriber, e);
            }
          }

          Collection<Configurator> allConfigurators = registerCache.getAllConfigurator();

          for (Configurator configurator : allConfigurators) {
            try {
              if (null != configurator && !((AbstractInternalRegister) configurator).isDone()) {
                addRegisterTask(configurator);
              }
            } catch (Exception e) {
              LOGGER.error("Sync configurator error, {}", configurator, e);
            }
          }
        } catch (Throwable e) {
          LOGGER.error("Execute error", e);
        }
      }
    }
  }
}
