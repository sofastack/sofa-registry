/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.registry.common.model.map;

/**
 * Copy from org.jboss.netty.util.internal.ReusableIterator
 *
 * @author kezhu.wukz
 * @version $Id: ReusableIterator.java, v 0.1 2019-10-19 13:18 kezhu.wukz Exp $
 */
import java.util.Iterator;

public interface ReusableIterator<E> extends Iterator<E> {
    void rewind();
}
