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
package com.alipay.sofa.registry.consistency.hash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Consistent hash implementation, use {@link HashNode} as physical node.
 * @param <T> hash node
 * @author zhuoyu.sjw
 * @version $Id : ConsistentHash.java, v 0.1 2016-08-24 11:10 zhuoyu.sjw Exp $$ 
 */
public class ConsistentHash<T extends HashNode> {

    /** character used to separate virtual nodes */
    private static final char           SIGN   = '#';

    /** number of virtual nodes for one real node */
    private final int                   numberOfReplicas;

    /** real nodes */
    private Set<HashNode>               realNodes;

    /** hash function */
    private final HashFunction          hashFunction;

    /** */
    private final SortedMap<Integer, T> circle = new TreeMap<>();

    /**
     * Instantiates a new Consistent hash.
     *
     * @param numberOfReplicas the number of replicas  
     * @param nodes the nodes
     */
    public ConsistentHash(int numberOfReplicas, Collection<T> nodes) {
        this(new MD5HashFunction(), numberOfReplicas, nodes);
    }

    /**
     * Instantiates a new Consistent hash.
     *
     * @param hashFunction the hash function  
     * @param numberOfReplicas the number of replicas  
     * @param nodes the nodes
     */
    public ConsistentHash(HashFunction hashFunction, int numberOfReplicas, Collection<T> nodes) {
        this.realNodes = new HashSet<>();
        this.hashFunction = hashFunction;
        this.numberOfReplicas = numberOfReplicas;
        for (T node : nodes) {
            addNode(node);
        }
    }

    /**
     * Add a new node to the consistent hash
     *
     * This is not thread safe.
     * @param node the node
     */
    private void addNode(T node) {
        realNodes.add(node);
        for (int i = 0; i < numberOfReplicas; i++) {
            // The string addition forces each replica to have different hash
            circle.put(hashFunction.hash(node.getNodeName() + SIGN + i), node);
        }
    }

    /**
     * This returns the closest node for the object. If the object is the node it
     * should be an exact hit, but if it is a value traverse to find closest
     * subsequent node.
     * @param key the key 
     * @return node for
     */
    public T getNodeFor(Object key) {
        if (circle.isEmpty()) {
            return null;
        }
        int hash = hashFunction.hash(key);
        T node = circle.get(hash);

        if (node == null) {
            // inexact match -- find the next value in the circle
            SortedMap<Integer, T> tailMap = circle.tailMap(hash);
            hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            node = circle.get(hash);
        }
        return node;
    }

    /**
     * This returns the closest n unique nodes in order for the object.
     *
     * This will return a list that has all nodes if n > number of nodes.
     *
     * @param key the key 
     * @param n the n 
     * @return the n unique nodes for
     */
    public List<T> getNUniqueNodesFor(Object key, int n) {
        if (circle.isEmpty()) {
            return Collections.emptyList();
        }

        if (n > realNodes.size()) {
            n = realNodes.size();
        }

        List<T> list = new ArrayList<>(n);
        int hash = hashFunction.hash(key);
        for (int i = 0; i < n; i++) {
            if (!circle.containsKey(hash)) {
                // go to next element.
                SortedMap<Integer, T> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }
            T candidate = circle.get(hash);
            if (!list.contains(candidate)) {
                list.add(candidate);
            } else {
                i--; // try again.
            }
            // find the next element in the circle
            hash++;
        }
        return list;
    }
}
