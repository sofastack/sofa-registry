/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.alipay.sofa.registry.server.meta.resource;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author xiaojian.xj
 * @version : AuthChecker.java, v 0.1 2021年10月18日 14:22 xiaojian.xj Exp $
 */
public final class AuthChecker {

    private static final String AUTH_TOKEN = "6c62lk8dmQoE5B8X";

    public static boolean authCheck(String token) {
        return StringUtils.equals(AUTH_TOKEN, token);
    }
}