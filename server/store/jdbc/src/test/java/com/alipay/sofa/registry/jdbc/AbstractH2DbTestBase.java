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
package com.alipay.sofa.registry.jdbc;

import com.alipay.sofa.registry.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.h2.tools.Server;
import org.junit.Before;
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
public class AbstractH2DbTestBase extends AbstractTest implements ApplicationContextAware {

  public static final String TABLE_STRUCTURE = "sql/h2/create_table.sql";
  public static final String TABLE_DATA = "sql/h2/base_info.sql";

  protected final String KEY_H2_PORT = "h2Port";
  private Server h2Server;

  protected DataSource dataSource;

  protected ApplicationContext applicationContext;

  @Before
  public void setUpTestDataSource() throws SQLException, IOException {
    executeSqlScript(prepareDatas());
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

  protected String prepareDatasFromFile(String path) throws IOException {
    return readFileAsString(path);
  }

  public static String readFileAsString(String fileName) {
    try {
      return Arrays.toString(FileUtils.readFileToByteArray(new File(fileName)));
    } catch (Throwable th) {
      return "";
    }
  }

  protected void executeSqlScript(String prepareSql) throws SQLException {
    if (StringUtils.isEmpty(prepareSql)) {
      return;
    }
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      conn = dataSource.getConnection();
      conn.setAutoCommit(false);
      if (!Strings.isEmpty(prepareSql)) {
        for (String sql : prepareSql.split(";")) {
          logger.debug("[setup][data]{}", sql.trim());
          stmt = conn.prepareStatement(sql);
          stmt.executeUpdate();
        }
      }
      conn.commit();

    } catch (Exception ex) {
      logger.error("[SetUpTestDataSource][fail]:", ex);
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
