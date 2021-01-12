package com.alipay.sofa.registry.comparator;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public interface ComparatorVisitor<T> {

    void visitAdded(T added);

    void visitModified(Pair<T, T> modified);

    void visitRemoved(T removed);

    void visitRemaining(T remain);
}
