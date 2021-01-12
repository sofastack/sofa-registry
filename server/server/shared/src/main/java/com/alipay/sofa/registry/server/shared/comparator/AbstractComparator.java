/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.shared.comparator;

import com.alipay.sofa.registry.common.model.Tuple;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author chen.zhu
 * <p>
 * Jan 12, 2021
 */
public class AbstractComparator<T> implements Comparator<T> {
    protected Set<T>           added      = new HashSet<>();
    protected Set<T>           removed    = new HashSet<>();
    protected Set<T>           remainings = new HashSet<>();
    protected Set<Tuple<T, T>> modified   = new HashSet<>();
    protected volatile int     count;

    @Override
    public Set<T> getAdded() {
        return added;
    }

    @Override
    public Set<T> getRemoved() {
        return removed;
    }

    @Override
    public Set<Tuple<T, T>> getModified() {
        return modified;
    }

    @Override
    public void accept(ComparatorVisitor<T> visitor) {
        for (T t : added) {
            visitor.visitAdded(t);
        }

        for (T t : removed) {
            visitor.visitRemoved(t);
        }

        for (Tuple<T, T> mod : modified) {
            visitor.visitModified(mod);
        }
    }

    @Override
    public void acceptAdded(ComparatorVisitor<T> visitor) {
        for (T t : added) {
            visitor.visitAdded(t);
        }
    }

    @Override
    public void acceptRemoved(ComparatorVisitor<T> visitor) {
        for (T t : removed) {
            visitor.visitRemoved(t);
        }
    }

    @Override
    public void acceptRemains(ComparatorVisitor<T> visitor) {
        for (T t : remainings) {
            visitor.visitRemaining(t);
        }
    }

    @Override
    public boolean hasAnyChange() {
        return count > 0;
    }

    @Override
    public int totalChange() {
        return count;
    }

    protected <Type> Triple<Set<Type>, Set<Type>, Set<Type>> getDiff(Collection<Type> current,
                                                                     Collection<Type> future) {

        Set<Type> added = new HashSet<>(future);
        Set<Type> remaining = new HashSet<>(future);
        Set<Type> deleted = new HashSet<>(current);

        added.removeAll(deleted);
        remaining.retainAll(deleted);
        deleted.removeAll(future);

        return new Triple<Set<Type>, Set<Type>, Set<Type>>(added, remaining, deleted);
    }
}
