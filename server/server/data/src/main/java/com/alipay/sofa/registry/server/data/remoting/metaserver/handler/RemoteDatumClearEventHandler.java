/** Alipay.com Inc. Copyright (c) 2004-2022 All Rights Reserved. */
package com.alipay.sofa.registry.server.data.remoting.metaserver.handler;

import com.alipay.sofa.registry.common.model.CommonResponse;
import com.alipay.sofa.registry.common.model.Node.NodeType;
import com.alipay.sofa.registry.common.model.metaserver.RemoteDatumClearEvent;
import com.alipay.sofa.registry.common.model.metaserver.RemoteDatumClearEvent.DatumType;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.data.multi.cluster.storage.MultiClusterDatumService;
import com.alipay.sofa.registry.server.shared.remoting.AbstractClientHandler;
import com.alipay.sofa.registry.util.ParaCheckUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xiaojian.xj
 * @version : RemoteDatumClearEventHandler.java, v 0.1 2022年09月08日 17:23 xiaojian.xj Exp $
 */
public class RemoteDatumClearEventHandler extends AbstractClientHandler<RemoteDatumClearEvent> {

  @Autowired private ThreadPoolExecutor metaNodeExecutor;

  @Autowired private MultiClusterDatumService multiClusterDatumService;

  /**
   * return processor request class name
   *
   * @return
   */
  @Override
  public Class interest() {
    return RemoteDatumClearEvent.class;
  }

  @Override
  protected NodeType getConnectNodeType() {
    return NodeType.META;
  }

  @Override
  public void checkParam(RemoteDatumClearEvent request) {
    ParaCheckUtil.checkNotNull(request, "RemoteDatumClearEvent");
    ParaCheckUtil.checkNotBlank(request.getRemoteDataCenter(), "RemoteDataCenter");
    ParaCheckUtil.checkNotNull(request.getDatumType(), "DatumType");
    if (request.getDatumType() == DatumType.GROUP) {
      ParaCheckUtil.checkNotBlank(request.getGroup(), "Group");
    } else {
      ParaCheckUtil.checkNotBlank(request.getDataInfoId(), "DataInfoId");
    }
  }

  /**
   * execute
   *
   * @param channel
   * @param request
   * @return
   */
  @Override
  public Object doHandle(Channel channel, RemoteDatumClearEvent request) {
    multiClusterDatumService.clear(request);
    return CommonResponse.buildSuccessResponse();
  }

  @Override
  public CommonResponse buildFailedResponse(String msg) {
    return CommonResponse.buildFailedResponse(msg);
  }

  @Override
  public Executor getExecutor() {
    return metaNodeExecutor;
  }
}
