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
package com.alipay.sofa.registry.jdbc.config;

import static com.alibaba.druid.pool.DruidDataSourceFactory.*;

import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alipay.sofa.registry.jdbc.elector.MetaJdbcLeaderElector;
import com.alipay.sofa.registry.jdbc.repository.batch.AppRevisionBatchQueryCallable;
import com.alipay.sofa.registry.jdbc.repository.batch.AppRevisionHeartbeatBatchCallable;
import com.alipay.sofa.registry.jdbc.repository.batch.InterfaceAppBatchQueryCallable;
import com.alipay.sofa.registry.jdbc.repository.impl.AppRevisionHeartbeatJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.AppRevisionJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.InterfaceAppsJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.ProvideDataJdbcRepository;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.store.api.repository.AppRevisionHeartbeatRepository;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.util.SystemUtils;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author xiaojian.xj
 * @version $Id: JdbcConfiguration.java, v 0.1 2021年01月17日 16:28 xiaojian.xj Exp $
 */
@Configuration
@EnableConfigurationProperties
public class JdbcConfiguration {

  @Configuration
  @MapperScan(basePackages = "com.alipay.sofa.registry.jdbc.mapper")
  public static class MybatisBeanConfiguration {
    @Bean
    public JdbcDriverConfig jdbcDriverConfig() {
      return new JdbcDriverConfigBean();
    }

    @Bean
    public Slf4jLogFilter logFilter() {
      Slf4jLogFilter filter = new Slf4jLogFilter();
      filter.setResultSetLogEnabled(true);
      filter.setConnectionLogEnabled(true);
      filter.setStatementParameterClearLogEnable(true);
      filter.setStatementCreateAfterLogEnabled(true);
      filter.setStatementCloseAfterLogEnabled(true);
      filter.setStatementParameterSetLogEnabled(true);
      filter.setStatementPrepareAfterLogEnabled(true);
      filter.setStatementExecutableSqlLogEnable(true);
      return filter;
    }

    /**
     * create datasource
     *
     * @return
     * @throws Exception
     */
    @Bean
    public DataSource dataSource(JdbcDriverConfig jdbcDriverConfig, Slf4jLogFilter logFilter)
        throws Exception {
      Properties props = new Properties();
      props.put(
          PROP_DRIVERCLASSNAME,
          SystemUtils.getSystem(PROP_DRIVERCLASSNAME, jdbcDriverConfig.getDriverClassName()));
      props.put(PROP_URL, SystemUtils.getSystem(PROP_URL, jdbcDriverConfig.getUrl()));
      props.put(
          PROP_USERNAME, SystemUtils.getSystem(PROP_USERNAME, jdbcDriverConfig.getUsername()));
      props.put(
          PROP_PASSWORD, SystemUtils.getSystem(PROP_PASSWORD, jdbcDriverConfig.getPassword()));

      // todo connection pool config
      props.put(PROP_MINIDLE, jdbcDriverConfig.getMinIdle() + "");
      props.put(PROP_MAXACTIVE, jdbcDriverConfig.getMaxActive() + "");
      props.put(PROP_MAXWAIT, jdbcDriverConfig.getMaxWait() + "");
      props.put(PROP_REMOVEABANDONED, "true");
      props.put(PROP_REMOVEABANDONEDTIMEOUT, "30");
      props.put(PROP_LOGABANDONED, "true");
      props.put(PROP_INIT, "true");

      // log filter
      // props.put(PROP_FILTERS, logFilter);

      DataSource dataSource = DruidDataSourceFactory.createDataSource(props);

      return dataSource;
    }

    /**
     * create sqlSessionFactory
     *
     * @param dataSource
     * @return
     * @throws Exception
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(
        DataSource dataSource, JdbcDriverConfig jdbcDriverConfig) throws Exception {
      SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
      factoryBean.setDataSource(dataSource);
      // factoryBean.setTypeAliasesPackage(jdbcDriverConfig.getTypeAliasesPackage());
      factoryBean.setMapperLocations(
          new PathMatchingResourcePatternResolver()
              .getResources(jdbcDriverConfig.getMapperLocations()));

      return factoryBean.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
      DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
      transactionManager.setDataSource(dataSource);
      return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate(
        DataSourceTransactionManager transactionManager) {
      TransactionTemplate transactionTemplate = new TransactionTemplate();
      transactionTemplate.setTransactionManager(transactionManager);
      return transactionTemplate;
    }
  }

  @Configuration
  public static class MetadataBeanConfiguration {
    @Bean
    public MetadataConfig metadataConfig() {
      return new MetadataConfigBean();
    }

    @Bean
    public MetaElectorConfig metaElectorConfig() {
      return new MetaElectorConfigBean();
    }
  }

  @Configuration
  public static class RepositoryBeanConfiguration {

    /** JDBC Repository */
    @Bean
    public AppRevisionRepository appRevisionJdbcRepository() {
      return new AppRevisionJdbcRepository();
    }

    @Bean
    public InterfaceAppsRepository interfaceAppsJdbcRepository() {
      return new InterfaceAppsJdbcRepository();
    }

    @Bean
    public AppRevisionHeartbeatRepository appRevisionHeartbeatJdbcRepository() {
      return new AppRevisionHeartbeatJdbcRepository();
    }

    @Bean
    public MetaJdbcLeaderElector jdbcLeaderElector() {
      return new MetaJdbcLeaderElector();
    }

    @Bean
    public ProvideDataRepository provideDataJdbcRepository() {
      return new ProvideDataJdbcRepository();
    }

    /** batch callable */
    @Bean
    public AppRevisionBatchQueryCallable appRevisionBatchQueryCallable() {
      return new AppRevisionBatchQueryCallable();
    }

    @Bean
    public InterfaceAppBatchQueryCallable interfaceAppBatchQueryCallable() {
      return new InterfaceAppBatchQueryCallable();
    }

    @Bean
    public AppRevisionHeartbeatBatchCallable appRevisionHeartbeatBatchCallable() {
      return new AppRevisionHeartbeatBatchCallable();
    }
  }
}
