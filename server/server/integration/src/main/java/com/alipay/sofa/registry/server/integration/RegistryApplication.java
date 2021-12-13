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
package com.alipay.sofa.registry.server.integration;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.core.model.Result;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.jersey.JerseyClient;
import com.alipay.sofa.registry.server.data.DataApplication;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerBootstrap;
import com.alipay.sofa.registry.server.meta.MetaApplication;
import com.alipay.sofa.registry.server.meta.bootstrap.MetaServerBootstrap;
import com.alipay.sofa.registry.server.session.SessionApplication;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerBootstrap;
import com.alipay.sofa.registry.util.FileUtils;
import com.alipay.sofa.registry.util.StringFormatter;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.h2.tools.Server;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class RegistryApplication {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryApplication.class);
  private static final Logger CRITICAL_LOGGER = LoggerFactory.getLogger("CRITICAL-ERROR");

  private static final String META_HTTP_SERVER_PORT = "meta.server.httpServerPort";
  private static final String DATA_HTTP_SERVER_PORT = "data.server.httpServerPort";
  private static final String SESSION_HTTP_SERVER_PORT = "session.server.httpServerPort";

  private static final String H2_DRIVER = "org.h2.Driver";

  private static ConfigurableApplicationContext metaApplicationContext;
  private static ConfigurableApplicationContext sessionApplicationContext;
  private static ConfigurableApplicationContext dataApplicationContext;

  public static void main(String[] args) throws Exception {
    if (StringUtils.isBlank(System.getProperty("spring.profiles.active"))) {
      System.setProperty("spring.profiles.active", "dev");
    }
    System.setProperty("registry.lease.duration.secs", "10");
    System.setProperty("registry.elector.warm.up.millis", "2000");

    // setup DefaultUncaughtExceptionHandler
    Thread.setDefaultUncaughtExceptionHandler(
        (t, e) ->
            CRITICAL_LOGGER.safeError(
                "UncaughtException in Thread({}): {}", t.getName(), e.getMessage(), e));

    // start registry application
    ConfigurableApplicationContext commonContext =
        new SpringApplicationBuilder(RegistryApplication.class).run(args);

    Collection<String> serverList = Collections.singletonList("localhost");

    String driver = commonContext.getEnvironment().getProperty("jdbc.driverClassName");
    if (H2_DRIVER.equals(driver)) {
      createTables(commonContext);
      Server.createWebServer("-web", "-webAllowOthers", "-webPort", "9630").start();
    }

    // start meta
    LOGGER.warn("starting meta");

    SpringApplicationBuilder springApplicationBuilder =
        new SpringApplicationBuilder(MetaApplication.class);
    springApplicationBuilder.parent(commonContext);
    metaApplicationContext = springApplicationBuilder.run();
    LOGGER.warn("waiting meta");
    // wait meta cluster start
    int metaPort =
        Integer.parseInt(commonContext.getEnvironment().getProperty(META_HTTP_SERVER_PORT, "9615"));
    waitClusterStart(serverList, metaPort);

    openPush(serverList.stream().findFirst().get(), metaPort);

    // start data
    LOGGER.warn("starting data");
    dataApplicationContext =
        new SpringApplicationBuilder(DataApplication.class).parent(commonContext).run();

    LOGGER.warn("waiting data");
    // wait data cluster start
    waitClusterStart(
        serverList,
        Integer.parseInt(
            commonContext.getEnvironment().getProperty(DATA_HTTP_SERVER_PORT, "9622")));

    // start session
    LOGGER.warn("starting session");
    sessionApplicationContext =
        new SpringApplicationBuilder(SessionApplication.class).parent(commonContext).run();

    // wait session cluster start
    LOGGER.warn("waiting session");
    waitClusterStart(
        serverList,
        Integer.parseInt(
            commonContext.getEnvironment().getProperty(SESSION_HTTP_SERVER_PORT, "9603")));
  }

  public static void stop() {
    if (sessionApplicationContext != null) {
      sessionApplicationContext
          .getBean("sessionServerBootstrap", SessionServerBootstrap.class)
          .destroy();
    }

    if (dataApplicationContext != null) {
      dataApplicationContext.getBean("dataServerBootstrap", DataServerBootstrap.class).destroy();
    }

    if (metaApplicationContext != null) {
      metaApplicationContext.getBean("metaServerBootstrap", MetaServerBootstrap.class).destroy();
    }
  }

  private static void waitClusterStart(Collection<String> serverList, int httpPort)
      throws Exception {
    for (String serverAddress : serverList) {
      for (int i = 0; i < 100; i++) {
        if (nodeHealthCheck(serverAddress, httpPort)) {
          LOGGER.info("{}:{} health check success.", serverAddress, httpPort);
          return;
        }
        LOGGER.error("{}:{} health check failed.", serverAddress, httpPort);
        Thread.sleep(1000);
      }
    }
    Runtime.getRuntime().halt(-1);
  }

  private static boolean nodeHealthCheck(String serverAddress, int httpPort) {
    CommonResponse resp = null;
    try {
      JerseyClient jerseyClient = JerseyClient.getInstance();
      Channel channel = jerseyClient.connect(new URL(serverAddress, httpPort));
      LOGGER.info("{}:{} health check", serverAddress, httpPort);
      resp =
          channel
              .getWebTarget()
              .path("health/check")
              .request(APPLICATION_JSON)
              .get(CommonResponse.class);
      return resp.isSuccess();
    } catch (Throwable t) {
      LOGGER.error("{}:{} health check failed. {}", serverAddress, httpPort, resp, t);
      return false;
    }
  }

  public static ConfigurableApplicationContext getMetaApplicationContext() {
    return metaApplicationContext;
  }

  public static ConfigurableApplicationContext getSessionApplicationContext() {
    return sessionApplicationContext;
  }

  public static ConfigurableApplicationContext getDataApplicationContext() {
    return dataApplicationContext;
  }

  public static FileInputStream openInputStream(File file) throws IOException {
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IOException("File '" + file + "' exists but is a directory");
      }
      if (!file.canRead()) {
        throw new IOException("File '" + file + "' cannot be read");
      }
    } else {
      throw new FileNotFoundException("File '" + file + "' does not exist");
    }
    return new FileInputStream(file);
  }

  private static void createTables(ConfigurableApplicationContext commonContext)
      throws ClassNotFoundException, SQLException, IOException {
    String driver = commonContext.getEnvironment().getProperty("jdbc.driverClassName", H2_DRIVER);
    Class.forName(driver);
    String url = commonContext.getEnvironment().getProperty("jdbc.url");
    String username = commonContext.getEnvironment().getProperty("jdbc.username");
    String password = commonContext.getEnvironment().getProperty("jdbc.password");
    try (Connection connection = DriverManager.getConnection(url, username, password)) {
      executeSqlScript(connection, readFileAsString("sql/h2/create_table.sql"));
    }
  }

  public static String readFileAsString(String fileName) throws IOException {

    try (InputStream ins = getFileInputStream(fileName)) {
      String fileContent = IOUtils.toString(ins);
      return fileContent;
    }
  }

  public static InputStream getFileInputStream(String fileName) throws FileNotFoundException {

    return getFileInputStream("./", fileName, FileUtils.class);
  }

  public static InputStream getFileInputStream(String path, String fileName, Class<?> clazz)
      throws FileNotFoundException {

    File f = null;
    if (path != null) {
      f = new File(path + "/" + fileName);
      if (f.exists()) {
        try {
          LOGGER.info("[getFileInputStream]{}", f.getAbsolutePath());
          return new FileInputStream(f);
        } catch (IOException e) {
          throw new IllegalArgumentException("file start fail:" + f, e);
        }
      }
    }

    // try file
    f = new File(fileName);
    if (f.exists()) {
      try {
        LOGGER.info("[getFileInputStream]{}", f.getAbsolutePath());
        return new FileInputStream(f);
      } catch (IOException e) {
        throw new IllegalArgumentException("file start fail:" + f, e);
      }
    }

    // try classpath
    java.net.URL url = clazz.getResource(fileName);
    if (url == null) {
      url = clazz.getClassLoader().getResource(fileName);
    }
    if (url != null) {
      try {
        LOGGER.info("[start]{}", url);
        return url.openStream();
      } catch (IOException e) {
        throw new IllegalArgumentException("classpath start fail:" + url, e);
      }
    }

    throw new FileNotFoundException(path + "," + fileName);
  }

  protected static void executeSqlScript(Connection connection, String prepareSql)
      throws SQLException {
    if (StringUtils.isEmpty(prepareSql)) {
      return;
    }
    java.sql.Connection conn = connection;
    PreparedStatement stmt = null;
    try {
      conn.setAutoCommit(false);
      if (!Strings.isEmpty(prepareSql)) {
        for (String sql : prepareSql.split(";")) {
          LOGGER.debug("[setup][data]{}", sql.trim());
          stmt = conn.prepareStatement(sql);
          stmt.executeUpdate();
        }
      }
      conn.commit();

    } catch (Exception ex) {
      LOGGER.error("[SetUpTestDataSource][fail]:", ex);
      if (null != conn) {
        conn.rollback();
      }
    } finally {
      if (null != stmt) {
        stmt.close();
      }
      if (null != conn) {
        conn.setAutoCommit(true);
        conn.close();
      }
    }
  }

  protected static void openPush(String metaAddress, int metaPort) {
    JerseyClient jerseyClient = JerseyClient.getInstance();
    Channel channel = jerseyClient.connect(new URL(metaAddress, metaPort));
    Result result =
        channel.getWebTarget().path("/stopPushDataSwitch/close").request().get(Result.class);
    if (!result.isSuccess()) {
      throw new RuntimeException(
          StringFormatter.format("open push failed: {}", result.getMessage()));
    }
  }
}
