package com.alipay.sofa.registry.common.model.sessionserver;

import com.alipay.sofa.registry.util.StringFormatter;

import java.io.Serializable;

/**
 * @author huicha
 * @date 2024/12/23
 */
public class QueryPublisherRequest implements Serializable  {

    private static final long serialVersionUID = 5295572570779995725L;

    private final String dataInfoId;

    public QueryPublisherRequest(String dataInfoId) {
        this.dataInfoId = dataInfoId;
    }

    public String getDataInfoId() {
        return dataInfoId;
    }

    @Override
    public String toString() {
        return StringFormatter.format("QueryPublisherRequest={}}", dataInfoId);
    }

}
