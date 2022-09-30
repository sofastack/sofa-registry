package com.alipay.sofa.registry.client.provider;

import com.alipay.sofa.registry.client.remoting.ServerNode;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author liqiuliang
 * @create 2022-09-30 21:43
 */
public class ConsistentHashTest {
    @Test
    public void consistentHashTest() {
        // 生成随机数进行测试
        Map<String, Integer> resMap = new HashMap<>();
        List<ServerNode> allServers = new LinkedList<>(Arrays.asList(
                new DefaultServerNode("192.168.0.1", "", 0, null),
                new DefaultServerNode("192.168.0.0", "", 0, null),
                new DefaultServerNode("192.168.0.2", "", 0, null),
                new DefaultServerNode("192.168.0.3", "", 0, null),
                new DefaultServerNode("192.168.0.4", "", 0, null))
        );
        ConsistentHash consistentHash = new ConsistentHash(allServers);
        for (int i = 0; i < 100000; i++) {
            Integer widgetId = i;
            String group = consistentHash.choose(widgetId.toString());
            if (resMap.containsKey(group)) {
                resMap.put(group, resMap.get(group) + 1);
            } else {
                resMap.put(group, 1);
            }
        }

        resMap.forEach(
                (k, v) -> {
                    System.out.println("group " + k + ": " + v + "(" + v / 100000.0D + "%)");
                }
        );
    }
}