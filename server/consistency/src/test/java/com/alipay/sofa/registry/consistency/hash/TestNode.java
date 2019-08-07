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

/**
 * The type Test node.
 * @author zhuoyu.sjw
 * @version $Id : TestNode.java, v 0.1 2018-03-07 11:23 zhuoyu.sjw Exp $$
 */
public class TestNode implements HashNode {

    private String nodeName;

    /**
     * Instantiates a new Test node.
     *
     * @param nodeName the node name
     */
    public TestNode(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Gets node name.
     *
     * @return the node name
     */
    @Override
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Equals boolean.
     *
     * @param o the o 
     * @return the boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestNode)) {
            return false;
        }

        TestNode testNode = (TestNode) o;

        return nodeName != null ? nodeName.equals(testNode.nodeName) : testNode.nodeName == null;
    }

    /**
     * Hash code int.
     *
     * @return the int
     */
    @Override
    public int hashCode() {
        return nodeName != null ? nodeName.hashCode() : 0;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "TestNode{" + "nodeName='" + nodeName + '\'' + '}';
    }
}
