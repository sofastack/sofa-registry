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
package com.alipay.sofa.registry.server.meta;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.FileUtils;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.h2.tools.Server;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author chen.zhu
 *     <p>Mar 15, 2021
 */
@ActiveProfiles("test")
@SpringBootTest(classes = AbstractH2DbTestBase.JdbcTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AbstractH2DbTestBase extends AbstractTestBase implements ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractH2DbTestBase.class);
  public static final String TABLE_STRUCTURE = "sql/h2/create_table.sql";
  public static final String TABLE_DATA = "sql/h2/base_info.sql";

  public static final String CLUSTER_ID = "DEFAULT_SEGMENT";
  public static final String RECOVER_CLUSTER_ID = "RECOVER_DEFAULT_SEGMENT";

  protected final String KEY_H2_PORT = "h2Port";
  private Server h2Server;

  protected ApplicationContext applicationContext;

  @BeforeClass
  public static void beforeAbstractH2DbTestBase()
      throws SQLException, IOException, ClassNotFoundException {
    createTables();
  }

  public AbstractH2DbTestBase() {
    System.setProperty("nodes.clusterId", CLUSTER_ID);
    System.setProperty("nodes.recoverClusterId", RECOVER_CLUSTER_ID);
  }

  protected void startH2Server() throws SQLException {

    int h2Port = Integer.parseInt(System.getProperty(KEY_H2_PORT, "9123"));
    h2Server = Server.createTcpServer("-tcpPort", String.valueOf(h2Port), "-tcpAllowOthers");
    h2Server.start();
    //		new Console().runTool();
  }

  protected String prepareDatas() {
    return "";
  }

  private static void createTables() throws ClassNotFoundException, SQLException, IOException {
    String driver = "org.h2.Driver";
    Class.forName(driver);
    String url = "jdbc:h2:mem:metadatadb;DB_CLOSE_DELAY=-1;MODE=MySQL;MV_STORE=FALSE";
    String username = "sa";
    String password = "";
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(url, username, password);
      executeSqlScript(connection, readFileAsString(TABLE_STRUCTURE));
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

  protected static void executeSqlScript(Connection conn, String prepareSql) throws SQLException {
    if (StringUtils.isEmpty(prepareSql)) {
      return;
    }
    PreparedStatement stmt = null;
    try {
      conn.setAutoCommit(false);
      if (!Strings.isEmpty(prepareSql)) {
        for (String sql : prepareSql.split(";")) {
          stmt = conn.prepareStatement(sql);
          stmt.executeUpdate();
        }
      }
      conn.commit();

    } catch (Exception ex) {
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

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @SpringBootApplication
  public static class JdbcTestConfig {
    public static void main(String[] args) {
      SpringApplication.run(JdbcTestConfig.class);
    }
  }
}
