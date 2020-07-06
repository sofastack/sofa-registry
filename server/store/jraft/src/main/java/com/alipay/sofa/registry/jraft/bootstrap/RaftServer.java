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
package com.alipay.sofa.registry.jraft.bootstrap;

import com.alipay.remoting.rpc.RpcClient;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.core.NodeImpl;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import com.alipay.sofa.jraft.rpc.RaftRpcServerFactory;
import com.alipay.sofa.jraft.rpc.impl.AbstractBoltClientService;
import com.alipay.sofa.jraft.storage.impl.RocksDBLogStorage;
import com.alipay.sofa.jraft.util.StorageOptionsFactory;
import com.alipay.sofa.registry.common.model.store.URL;
import com.alipay.sofa.registry.jraft.command.ConfigurationCommitted;
import com.alipay.sofa.registry.jraft.command.NotifyLeaderChange;
import com.alipay.sofa.registry.jraft.handler.ConfigurationCommittedHandler;
import com.alipay.sofa.registry.jraft.handler.NotifyLeaderChangeHandler;
import com.alipay.sofa.registry.jraft.handler.RaftServerConnectionHandler;
import com.alipay.sofa.registry.jraft.handler.RaftServerHandler;
import com.alipay.sofa.registry.jraft.processor.ConfigurationCommittedListener;
import com.alipay.sofa.registry.jraft.processor.FollowerProcessListener;
import com.alipay.sofa.registry.jraft.processor.LeaderProcessListener;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.metrics.ReporterUtils;
import com.alipay.sofa.registry.net.NetUtil;
import com.alipay.sofa.registry.remoting.Channel;
import com.alipay.sofa.registry.remoting.ChannelHandler;
import com.alipay.sofa.registry.remoting.RemotingException;
import com.alipay.sofa.registry.remoting.bolt.BoltServer;
import com.alipay.sofa.registry.remoting.bolt.SyncUserProcessorAdapter;
import com.alipay.sofa.registry.util.FileUtils;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.IndexType;
import org.rocksdb.RocksDB;
import org.rocksdb.util.SizeUnit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author shangyu.wh
 * @version $Id: RaftServer.java, v 0.1 2018-05-16 11:39 shangyu.wh Exp $
 */
public class RaftServer {

    static {
        RocksDB.loadLibrary();
    }

    private static final Logger            LOGGER         = LoggerFactory
                                                              .getLogger(RaftServer.class);

    private RaftGroupService               raftGroupService;
    private Node                           node;
    private ServiceStateMachine            fsm;
    private PeerId                         serverId;
    private Configuration                  initConf;
    private String                         groupId;
    private String                         dataPath;
    private List<ChannelHandler>           serverHandlers = new ArrayList<>();

    private LeaderProcessListener          leaderProcessListener;

    private FollowerProcessListener        followerProcessListener;

    private ConfigurationCommittedListener configurationCommittedListener;

    private BoltServer                     boltServer;

    /**
     *
     * @param dataPath    Example: /tmp/server1
     * @param groupId
     * @param serverIdStr Example: 127.0.0.1:8081
     * @param initConfStr Example: 127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083
     * @throws IOException
     */
    public RaftServer(String dataPath, String groupId, String serverIdStr, String initConfStr) {
        this.dataPath = dataPath;
        this.groupId = groupId;
        serverId = new PeerId();
        if (!serverId.parse(serverIdStr)) {
            throw new IllegalArgumentException("Fail to parse serverId:" + serverIdStr);
        }

        initConf = new Configuration();
        if (!initConf.parse(initConfStr)) {
            throw new IllegalArgumentException("Fail to parse initConf:" + initConfStr);
        }
    }

    /**
     * start raft server
     * @param raftServerConfig
     * @throws IOException
     */
    public void start(RaftServerConfig raftServerConfig) throws IOException {

        FileUtils.forceMkdir(new File(dataPath));

        serverHandlers.add(new RaftServerHandler(this));
        serverHandlers.add(new RaftServerConnectionHandler());

        boltServer = new BoltServer(new URL(NetUtil.getLocalAddress().getHostAddress(),
            serverId.getPort()), serverHandlers);

        boltServer.initServer();

        RpcServer rpcServer = boltServer.getRpcServer();

        RaftRpcServerFactory.addRaftRequestProcessors(rpcServer);

        this.fsm = ServiceStateMachine.getInstance();
        this.fsm.setLeaderProcessListener(leaderProcessListener);
        this.fsm.setFollowerProcessListener(followerProcessListener);
        this.fsm.setConfigurationCommittedListener(configurationCommittedListener);

        NodeOptions nodeOptions = initNodeOptions(raftServerConfig);

        this.raftGroupService = new RaftGroupService(groupId, serverId, nodeOptions, rpcServer);
        //start
        this.node = this.raftGroupService.start();

        if (raftServerConfig.isEnableMetrics()) {
            ReporterUtils.startSlf4jReporter(raftServerConfig.getEnableMetricsReporterPeriod(),
                node.getNodeMetrics().getMetricRegistry(), raftServerConfig.getMetricsLogger());
        }

        RpcClient raftClient = ((AbstractBoltClientService) (((NodeImpl) node).getRpcService()))
            .getRpcClient();

        NotifyLeaderChangeHandler notifyLeaderChangeHandler = new NotifyLeaderChangeHandler(
            groupId, null);
        raftClient.registerUserProcessor(new SyncUserProcessorAdapter(notifyLeaderChangeHandler));

        ConfigurationCommittedHandler.registerMockHandler(raftClient);
    }

