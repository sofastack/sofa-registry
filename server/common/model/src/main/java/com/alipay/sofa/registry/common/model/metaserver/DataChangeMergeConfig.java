package com.alipay.sofa.registry.common.model.metaserver;

/**
 * @author jiangcun.hlc@antfin.com
 * @since 2023/4/24
 */
public class DataChangeMergeConfig {

    private int dataChangeMergeDelay;
    private int multiDataChangeMergeDelay;

    public int getDataChangeMergeDelay() {
        return dataChangeMergeDelay;
    }

    public void setDataChangeMergeDelay(int dataChangeMergeDelay) {
        this.dataChangeMergeDelay = dataChangeMergeDelay;
    }

    public int getMultiDataChangeMergeDelay() {
        return multiDataChangeMergeDelay;
    }

    public void setMultiDataChangeMergeDelay(int multiDataChangeMergeDelay) {
        this.multiDataChangeMergeDelay = multiDataChangeMergeDelay;
    }

    @Override
    public String toString() {
        return "DataChangeMergeConfig{" +
                "dataChangeMergeDelay=" + dataChangeMergeDelay +
                ", multiDataChangeMergeDelay=" + multiDataChangeMergeDelay +
                '}';
    }
}
