/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.configuration.impl;

import com.alipay.sofa.configuration.model.ConfigFileChangeEvent;
import com.google.gson.Gson;

/**
 *
 * @author lepdou
 * @version $Id: JsonConfigurationFile.java, v 0.1 2019年03月27日 上午11:55 lepdou Exp $
 */
public class JsonConfigurationFile<T> extends AbstractConfigurationFile {
    private static Gson gson = new Gson();

    private T object;

    public JsonConfigurationFile(String jsonStr, Class<T> classOfT) {
        this.content = jsonStr;

        if (content != null) {
            object = gson.fromJson(content, classOfT);
        }
    }

    public void updateJsonStr(String jsonStr, Class<T> classOfT) {
        if ((jsonStr != null && jsonStr.equals(content))) {
            return;
        }

        ConfigFileChangeEvent event = new ConfigFileChangeEvent(content, jsonStr);

        this.content = jsonStr;

        if (content != null) {
            object = gson.fromJson(content, classOfT);
        }

        fireConfigChange(event);
    }

    public T getObject() {
        return object;
    }

}