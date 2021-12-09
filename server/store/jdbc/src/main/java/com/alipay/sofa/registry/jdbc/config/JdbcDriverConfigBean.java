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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojian.xj
 * @version $Id: JdbcDriverConfigBean.java, v 0.1 2021年01月17日 16:39 xiaojian.xj Exp $
 */
@ConfigurationProperties(prefix = JdbcDriverConfigBean.PRE_FIX)
public class JdbcDriverConfigBean implements JdbcDriverConfig {

  public static final String PRE_FIX = "jdbc";

  private String driverClassName = "com.mysql.jdbc.Driver";

  private String url =
      "jdbc:mysql://127.0.0.1:3306/registrymetadb?useUnicode=true&characterEncoding=utf8";

  private String username = "username";

  private String password = "password";

  private int slowSqlMillis = 1000;

  private String typeAliasesPackage;

  private String[] mapperLocations = {"classpath:mapper/*.xml"};

  private int minIdle = 5;

  private int maxActive = 25;

  private int maxWait = 2000;

  /**
   * Getter method for property <tt>driverClassName</tt>.
   *
   * @return property value of driverClassName
   */
  @Override
  public String getDriverClassName() {
    return driverClassName;
  }

  /**
   * Setter method for property <tt>driverClassName</tt>.
   *
   * @param driverClassName value to be assigned to property driverClassName
   */
  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  /**
   * Getter method for property <tt>url</tt>.
   *
   * @return property value of url
   */
  @Override
  public String getUrl() {
    return url;
  }

  /**
   * Setter method for property <tt>url</tt>.
   *
   * @param url value to be assigned to property url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Getter method for property <tt>username</tt>.
   *
   * @return property value of username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Setter method for property <tt>username</tt>.
   *
   * @param username value to be assigned to property username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Getter method for property <tt>password</tt>.
   *
   * @return property value of password
   */
  @Override
  public String getPassword() {
    return password;
  }

  /**
   * Setter method for property <tt>password</tt>.
   *
   * @param password value to be assigned to property password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Getter method for property <tt>typeAliasesPackage</tt>.
   *
   * @return property value of typeAliasesPackage
   */
  @Override
  public String getTypeAliasesPackage() {
    return typeAliasesPackage;
  }

  /**
   * Setter method for property <tt>typeAliasesPackage</tt>.
   *
   * @param typeAliasesPackage value to be assigned to property typeAliasesPackage
   */
  public void setTypeAliasesPackage(String typeAliasesPackage) {
    this.typeAliasesPackage = typeAliasesPackage;
  }

  /**
   * Getter method for property <tt>mapperLocations</tt>.
   *
   * @return property value of mapperLocations
   */
  @Override
  public String[] getMapperLocations() {
    return mapperLocations;
  }

  /**
   * Setter method for property <tt>mapperLocations</tt>.
   *
   * @param mapperLocations value to be assigned to property mapperLocations
   */
  public void setMapperLocations(String[] mapperLocations) {
    this.mapperLocations = mapperLocations;
  }

  /**
   * Getter method for property <tt>minIdle</tt>.
   *
   * @return property value of minIdle
   */
  @Override
  public int getMinIdle() {
    return minIdle;
  }

  /**
   * Setter method for property <tt>minIdle</tt>.
   *
   * @param minIdle value to be assigned to property minIdle
   */
  public void setMinIdle(int minIdle) {
    this.minIdle = minIdle;
  }

  /**
   * Getter method for property <tt>maxActive</tt>.
   *
   * @return property value of maxActive
   */
  @Override
  public int getMaxActive() {
    return maxActive;
  }

  /**
   * Setter method for property <tt>maxActive</tt>.
   *
   * @param maxActive value to be assigned to property maxActive
   */
  public void setMaxActive(int maxActive) {
    this.maxActive = maxActive;
  }

  /**
   * Getter method for property <tt>maxWait</tt>.
   *
   * @return property value of maxWait
   */
  @Override
  public int getMaxWait() {
    return maxWait;
  }

  /**
   * Setter method for property <tt>maxWait</tt>.
   *
   * @param maxWait value to be assigned to property maxWait
   */
  public void setMaxWait(int maxWait) {
    this.maxWait = maxWait;
  }

  /**
   * Getter method for property <tt>slowSqlMillis</tt>.
   *
   * @return property value of slowSqlMillis
   */
  public int getSlowSqlMillis() {
    return slowSqlMillis;
  }

  /**
   * Setter method for property <tt>slowSqlMillis</tt>.
   *
   * @param slowSqlMillis value to be assigned to property slowSqlMillis
   */
  public void setSlowSqlMillis(int slowSqlMillis) {
    this.slowSqlMillis = slowSqlMillis;
  }
}
