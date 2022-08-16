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

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.core.model.AppRevisionInterface;
import com.alipay.sofa.registry.util.FileUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.h2.tools.Console;
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

  public static final String CLUSTER_ID = "DEFAULT_SEGMENT";
  public static final String RECOVER_CLUSTER_ID = "RECOVER_DEFAULT_SEGMENT";

  protected final String KEY_H2_PORT = "h2Port";
  private Server h2Server;

  protected DataSource dataSource;

  protected ApplicationContext applicationContext;

  public AbstractH2DbTestBase() {
    System.setProperty("nodes.clusterId", CLUSTER_ID);
    System.setProperty("nodes.recoverClusterId", RECOVER_CLUSTER_ID);
  }

  @Before
  public void setUpTestDataSource() throws SQLException, IOException {
    executeSqlScript(prepareDatas());
  }

  protected void startH2Server() throws SQLException {

    int h2Port = Integer.parseInt(System.getProperty(KEY_H2_PORT, "9123"));
    h2Server = Server.createTcpServer("-tcpPort", String.valueOf(h2Port), "-tcpAllowOthers");
    h2Server.start();
    new Console().runTool();
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

  public List<AppRevision> buildAppRevisions(int size) {
    List<AppRevision> appRevisionList = new ArrayList<>();
    for (int i = 1; i <= size; i++) {
      long l = System.currentTimeMillis();
      String suffix = l + "-" + i;

      String appname = "foo" + suffix;
      String revision = "1111" + suffix;

      AppRevision appRevision = new AppRevision();
      appRevision.setAppName(appname);
      appRevision.setRevision(revision);
      appRevision.setClientVersion("1.0");

      Map<String, List<String>> baseParams = Maps.newHashMap();
      baseParams.put("metaBaseParam1", Lists.newArrayList("metaBaseValue1"));
      appRevision.setBaseParams(baseParams);

      Map<String, AppRevisionInterface> interfaceMap = Maps.newHashMap();
      String dataInfo1 =
          DataInfo.toDataInfoId(
              "func1" + suffix, ValueConstants.DEFAULT_GROUP, ValueConstants.DEFAULT_INSTANCE_ID);
      String dataInfo2 =
          DataInfo.toDataInfoId(
              "func2" + suffix, ValueConstants.DEFAULT_GROUP, ValueConstants.DEFAULT_INSTANCE_ID);

      AppRevisionInterface inf1 = new AppRevisionInterface();
      AppRevisionInterface inf2 = new AppRevisionInterface();
      interfaceMap.put(dataInfo1, inf1);
      interfaceMap.put(dataInfo2, inf2);
      appRevision.setInterfaceMap(interfaceMap);

      inf1.setId("1");
      Map<String, List<String>> serviceParams1 = new HashMap<String, List<String>>();
      serviceParams1.put("metaParam2", Lists.newArrayList("metaValue2"));
      inf1.setServiceParams(serviceParams1);

      inf2.setId("2");
      Map<String, List<String>> serviceParams2 = new HashMap<String, List<String>>();
      serviceParams1.put("metaParam3", Lists.newArrayList("metaValues3"));
      inf1.setServiceParams(serviceParams2);

      appRevisionList.add(appRevision);
    }
    return appRevisionList;
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
