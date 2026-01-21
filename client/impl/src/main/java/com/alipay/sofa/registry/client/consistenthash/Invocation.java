package com.alipay.sofa.registry.client.consistenthash;

/**
 * @author liqiuliang
 * @create 2022-10-5
 */
public class Invocation {
    public Invocation() {
    }

    public Invocation(String hashKey) {
        this.hashKey = hashKey;
    }

    private String hashKey;

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }
}
