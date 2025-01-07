package com.alipay.sofa.registry.server.session.wrapper;

import com.alipay.sofa.registry.common.model.store.BaseInfo;
import com.alipay.sofa.registry.common.model.store.Publisher;
import com.alipay.sofa.registry.common.model.store.StoreData.DataType;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInterceptor;
import com.alipay.sofa.registry.common.model.wrapper.WrapperInvocation;
import com.alipay.sofa.registry.server.session.providedata.FetchDataInfoIDBlackListService;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author huicha
 * @date 2024/12/16
 */
public class DataInfoIDBlacklistWrapperInterceptor implements WrapperInterceptor<RegisterInvokeData, Boolean> {

  @Autowired
  private FetchDataInfoIDBlackListService fetchDataInfoIDBlackListService;

  @Override
  public Boolean invokeCodeWrapper(WrapperInvocation<RegisterInvokeData, Boolean> invocation) throws Exception {
    RegisterInvokeData registerInvokeData = invocation.getParameterSupplier().get();
    BaseInfo storeData = (BaseInfo) registerInvokeData.getStoreData();

    // 这里我们只拦截 Publisher
    if (DataType.PUBLISHER == storeData.getDataType()) {
      Publisher publisher = (Publisher) storeData;
      String dataInfoId = publisher.getDataInfoId();
      if (this.fetchDataInfoIDBlackListService.isInBlackList(dataInfoId)) {
        // 命中规则，跳过 Pub
        return true;
      } else {
        // 没命中则继续处理
        return invocation.proceed();
      }
    }

    // 非 Publisher 也继续处理
    return invocation.proceed();
  }

  @Override
  public int getOrder() {
    return 400;
  }

  @VisibleForTesting
  public DataInfoIDBlacklistWrapperInterceptor setFetchDataInfoIDBlackListService(FetchDataInfoIDBlackListService fetchDataInfoIDBlackListService) {
    this.fetchDataInfoIDBlackListService = fetchDataInfoIDBlackListService;
    return this;
  }

}
