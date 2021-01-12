package com.alipay.sofa.registry.comparator;

import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public interface Comparator<T> {

    Set<T> getAdded();

    Set<T> getRemoved();

    Set<Pair<T, T>> getModified();

    void accept(ComparatorVisitor<T> visitor);

    void acceptAdded(ComparatorVisitor<T> visitor);

    void acceptRemoved(ComparatorVisitor<T> visitor);

    void acceptRemains(ComparatorVisitor<T> visitor);

    boolean hasAnyChange();

    int totalChange();
}
