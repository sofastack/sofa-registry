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
package com.alipay.sofa.registry.server.shared;

import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.server.shared.meta.AbstractMetaLeaderExchanger;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.junit.Assert;

public class TestUtils {

  public static void assertRunException(Class<? extends Throwable> eclazz, RunError runnable) {
    try {
      runnable.run();
      Assert.fail();
    } catch (Throwable exception) {
      Assert.assertEquals(eclazz, exception.getClass());
    }
  }

  public interface RunError {
    void run() throws Exception;
  }

  public static Response createSuccessResponse(Object object) {
    Response response =
        new Response() {
          @Override
          public int getStatus() {
            return 200;
          }

          @Override
          public StatusType getStatusInfo() {
            return null;
          }

          @Override
          public Object getEntity() {
            return null;
          }

          @Override
          public Object readEntity(Class aClass) {
            ParaCheckUtil.checkEquals(object.getClass(), aClass, "class");
            return object;
          }

          @Override
          public <T> T readEntity(GenericType<T> genericType) {
            return null;
          }

          @Override
          public <T> T readEntity(Class<T> aClass, Annotation[] annotations) {
            return null;
          }

          @Override
          public <T> T readEntity(GenericType<T> genericType, Annotation[] annotations) {
            return null;
          }

          @Override
          public boolean hasEntity() {
            return false;
          }

          @Override
          public boolean bufferEntity() {
            return false;
          }

          @Override
          public void close() {}

          @Override
          public MediaType getMediaType() {
            return null;
          }

          @Override
          public Locale getLanguage() {
            return null;
          }

          @Override
          public int getLength() {
            return 0;
          }

          @Override
          public Set<String> getAllowedMethods() {
            return null;
          }

          @Override
          public Map<String, NewCookie> getCookies() {
            return null;
          }

          @Override
          public EntityTag getEntityTag() {
            return null;
          }

          @Override
          public Date getDate() {
            return null;
          }

          @Override
          public Date getLastModified() {
            return null;
          }

          @Override
          public URI getLocation() {
            return null;
          }

          @Override
          public Set<Link> getLinks() {
            return null;
          }

          @Override
          public boolean hasLink(String s) {
            return false;
          }

          @Override
          public Link getLink(String s) {
            return null;
          }

          @Override
          public Builder getLinkBuilder(String s) {
            return null;
          }

          @Override
          public MultivaluedMap<String, Object> getMetadata() {
            return null;
          }

          @Override
          public MultivaluedMap<String, String> getStringHeaders() {
            return null;
          }

          @Override
          public String getHeaderString(String s) {
            return null;
          }
        };

    return response;
  }

  public static Response createFailResponse(int failCode) {
    Response response =
        new Response() {
          @Override
          public int getStatus() {
            return failCode;
          }

          @Override
          public StatusType getStatusInfo() {
            return null;
          }

          @Override
          public Object getEntity() {
            return null;
          }

          @Override
          public Object readEntity(Class aClass) {
            return null;
          }

          @Override
          public <T> T readEntity(GenericType<T> genericType) {
            return null;
          }

          @Override
          public <T> T readEntity(Class<T> aClass, Annotation[] annotations) {
            return null;
          }

          @Override
          public <T> T readEntity(GenericType<T> genericType, Annotation[] annotations) {
            return null;
          }

          @Override
          public boolean hasEntity() {
            return false;
          }

          @Override
          public boolean bufferEntity() {
            return false;
          }

          @Override
          public void close() {}

          @Override
          public MediaType getMediaType() {
            return null;
          }

          @Override
          public Locale getLanguage() {
            return null;
          }

          @Override
          public int getLength() {
            return 0;
          }

          @Override
          public Set<String> getAllowedMethods() {
            return null;
          }

          @Override
          public Map<String, NewCookie> getCookies() {
            return null;
          }

          @Override
          public EntityTag getEntityTag() {
            return null;
          }

          @Override
          public Date getDate() {
            return null;
          }

          @Override
          public Date getLastModified() {
            return null;
          }

          @Override
          public URI getLocation() {
            return null;
          }

          @Override
          public Set<Link> getLinks() {
            return null;
          }

          @Override
          public boolean hasLink(String s) {
            return false;
          }

          @Override
          public Link getLink(String s) {
            return null;
          }

          @Override
          public Builder getLinkBuilder(String s) {
            return null;
          }

          @Override
          public MultivaluedMap<String, Object> getMetadata() {
            return null;
          }

          @Override
          public MultivaluedMap<String, String> getStringHeaders() {
            return null;
          }

          @Override
          public String getHeaderString(String s) {
            return null;
          }
        };

    return response;
  }

  public abstract static class MockBaseMetaLeaderExchanger extends AbstractMetaLeaderExchanger {

    public MockBaseMetaLeaderExchanger(String serverType, Logger logger) {
      super(serverType, logger);
    }

    @Override
    protected Collection<String> getMetaServerDomains(String dataCenter) {
      return null;
    }

    @Override
    public int getRpcTimeoutMillis() {
      return 0;
    }

    @Override
    public int getServerPort() {
      return 0;
    }

    @Override
    protected Collection<ChannelHandler> getClientHandlers() {
      return null;
    }

    @Override
    public Channel connect(URL url) {
      return null;
    }
  }
}
