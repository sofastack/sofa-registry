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
package com.alipay.sofa.registry.jdbc.decrypt;

import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class DecryptorManager {

  @Autowired private Collection<Decryptor> decryptors;

  private static final Logger LOG = LoggerFactory.getLogger(DecryptorManager.class);

  @VisibleForTesting
  public void setDecryptors(Collection<Decryptor> decryptors) {
    this.decryptors = decryptors;
  }

  public String decrypt(String matchedType, String cipher) {
    List<Decryptor> enabled =
        decryptors.stream()
            .filter(Decryptor::enabled)
            .filter(decryptor -> StringUtils.equals(decryptor.type(), matchedType))
            .collect(Collectors.toList());
    if (enabled.size() > 0) {
      LOG.info("use {} decryptor", enabled.get(0).name());
      return enabled.get(0).decrypt(cipher);
    }
    LOG.info("no enabled decryptor, return raw input");
    return cipher;
  }
}
