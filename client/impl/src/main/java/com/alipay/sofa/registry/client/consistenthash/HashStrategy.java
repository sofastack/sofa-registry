package com.alipay.sofa.registry.client.consistenthash;

/**
 * @author liqiuliang
 * @create 2022-10-5
 */
public interface HashStrategy {
    int getHashCode(String origin);
}
