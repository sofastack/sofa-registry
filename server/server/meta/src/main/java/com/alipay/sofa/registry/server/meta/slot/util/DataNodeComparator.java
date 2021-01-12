package com.alipay.sofa.registry.server.meta.slot.util;

import com.alipay.sofa.registry.comparator.AbstractComparator;
import com.alipay.sofa.registry.comparator.Triple;

import java.util.Collection;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 *
 * String stands for data node ip
 *
 * as the situation is mainly about rebalance the slot-table
 * which, data node is stored just as IP address (String)
 */
public class DataNodeComparator extends AbstractComparator<String> {

    private Collection<String> prev;

    private Collection<String> current;

    public DataNodeComparator(Collection<String> prev, Collection<String> current) {
        this.prev = prev;
        this.current = current;
        compare();
    }

    public void compare() {
        Triple<Set<String>, Set<String>, Set<String>> triple = getDiff(prev, current);
        this.added = triple.getFirst();
        this.remainings = triple.getMiddle();
        this.removed = triple.getLast();
        this.count = this.added.size() + this.removed.size();
    }
}
