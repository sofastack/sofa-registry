/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.alipay.sofa.registry.common.model.metaserver;

import com.alipay.sofa.registry.util.ParaCheckUtil;

import java.io.Serializable;

/**
 *
 * @author xiaojian.xj
 * @version : RemoteDatumClearEvent.java, v 0.1 2022年09月08日 17:05 xiaojian.xj Exp $
 */
public class RemoteDatumClearEvent implements Serializable {

    private final String remoteDataCenter;

    private final DatumType datumType;

    private final String dataInfoId;

    private final String group;

    public RemoteDatumClearEvent(String remoteDataCenter,
                                 DatumType datumType, String dataInfoId, String group) {
        this.remoteDataCenter = remoteDataCenter;
        this.datumType = datumType;
        this.dataInfoId = dataInfoId;
        this.group = group;
    }

    public static RemoteDatumClearEvent dataInfoIdEvent(String remoteDataCenter,
                                                        String dataInfoId) {
        ParaCheckUtil.checkNotBlank(remoteDataCenter, "remoteDataCenter");
        ParaCheckUtil.checkNotBlank(dataInfoId, "dataInfoId");
        return new RemoteDatumClearEvent(remoteDataCenter, DatumType.DATA_INFO_ID, dataInfoId, null);
    }

    public static RemoteDatumClearEvent groupEvent(String remoteDataCenter,
                                                        String group) {
        ParaCheckUtil.checkNotBlank(remoteDataCenter, "remoteDataCenter");
        ParaCheckUtil.checkNotBlank(group, "group");
        return new RemoteDatumClearEvent(remoteDataCenter, DatumType.GROUP, null, group);
    }

    /**
     * Getter method for property <tt>remoteDataCenter</tt>.
     *
     * @return property value of remoteDataCenter
     */
    public String getRemoteDataCenter() {
        return remoteDataCenter;
    }

    /**
     * Getter method for property <tt>datumType</tt>.
     *
     * @return property value of datumType
     */
    public DatumType getDatumType() {
        return datumType;
    }

    /**
     * Getter method for property <tt>dataInfoId</tt>.
     *
     * @return property value of dataInfoId
     */
    public String getDataInfoId() {
        return dataInfoId;
    }

    /**
     * Getter method for property <tt>group</tt>.
     *
     * @return property value of group
     */
    public String getGroup() {
        return group;
    }

    public enum DatumType {
        DATA_INFO_ID,
        GROUP,;
    }

    @Override
    public String toString() {
        return "RemoteDatumClearEvent{" +
                "remoteDataCenter='" + remoteDataCenter + '\'' +
                ", datumType=" + datumType +
                ", dataInfoId='" + dataInfoId + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
