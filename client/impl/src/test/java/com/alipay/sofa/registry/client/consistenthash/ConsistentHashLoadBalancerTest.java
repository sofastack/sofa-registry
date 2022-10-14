package com.alipay.sofa.registry.client.consistenthash;

import com.alipay.sofa.registry.client.provider.DefaultServerNode;
import com.alipay.sofa.registry.client.remoting.ServerNode;
import org.junit.Test;

import java.util.*;

/**
 * @author liqiuliang
 * @create 2022-10-5
 */
public class ConsistentHashLoadBalancerTest {
    @Test
    public void testHits() {
        List<ServerNode> servers = new ArrayList<>();
        for (String ip : ips) {
            servers.add(new DefaultServerNode(ip + ":8080", ip, 8080, null));
        }
        LoadBalancer chloadBalance = new ConsistentHashLoadBalancer(100);
        // Construct 10000 random requests
        List<Invocation> invocations = new ArrayList<>(100);
        for (int i = 0; i < 10000; i++) {
            invocations.add(new Invocation(UUID.randomUUID().toString()));
        }
        // hit statistics
        Map<ServerNode, Integer> map = new HashMap<ServerNode, Integer>();
        for (ServerNode server : servers) {
            map.put(server, 0);
        }
        for (Invocation invocation : invocations) {
            ServerNode selectedServer = chloadBalance.select(servers, invocation);
            map.put(selectedServer, map.get(selectedServer)+1);
        }
        for (Map.Entry<ServerNode, Integer> serverNodeIntegerEntry : map.entrySet()) {
            System.out.println("serverNode: " + serverNodeIntegerEntry.getKey().getUrl() + " hits condition: "
                    + serverNodeIntegerEntry.getValue() / 10000.0);
        }
    }

    @Test
    public void testDistribution() {
        List<ServerNode> servers = new ArrayList<>();
        for (String ip : ips) {
            servers.add(new DefaultServerNode(ip + ":8080", ip, 8080, null));
        }
        LoadBalancer chloadBalance = new ConsistentHashLoadBalancer();
        List<Invocation> invocations = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            invocations.add(new Invocation(UUID.randomUUID().toString()));
        }
        // statistical distribution
        Map<ServerNode, Long> map = new HashMap<>();
        for (ServerNode server : servers) {
            map.put(server, 0L);
        }
        for (Invocation invocation : invocations) {
            ServerNode selectedServer = chloadBalance.select(servers, invocation);
            map.put(selectedServer, map.get(selectedServer)+1);
        }
        System.out.println("方差" + StatisticsUtil.variance(map.values().toArray(new Long[]{})));
        System.out.println("标准差" + StatisticsUtil.standardDeviation(map.values().toArray(new Long[]{})));
    }

    @Test
    public void testNodeAddAndRemove() {
        List<ServerNode> servers = new ArrayList<>();
        for (String ip : ips) {
            servers.add(new DefaultServerNode(ip, null, 0, null));
        }
        List<ServerNode> serverChanged = servers.subList(0, 80);
        LoadBalancer chloadBalance = new ConsistentHashLoadBalancer();
        List<Invocation> invocations = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            invocations.add(new Invocation(UUID.randomUUID().toString()));
        }
        int count = 0;
        for (Invocation invocation : invocations) {
            ServerNode origin = chloadBalance.select(servers, invocation);
            ServerNode changed = chloadBalance.select(serverChanged, invocation);
            if (origin.getUrl().equals(changed.getUrl())) count++;
        }
        System.out.println(count / 10000D);
    }


    static String[] ips = {
            "11.10.172.215",
            "11.10.176.96",
            "11.14.65.34",
            "11.14.64.205",
            "11.14.65.67",
            "11.134.247.206",
            "11.10.173.47",
            "11.14.65.117",
            "11.14.69.32",
            "11.10.173.46",
            "11.14.65.170",
            "11.14.65.159",
            "11.14.65.172",
            "11.14.65.171",
            "11.13.137.50",
            "11.13.129.20",
            "11.14.65.60",
            "11.13.167.124",
            "11.13.137.175",
            "11.14.65.17",
            "11.14.65.79",
            "11.14.65.179",
            "11.13.129.114",
            "11.13.129.123",
            "11.14.64.107",
            "11.14.65.177",
            "11.14.64.254",
            "11.14.65.63",
            "11.13.137.48",
            "11.14.64.235",
            "11.14.65.155",
            "11.13.129.121",
            "11.14.65.142",
            "11.14.69.45",
            "11.10.173.57",
            "11.10.173.54",
            "11.10.185.203",
            "11.10.176.102",
            "11.179.205.41",
            "11.179.206.58",
            "11.179.206.227",
            "11.179.205.71",
            "11.10.176.100",
            "11.179.206.42",
            "11.10.176.140",
            "11.10.173.115",
            "11.10.173.82",
            "11.10.185.105",
            "11.10.176.134",
            "11.179.206.27",
            "11.179.206.190",
            "11.15.246.86",
            "11.15.92.53",
            "11.15.214.36",
            "11.15.180.34",
            "11.14.67.4",
            "11.13.111.15",
            "11.8.239.196",
            "11.10.147.202",
            "11.10.174.220",
            "11.17.110.6",
            "11.14.68.78",
            "11.17.110.108",
            "11.17.110.107",
            "11.21.132.41",
            "11.17.98.170",
            "11.13.166.82",
            "11.17.97.234",
            "11.14.69.38",
            "11.27.62.112",
            "11.27.78.248",
            "11.27.146.130",
            "11.27.122.51",
            "11.27.134.108",
            "11.27.127.67",
            "11.27.134.107",
            "11.23.58.112",
            "11.23.90.169",
            "11.24.58.112",
            "11.24.50.24",
            "11.23.120.8",
            "11.26.228.195",
            "11.26.240.203",
            "11.27.19.252",
            "11.23.91.19",
            "11.17.110.52",
            "11.27.61.119",
            "11.27.85.228",
            "11.224.244.121",
            "11.226.220.49",
            "11.27.0.108",
            "11.8.17.104",
            "11.11.68.168",
            "11.14.65.133",
            "11.134.247.244",
            "11.10.192.114",
            "11.10.192.115",
            "11.10.192.116",
            "11.10.192.117",
            "11.10.192.118"
    };
}
