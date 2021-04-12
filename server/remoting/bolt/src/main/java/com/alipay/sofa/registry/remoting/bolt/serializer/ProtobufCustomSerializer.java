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
package com.alipay.sofa.registry.remoting.bolt.serializer;

import com.alipay.remoting.CustomSerializer;
import com.alipay.remoting.InvokeContext;
import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.exception.DeserializationException;
import com.alipay.remoting.rpc.RequestCommand;
import com.alipay.remoting.rpc.ResponseCommand;
import com.alipay.remoting.rpc.protocol.RpcRequestCommand;
import com.alipay.remoting.rpc.protocol.RpcResponseCommand;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;

/**
 * The type Protobuf custom serializer.
 *
 * @author zhuoyu.sjw
 * @version $Id : ProtobufCustomSerializer.java, v 0.1 2018年03月21日 6:14 PM bystander Exp $
 */
public class ProtobufCustomSerializer implements CustomSerializer {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProtobufCustomSerializer.class);

  /** The constant PROTOCOL_PROTOBUF. */
  public static final byte PROTOCOL_PROTOBUF = 11;

  @Override
  public <T extends RequestCommand> boolean serializeHeader(
      T request, InvokeContext invokeContext) {
    return false;
  }

  @Override
  public <T extends ResponseCommand> boolean serializeHeader(T response) {
    return false;
  }

  @Override
  public <T extends RequestCommand> boolean deserializeHeader(T request) {
    return false;
  }

  @Override
  public <T extends ResponseCommand> boolean deserializeHeader(
      T response, InvokeContext invokeContext) {
    return false;
  }

  @Override
  public <T extends RequestCommand> boolean serializeContent(
      T request, InvokeContext invokeContext) {
    if (request instanceof RpcRequestCommand) {
      try {
        RpcRequestCommand requestCommand = (RpcRequestCommand) request;
        Object requestObject = requestCommand.getRequestObject();
        request.setContent(ProtobufSerializer.getInstance().serialize(requestObject));
        return true;
      } catch (CodecException e) {
        LOGGER.error("[bolt] encode request error, {}", request, e);
      }
    }
    return false;
  }

  @Override
  public <T extends ResponseCommand> boolean serializeContent(T response) {
    if (response instanceof RpcResponseCommand) {
      if (response.getSerializer() == PROTOCOL_PROTOBUF) {
        try {
          Object appResponse = ((RpcResponseCommand) response).getResponseObject();
          if (appResponse instanceof Throwable) {
            // return throwable.message as result
            response.setContent(
                ProtobufSerializer.getInstance().serialize(((Throwable) appResponse).getMessage()));
            // TODO should return true ?
            return false;
          } else {
            response.setContent(ProtobufSerializer.getInstance().serialize(appResponse));
            return true;
          }
        } catch (CodecException e) {
          LOGGER.error("[bolt] encode response error, {}", response, e);
        }
      }
    }

    return false;
  }

  @Override
  public <T extends RequestCommand> boolean deserializeContent(T request)
      throws DeserializationException {
    if (request instanceof RpcRequestCommand) {
      RpcRequestCommand requestCommand = (RpcRequestCommand) request;
      if (requestCommand.getSerializer() == PROTOCOL_PROTOBUF) {
        try {
          Object pbReq = null;
          byte[] content = requestCommand.getContent();
          if (content != null && content.length != 0) {
            pbReq =
                ProtobufSerializer.getInstance()
                    .deserialize(content, requestCommand.getRequestClass());
          }
          requestCommand.setRequestObject(pbReq);
          return true;
        } catch (DeserializationException e) {
          throw e;
        } catch (Exception e) {
          throw new DeserializationException(e.getMessage(), e);
        }
      }
    }
    return false;
  }

  @Override
  public <T extends ResponseCommand> boolean deserializeContent(
      T response, InvokeContext invokeContext) throws DeserializationException {
    if (response instanceof RpcResponseCommand) {
      RpcResponseCommand responseCommand = (RpcResponseCommand) response;
      if (response.getSerializer() == PROTOCOL_PROTOBUF) {
        try {
          Object pbReq = null;

          byte[] content = responseCommand.getContent();
          if (content != null && content.length != 0) {
            pbReq =
                ProtobufSerializer.getInstance()
                    .deserialize(content, ((RpcResponseCommand) response).getResponseClass());
          }
          responseCommand.setResponseObject(pbReq);
          return true;
        } catch (DeserializationException e) {
          throw e;
        } catch (Exception e) {
          throw new DeserializationException(e.getMessage(), e);
        }
      }
    }

    return false;
  }
}
