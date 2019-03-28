/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.configuration.model;

/**
 *
 * @author lepdou
 * @version $Id: ConfigFileChangeEvent.java, v 0.1 2019年03月27日 上午11:06 lepdou Exp $
 */
public class ConfigFileChangeEvent {

    private final String oldValue;
    private final String newValue;

    public ConfigFileChangeEvent(String oldValue, String newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        return "ConfigFileChangeEvent{" +
                "oldValue='" + oldValue + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }
}