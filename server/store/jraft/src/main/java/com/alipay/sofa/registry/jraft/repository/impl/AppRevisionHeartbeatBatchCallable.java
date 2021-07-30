package com.alipay.sofa.registry.jraft.repository.impl;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.registry.common.model.store.AppRevision;
import com.alipay.sofa.registry.jraft.command.CommandCodec;
import com.alipay.sofa.registry.jraft.config.DefaultCommonConfig;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.util.BatchCallableRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author : xingpeng
 * @date : 2021-07-28 20:28
 **/
public class AppRevisionHeartbeatBatchCallable extends BatchCallableRunnable<String, String> {
    private static final Logger LOG =
            LoggerFactory.getLogger("METADATA-EXCHANGE", "[AppRevisionHeartbeatBatch]");

    @Autowired
    private DefaultCommonConfig defaultCommonConfig;

    @Autowired
    private RheaKVStore rheaKVStore;

    private static final String APP_REVISION="AppRevision";

    /**dataCenter,AppRevision*/
    private Map<String, AppRevision> appRevisionMap=new ConcurrentHashMap<>();

    public AppRevisionHeartbeatBatchCallable() {
        super(100, TimeUnit.MILLISECONDS, 200);
    }

    @Override
    protected boolean batchProcess(List<TaskEvent> tasks) {
        if(CollectionUtils.isEmpty(tasks)){
            return true;
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("update app_revision gmt_modified, task size: " + tasks.size());
        }
        List<String> revisions =
                tasks.stream().map(task -> task.getData()).collect(Collectors.toList());

        byte[] appRevisionBytes = rheaKVStore.bGet(APP_REVISION);

        Map<String, AppRevision> appRevisionInfoMap = CommandCodec.decodeCommand(appRevisionBytes, appRevisionMap.getClass());

        for(String revision:revisions){

        }
        return false;
    }
}
