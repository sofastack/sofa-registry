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
import com.alipay.sofa.registry.util.PropertySplitter;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author xuanbei
 * @since 2019/2/15
 */
public class RegistryApplication {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryApplication.class);
  private static final String META_HTTP_SERVER_PORT = "meta.server.httpServerPort";
  private static final String DATA_HTTP_SERVER_PORT = "data.server.httpServerPort";
  private static final String SESSION_HTTP_SERVER_PORT = "session.server.httpServerPort";
  private static final String META_NODES = "nodes.metaNode";
  private static final String NODES_LOCAL_DATA_CENTER = "nodes.localDataCenter";

  private static ConfigurableApplicationContext metaApplicationContext;
  private static ConfigurableApplicationContext sessionApplicationContext;
  private static ConfigurableApplicationContext dataApplicationContext;

  public static void main(String[] args) throws Exception {
    System.setProperty("spring.profiles.active", "integrate");
    System.setProperty("lease.duration", "2");
    // setup DefaultUncaughtExceptionHandler
    Thread.setDefaultUncaughtExceptionHandler(
        (t, e) -> {
          LOGGER.error(
              String.format("UncaughtException in Thread(%s): %s", t.getName(), e.getMessage()), e);
        });

    // start registry application
    ConfigurableApplicationContext commonContext =
        new SpringApplicationBuilder(RegistryApplication.class).run(args);

    // get all server address list
    Collection<String> serverList = getServerList(commonContext);

    String driver = commonContext.getEnvironment().getProperty("jdbc.driverClassName");
    if ("org.h2.Driver".equals(driver)) {
      createTables(commonContext);
    }

    // start meta
    SpringApplicationBuilder springApplicationBuilder =
        new SpringApplicationBuilder(MetaApplication.class);
    springApplicationBuilder.parent(commonContext);
    metaApplicationContext = springApplicationBuilder.run();

    // wait meta cluster start
    waitClusterStart(
        serverList,
        Integer.parseInt(commonContext.getEnvironment().getProperty(META_HTTP_SERVER_PORT)));

    // start data
    dataApplicationContext =
        new SpringApplicationBuilder(DataApplication.class).parent(commonContext).run();

    // wait data cluster start
    waitClusterStart(
        serverList,
        Integer.parseInt(commonContext.getEnvironment().getProperty(DATA_HTTP_SERVER_PORT)));

    // start session
    sessionApplicationContext =
        new SpringApplicationBuilder(SessionApplication.class).parent(commonContext).run();

    // wait session cluster start
    waitClusterStart(
        serverList,
        Integer.parseInt(commonContext.getEnvironment().getProperty(SESSION_HTTP_SERVER_PORT)));
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

  private static Collection<String> getServerList(ConfigurableApplicationContext commonContext) {
    String metaNodes = commonContext.getEnvironment().getProperty(META_NODES);
    String localDataCenter = commonContext.getEnvironment().getProperty(NODES_LOCAL_DATA_CENTER);
    return new PropertySplitter().mapOfList(metaNodes).get(localDataCenter);
  }

  private static void waitClusterStart(Collection<String> serverList, int httpPort)
      throws Exception {
    for (String serverAddress : serverList) {
      while (true) {
        if (nodeHealthCheck(serverAddress, httpPort)) {
          LOGGER.info("{}:{} health check success.", serverAddress, httpPort);
          break;
        }
        LOGGER.error("{}:{} health check failed.", serverAddress, httpPort);
        Thread.sleep(1000);
      }
    }
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
    String driver = commonContext.getEnvironment().getProperty("jdbc.driverClassName");
    Class.forName(driver);
    String url = commonContext.getEnvironment().getProperty("jdbc.url");
    String username = commonContext.getEnvironment().getProperty("jdbc.username");
    String password = commonContext.getEnvironment().getProperty("jdbc.password");
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(url, username, password);
      executeSqlScript(connection, readFileAsString("sql/h2/create_table.sql"));
    } finally {
      if (connection != null) {
        connection.close();
      }
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
          throw new IllegalArgumentException("file load fail:" + f, e);
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
        throw new IllegalArgumentException("file load fail:" + f, e);
      }
    }

    // try classpath
    java.net.URL url = clazz.getResource(fileName);
    if (url == null) {
      url = clazz.getClassLoader().getResource(fileName);
    }
    if (url != null) {
      try {
        LOGGER.info("[load]{}", url);
        return url.openStream();
      } catch (IOException e) {
        throw new IllegalArgumentException("classpath load fail:" + url, e);
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
}
