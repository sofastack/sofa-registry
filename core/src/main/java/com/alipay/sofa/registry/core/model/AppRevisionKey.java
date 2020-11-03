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
package com.alipay.sofa.registry.core.model;

import java.io.Serializable;
import java.util.Objects;

public class AppRevisionKey implements Comparable<AppRevisionKey>, Serializable {
    String appname;
    String revision;

    public AppRevisionKey(String appname, String revision) {
        this.appname = appname;
        this.revision = revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AppRevisionKey that = (AppRevisionKey) o;
        return Objects.equals(appname, that.appname) && Objects.equals(revision, that.revision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appname, revision);
    }

    @Override
    public String toString() {
        return appname + "@" + revision;
    }

    @Override
    public int compareTo(AppRevisionKey o) {
        return toString().compareTo(o.toString());
    }
}
