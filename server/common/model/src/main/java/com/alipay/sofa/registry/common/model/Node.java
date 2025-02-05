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
package com.alipay.sofa.registry.common.model;

import com.alipay.sofa.registry.common.model.store.URL;
import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 * @author shangyu.wh
 * @version $Id: node.java, v 0.1 2017-11-28 11:59 shangyu.wh Exp $
 */
public interface Node extends Serializable {

  /** node type enum */
  enum NodeType {
    CLIENT,
    SESSION,
    META,
    DATA,
    CONSOLE,
    ;

    public static NodeType codeOf(String type) {
      if (StringUtils.isBlank(type)) {
        return null;
      }
      for (NodeType nodeType : NodeType.values()) {
        if (StringUtils.containsIgnoreCase(type, nodeType.name())) {
          return nodeType;
        }
      }
      return null;
    }
  }

  /**
   * get node type
   *
   * @return NodeType
   */
  NodeType getNodeType();

  /**
   * get node url
   *
   * @return URL
   */
  URL getNodeUrl();
}
