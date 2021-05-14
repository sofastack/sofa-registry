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

import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_DRIVERCLASSNAME;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_FILTERS;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_INIT;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_LOGABANDONED;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_MAXACTIVE;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_MAXWAIT;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_MINIDLE;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_PASSWORD;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_REMOVEABANDONED;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_REMOVEABANDONEDTIMEOUT;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_URL;
import static com.alibaba.druid.pool.DruidDataSourceFactory.PROP_USERNAME;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
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
import com.alipay.sofa.registry.store.api.spring.SpringContext;
import com.alipay.sofa.registry.util.SystemUtils;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author xiaojian.xj
 * @version $Id: JdbcConfiguration.java, v 0.1 2021年01月17日 16:28 xiaojian.xj Exp $
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(
    value = SpringContext.PERSISTENCE_PROFILE_ACTIVE,
    havingValue = SpringContext.META_STORE_API_JDBC)
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

    @Bean
    public StatFilter statFilter(JdbcDriverConfig jdbcDriverConfig) {
      StatFilter filter = new StatFilter();
      filter.setSlowSqlMillis(jdbcDriverConfig.getSlowSqlMillis());
      filter.setLogSlowSql(true);
      return filter;
    }

    /**
     * create datasource
     *
     * @return
     * @throws Exception
     */
    @Bean
    public DataSource dataSource(JdbcDriverConfig jdbcDriverConfig)
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

      DruidDataSource dataSource = (DruidDataSource)DruidDataSourceFactory.createDataSource(props);
      // log filter
      List list= Lists.newArrayList(logFilter(), statFilter(jdbcDriverConfig));
      dataSource.setProxyFilters(list);
      return dataSource;
    }

    @Bean
    public DatabaseIdProvider databaseIdProvider() {
      DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
      Properties p = new Properties();
      p.setProperty("MySQL", "mysql");
      p.setProperty("H2", "h2");
      databaseIdProvider.setProperties(p);
      return databaseIdProvider;
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
        DataSource dataSource,
        JdbcDriverConfig jdbcDriverConfig,
        DatabaseIdProvider databaseIdProvider)
        throws Exception {
      SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
      factoryBean.setDataSource(dataSource);
      factoryBean.setDatabaseIdProvider(databaseIdProvider);
      // factoryBean.setTypeAliasesPackage(jdbcDriverConfig.getTypeAliasesPackage());
      ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

      List<Resource> resources = Lists.newArrayList();
      for (String location : jdbcDriverConfig.getMapperLocations()) {
        resources.addAll(Arrays.asList(resolver.getResources(location)));
      }

      factoryBean.setMapperLocations(resources.toArray(new Resource[0]));
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
    public DefaultCommonConfig defaultCommonConfig() {
      return new DefaultCommonConfigBean();
    }

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
