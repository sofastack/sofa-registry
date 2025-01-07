package com.alipay.sofa.registry.server.session.remoting.console.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.PublisherUtils;
import com.alipay.sofa.registry.common.model.sessionserver.QueryPublisherRequest;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.bootstrap.ExecutorManager;
import com.alipay.sofa.registry.server.session.store.DataStore;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * @author huicha
 * @date 2024/12/23
 */
public class QueryPublisherRequestHandler extends AbstractConsoleHandler<QueryPublisherRequest> {

  @Autowired
  protected DataStore sessionDataStore;

  @Override
  public Object doHandle(Channel channel, QueryPublisherRequest request) {
    Collection<Publisher> publishers = sessionDataStore.getDatas(request.getDataInfoId());
    return new GenericResponse().fillSucceed(PublisherUtils.convert(publishers));
  }

  @Override
  public Object buildFailedResponse(String msg) {
    return new GenericResponse().fillFailed(msg);
  }

  @Override
  public Class interest() {
    return QueryPublisherRequest.class;
  }

  @VisibleForTesting
  public QueryPublisherRequestHandler setSessionDataStore(DataStore sessionDataStore) {
    this.sessionDataStore = sessionDataStore;
    return this;
  }

  @VisibleForTesting
  public QueryPublisherRequestHandler setExecutorManager(ExecutorManager executorManager) {
    this.executorManager = executorManager;
    return this;
  }

}
