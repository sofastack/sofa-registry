/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.configuration;

/**
 *
 * @author lepdou
 * @version $Id: ConfigurationFile.java, v 0.1 2019年03月27日 上午11:04 lepdou Exp $
 */
public interface ConfigurationFile {

    String getContent();

    boolean hasContent();

    void addChangeListener(ConfigFileChangeListener listener);

    void removeChangeListener(ConfigFileChangeListener listener);
}