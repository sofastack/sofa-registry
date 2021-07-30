/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.sofa.registry.server.session.remoting.console.handler;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerQueryRequest;
import com.alipay.sofa.registry.common.model.sessionserver.ClientManagerResp;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.server.session.providedata.FetchClientOffAddressService;

import javax.annotation.Resource;
import java.util.Set;

/**
 *
 * @author xiaojian.xj
 * @version : GetClientManagerRequestHandler.java, v 0.1 2021年07月31日 17:31 xiaojian.xj Exp $
 */
public class GetClientManagerRequestHandler extends AbstractConsoleHandler<ClientManagerQueryRequest> {

    @Resource
    private FetchClientOffAddressService fetchClientOffAddressService;

    @Override
    public GenericResponse<ClientManagerResp> doHandle(Channel channel, ClientManagerQueryRequest request) {
        Set<String> clientOffAddress = fetchClientOffAddressService.getClientOffAddress();
        return new GenericResponse<ClientManagerResp>().fillSucceed(new ClientManagerResp(true, clientOffAddress));
    }

    @Override
    public Class interest() {
        return ClientManagerQueryRequest.class;
    }

    @Override
    public Object buildFailedResponse(String msg) {
        return new GenericResponse<ClientManagerResp>().fillFailed(msg);
    }
}