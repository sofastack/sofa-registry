package com.alipay.sofa.registry.server.data.providedata;

import com.alipay.sofa.registry.common.model.ServerDataBox;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.metaserver.ProvideData;
import com.alipay.sofa.registry.server.data.bootstrap.DataServerConfig;
import com.alipay.sofa.registry.server.data.change.DataChangeEventCenter;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * @author jiangcun.hlc@antfin.com
 * @since 2023/4/24
 */
public class FetchDataChangeMergerConfigServiceTest {

    FetchDataChangeMergerConfigService fetchDataChangeMergerConfigService;

    @Before
    public void beforeTest() {
        fetchDataChangeMergerConfigService = new FetchDataChangeMergerConfigService();
        fetchDataChangeMergerConfigService
                .setDataChangeEventCenter(mock(DataChangeEventCenter.class))
                .setDataServerConfig(mock(DataServerConfig.class));
    }

    @Test
    public void testDoProcess() {
        Assert.assertTrue(fetchDataChangeMergerConfigService.getSystemPropertyIntervalMillis() == 0);
        Assert.assertTrue(
                fetchDataChangeMergerConfigService.doProcess(
                        fetchDataChangeMergerConfigService.getStorage().get(),
                        new ProvideData(
                                new ServerDataBox(
                                        ""),
                                ValueConstants.DATA_MERGER_TASK_DELAY_CONFIG_DATA_ID,
                                0L)));
        Assert.assertFalse(
                fetchDataChangeMergerConfigService.doProcess(
                        new FetchDataChangeMergerConfigService.SwitchStorage(0L, null),
                        new ProvideData(
                                new ServerDataBox(
                                        "{\"changeDebouncingMillis\":1000,\"changeDebouncingMaxMillis\":3000,\"changeTaskWaitingMillis\":100,\"pushTaskWaitingMillis\":0,\"pushTaskDebouncingMillis\":500,\"regWorkWaitingMillis\":200,\"zoneSet\":[\"ALL_ZONE\"]}"),
                                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                                0L)));
        Assert.assertTrue(
                fetchDataChangeMergerConfigService.doProcess(
                        fetchDataChangeMergerConfigService.getStorage().get(),
                        new ProvideData(
                                new ServerDataBox(
                                        "{\"dataChangeMergeDelay\":1000,\"multiDataChangeMergeDelay\":200}"),
                                ValueConstants.CHANGE_PUSH_TASK_DELAY_CONFIG_DATA_ID,
                                2L)));
    }
}