
package com.alipay.sofa.registry.jraft.config;

import java.io.File;

/**
 * @author : xingpeng
 * @date : 2021-06-26 16:16
 **/
public final class DefaultConfigs {

    public static String DB_PATH            = "rhea_db" + File.separator;

    public static String RAFT_DATA_PATH     = "raft_data" + File.separator;

    //127.0.0.1:8182,127.0.0.1:8183
    public static String ADDRESS            = "localhost";

    public static String CLUSTER_NAME       = "rhea_example";
}