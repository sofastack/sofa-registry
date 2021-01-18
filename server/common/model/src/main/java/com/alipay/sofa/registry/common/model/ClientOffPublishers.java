package com.alipay.sofa.registry.common.model;

import com.alipay.sofa.registry.common.model.store.Publisher;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class ClientOffPublishers {
    private final ConnectId       connectId;
    private final List<Publisher> publishers;

    public ClientOffPublishers(ConnectId connectId, List<Publisher> publishers) {
        this.connectId = connectId;
        this.publishers = Collections.unmodifiableList(Lists.newArrayList(publishers));
    }

    public ConnectId getConnectId() {
        return connectId;
    }

    public boolean isEmpty() {
        return publishers.isEmpty();
    }

    public List<Publisher> getPublishers() {
        return publishers;
    }
}
