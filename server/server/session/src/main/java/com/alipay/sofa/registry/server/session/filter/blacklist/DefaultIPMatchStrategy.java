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
package com.alipay.sofa.registry.server.session.filter.blacklist;

import com.alipay.sofa.registry.server.session.filter.IPMatchStrategy;
import com.alipay.sofa.registry.server.session.provideData.FetchBlackListService;
import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;

/**
 * @author shangyu.wh
 * @version 1.0: DefaultIPMatchStrategy.java, v 0.1 2019-06-19 22:16 shangyu.wh Exp $
 */
public class DefaultIPMatchStrategy implements IPMatchStrategy<String> {

  @Resource private FetchBlackListService fetchBlackListService;

  @Override
  public boolean match(String IP, Supplier<String> getOperatorType) {
    return match(getOperatorType.get(), IP);
  }

  private boolean match(String type, String matchPattern) {

    List<BlacklistConfig> configList = fetchBlackListService.getBlacklistConfigList();
    for (BlacklistConfig blacklistConfig : configList) {

      // 如黑名单类型不匹配则跳过
      if (!StringUtils.equals(type, blacklistConfig.getType())) {
        continue;
      }

      List<MatchType> matchTypeList = blacklistConfig.getMatchTypes();

      // 匹配规则为空跳过
      if (null == matchTypeList || matchTypeList.size() == 0) {
        continue;
      }

      for (MatchType matchType : matchTypeList) {
        if (null == matchType) {
          continue;
        }

        if (BlacklistConstants.IP_FULL.equals(matchType.getType())) {
          // IP 全匹配时判断当前发布者IP是否在IP列表中，如命中则拒绝发布
          @SuppressWarnings("unchecked")
          Set<String> patterns = matchType.getPatternSet();

          if (null == patterns || patterns.size() == 0) {
            continue;
          }

          if (patterns.contains(matchPattern)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Setter method for property <tt>fetchBlackListService</tt>.
   *
   * @param fetchBlackListService value to be assigned to property fetchBlackListService
   */
  @VisibleForTesting
  public void setFetchBlackListService(FetchBlackListService fetchBlackListService) {
    this.fetchBlackListService = fetchBlackListService;
  }
}
