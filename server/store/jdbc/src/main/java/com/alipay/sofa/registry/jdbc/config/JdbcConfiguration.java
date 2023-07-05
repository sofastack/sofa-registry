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
import com.alipay.sofa.registry.jdbc.repository.impl.AppRevisionJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.ClientManagerAddressJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.DateNowJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.DistributeLockJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.InterfaceAppsJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.MultiClusterSyncJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.ProvideDataJdbcRepository;
import com.alipay.sofa.registry.jdbc.repository.impl.RecoverConfigJdbcRepository;
import com.alipay.sofa.registry.store.api.config.StoreApiConfiguration;
import com.alipay.sofa.registry.store.api.date.DateNowRepository;
import com.alipay.sofa.registry.store.api.elector.DistributeLockRepository;
import com.alipay.sofa.registry.store.api.meta.ClientManagerAddressRepository;
import com.alipay.sofa.registry.store.api.meta.MultiClusterSyncRepository;
import com.alipay.sofa.registry.store.api.meta.ProvideDataRepository;
import com.alipay.sofa.registry.store.api.meta.RecoverConfigRepository;
import com.alipay.sofa.registry.store.api.repository.AppRevisionRepository;
import com.alipay.sofa.registry.store.api.repository.InterfaceAppsRepository;
import com.alipay.sofa.registry.store.api.spring.SpringContext;
import com.alipay.sofa.registry.util.SystemUtils;
import com.google.common.collect.Lists;
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
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
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
@Import({
  StoreApiConfiguration.class,
})
@EnableConfigurationProperties
@ConditionalOnProperty(
    value = SpringContext.PERSISTENCE_PROFILE_ACTIVE,
    havingValue = SpringContext.META_STORE_API_JDBC,
    matchIfMissing = true)
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

      filter.setStatementLogEnabled(true);
      filter.setStatementExecutableSqlLogEnable(true);
      filter.setStatementLogErrorEnabled(true);

      filter.setStatementPrepareAfterLogEnabled(false);
      filter.setStatementParameterSetLogEnabled(false);
      filter.setStatementExecuteQueryAfterLogEnabled(false);
      filter.setStatementCloseAfterLogEnabled(false);
      filter.setStatementPrepareCallAfterLogEnabled(false);
      filter.setStatementExecuteAfterLogEnabled(false);
      filter.setStatementExecuteUpdateAfterLogEnabled(false);
      filter.setStatementExecuteBatchAfterLogEnabled(false);
      filter.setStatementParameterClearLogEnable(false);

      filter.setResultSetLogEnabled(false);
      filter.setConnectionLogEnabled(false);

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
     * @param jdbcDriverConfig jdbcDriverConfig
     * @return DataSource
     * @throws Exception Exception
     */
    @Bean
    public DataSource dataSource(JdbcDriverConfig jdbcDriverConfig) throws Exception {
      Properties props = new Properties();
      props.put(
          PROP_DRIVERCLASSNAME,
          SystemUtils.getSystem(
              JdbcDriverConfigBean.PRE_FIX + "." + PROP_DRIVERCLASSNAME,
              jdbcDriverConfig.getDriverClassName()));
      props.put(
          PROP_URL,
          SystemUtils.getSystem(
              JdbcDriverConfigBean.PRE_FIX + "." + PROP_URL, jdbcDriverConfig.getUrl()));
      props.put(
          PROP_USERNAME,
          SystemUtils.getSystem(
              JdbcDriverConfigBean.PRE_FIX + "." + PROP_USERNAME, jdbcDriverConfig.getUsername()));
      props.put(
          PROP_PASSWORD,
          SystemUtils.getSystem(
              JdbcDriverConfigBean.PRE_FIX + "." + PROP_PASSWORD, jdbcDriverConfig.getPassword()));

      // todo connection pool config
      props.put(PROP_MINIDLE, jdbcDriverConfig.getMinIdle() + "");
      props.put(PROP_MAXACTIVE, jdbcDriverConfig.getMaxActive() + "");
      props.put(PROP_MAXWAIT, jdbcDriverConfig.getMaxWait() + "");
      props.put(PROP_REMOVEABANDONED, "true");
      props.put(PROP_REMOVEABANDONEDTIMEOUT, "30");
      props.put(PROP_LOGABANDONED, "true");
      props.put(PROP_INIT, "true");

      DruidDataSource dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(props);
      // log filter
      List<Filter> list = Lists.newArrayList(logFilter(), statFilter(jdbcDriverConfig));
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
     * @param dataSource dataSource
     * @param jdbcDriverConfig jdbcDriverConfig
     * @param databaseIdProvider databaseIdProvider
     * @return SqlSessionFactory
     * @throws Exception Exception
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
    public MetadataConfig metadataConfig() {
      return new MetadataConfigBean();
    }

    @Bean
    public MetaElectorConfig metaElectorConfig() {
      return new MetaElectorConfigBean();
    }
  }

  @Lazy
  @Configuration
  public static class RepositoryBeanConfiguration {

    /**
     * JDBC Repository
     *
     * @return AppRevisionRepository
     */
    @Bean
    public AppRevisionRepository appRevisionJdbcRepository() {
      return new AppRevisionJdbcRepository();
    }

    @Bean
    public InterfaceAppsRepository interfaceAppsJdbcRepository() {
      return new InterfaceAppsJdbcRepository();
    }

    @Bean
    public ProvideDataRepository provideDataJdbcRepository() {
      return new ProvideDataJdbcRepository();
    }

    @Bean
    public ClientManagerAddressRepository clientManagerAddressJdbcRepository() {
      return new ClientManagerAddressJdbcRepository();
    }

    @Bean
    public DistributeLockRepository distributeLockRepository() {
      return new DistributeLockJdbcRepository();
    }

    @Bean
    public RecoverConfigRepository recoverConfigJdbcRepository() {
      return new RecoverConfigJdbcRepository();
    }

    @Bean
    public DateNowRepository dateNowJdbcRepository() {
      return new DateNowJdbcRepository();
    }

    @Bean
    public MultiClusterSyncRepository multiClusterSyncRepository() {
      return new MultiClusterSyncJdbcRepository();
    }
  }
}
