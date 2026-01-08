package com.alipay.sofa.registry.client.consistenthash;

/**
 * @author liqiuliang
 * @create 2022-10-5
 */
public class JdkHashCodeStrategy implements HashStrategy {

    @Override
    public int getHashCode(String origin) {
        return origin.hashCode();
    }

}