    /**
     * shutdown raftGroupService
     */
    public void shutdown() {
        if (raftGroupService != null) {
            this.raftGroupService.shutdown();
        }
    }

    private NodeOptions initNodeOptions(RaftServerConfig raftServerConfig) {

        NodeOptions nodeOptions = new NodeOptions();

        nodeOptions.setElectionTimeoutMs(raftServerConfig.getElectionTimeoutMs());

        nodeOptions.setDisableCli(false);

        nodeOptions.setSnapshotIntervalSecs(raftServerConfig.getSnapshotIntervalSecs());

        nodeOptions.setInitialConf(initConf);

        nodeOptions.setFsm(this.fsm);

        nodeOptions.setLogUri(dataPath + File.separator + "log");
        nodeOptions.setRaftMetaUri(dataPath + File.separator + "raft_meta");
        nodeOptions.setSnapshotUri(dataPath + File.separator + "snapshot");

        if (raftServerConfig.isEnableMetrics()) {
            nodeOptions.setEnableMetrics(raftServerConfig.isEnableMetrics());
        }

        // See https://github.com/sofastack/sofa-jraft/pull/156
        final BlockBasedTableConfig conf = new BlockBasedTableConfig() //
            // Begin to use partitioned index filters
            // https://github.com/facebook/rocksdb/wiki/Partitioned-Index-Filters#how-to-use-it
            .setIndexType(IndexType.kTwoLevelIndexSearch) //
            .setFilter(new BloomFilter(16, false)) //
            .setPartitionFilters(true) //
            .setMetadataBlockSize(8 * SizeUnit.KB) //
            .setCacheIndexAndFilterBlocks(false) //
            .setCacheIndexAndFilterBlocksWithHighPriority(true) //
            .setPinL0FilterAndIndexBlocksInCache(true) //
            // End of partitioned index filters settings.
            .setBlockSize(4 * SizeUnit.KB)//
            .setBlockCacheSize(raftServerConfig.getRockDBCacheSize() * SizeUnit.MB) //
            .setCacheNumShardBits(8);

        StorageOptionsFactory.registerRocksDBTableFormatConfig(RocksDBLogStorage.class, conf);

        return nodeOptions;
    }

    /**
     * Redirect request to new leader
     * @return
     */
    public String redirect() {
        if (node != null) {
            PeerId leader = node.getLeaderId();
            if (leader != null) {
                return leader.toString();
            }
        }
        return null;
    }

    /**
     * send notify
     * @param leader
     * @param sender
     */
    public void sendLeaderChangeNotify(PeerId leader, String sender) {

        if (boltServer == null) {
            LOGGER.error("Send notify leader change error!server must be started!");
            throw new IllegalStateException(
                "Send notify leader change error!server must be started!");
        }
        NotifyLeaderChange notifyLeaderChange = new NotifyLeaderChange(leader);
        notifyLeaderChange.setSender(sender);
        Collection<Channel> channels = boltServer.getChannels();

        List<Throwable> throwables = new ArrayList<>();
        channels.forEach(channel -> {
            try {
                boltServer.sendSync(channel, notifyLeaderChange, 1000);
            } catch (Exception e) {
                LOGGER.error("Send notify leader change error!url:{}", channel.getRemoteAddress(),
                    e);
                throwables.add(e);
            }
        });

        if (!throwables.isEmpty()) {
            LOGGER.error("Send notify leader change error!");
            throw new RuntimeException("Send notify leader change error!");
        }
    }

    public void sendConfigurationCommittedNotify(Configuration configuration) {
        if (boltServer == null) {
            LOGGER.error("Send notify configuration committed error!server must be started!");
            throw new IllegalStateException(
                "Send configuration committed error!server must be started!");
        }

        ConfigurationCommitted configurationCommitted = new ConfigurationCommitted(
            configuration.toString());
        Collection<Channel> channels = boltServer.getChannels();
        List<Throwable> throwables = new ArrayList<>();
        channels.forEach(channel -> {
            try {
                boltServer.sendSync(channel, configurationCommitted, 1000);
            } catch (Exception e) {
                LOGGER.error("Send notify configuration commited error!url:{}",
                    channel.getRemoteAddress(), e);
                throwables.add(e);
            }
        });
        if (!throwables.isEmpty()) {
            LOGGER.error("Send configuration committed error!");
            throw new RuntimeException("Send configuration committed error!");
        }
    }

    /**
     * Getter method for property <tt>fsm</tt>.
     *
     * @return property value of fsm
     */
    public ServiceStateMachine getFsm() {
        return this.fsm;
    }

    /**
     * Getter method for property <tt>node</tt>.
     *
     * @return property value of node
     */
    public Node getNode() {
        return this.node;
    }

    /**
     * Setter method for property <tt>leaderProcessListener</tt>.
     *
     * @param leaderProcessListener  value to be assigned to property leaderProcessListener
     */
    public void setLeaderProcessListener(LeaderProcessListener leaderProcessListener) {
        this.leaderProcessListener = leaderProcessListener;
    }

    /**
     * Setter method for property <tt>followerProcessListener</tt>.
     *
     * @param followerProcessListener  value to be assigned to property followerProcessListener
     */
    public void setFollowerProcessListener(FollowerProcessListener followerProcessListener) {
        this.followerProcessListener = followerProcessListener;
    }

    /**
     * Getter method for property <tt>serverHandlers</tt>.
     *
     * @return property value of serverHandlers
     */
    public List<ChannelHandler> getServerHandlers() {
        return serverHandlers;
    }

    public void setConfigurationCommittedListener(ConfigurationCommittedListener listener) {
        this.configurationCommittedListener = listener;
    }
}
