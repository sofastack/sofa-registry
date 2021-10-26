/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.sofa.registry.server.shared.util;

import com.alipay.sofa.common.profile.StringUtil;
import com.alipay.sofa.registry.common.model.console.PersistenceData;
import com.alipay.sofa.registry.store.api.DBResponse;
import com.alipay.sofa.registry.store.api.OperationStatus;

/**
 *
 * @author xiaojian.xj
 * @version : PersistenceParser.java, v 0.1 2021年10月27日 14:27 xiaojian.xj Exp $
 */
public class PersistenceDataParser {

    public static boolean parse2BoolIgnoreCase(PersistenceData persistenceData, boolean defaultValue) {
        if (persistenceData == null || StringUtil.isBlank(persistenceData.getData())) {
            return defaultValue;
        }
        return Boolean.parseBoolean(persistenceData.getData());
    }

    public static boolean parse2BoolIgnoreCase(DBResponse<PersistenceData> response, boolean defaultValue) {
        if (response == null || response.getEntity() == null
                || response.getOperationStatus()!= OperationStatus.SUCCESS) {
            return defaultValue;
        }
        return parse2BoolIgnoreCase(response.getEntity(), defaultValue);

    }
}