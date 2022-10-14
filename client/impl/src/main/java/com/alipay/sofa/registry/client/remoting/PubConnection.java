package com.alipay.sofa.registry.client.remoting;

import com.alipay.remoting.Connection;
import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.Url;
import io.netty.channel.Channel;

/**
 * @author liqiuliang
 * @create 2022-10-10 2:24
 */
public class PubConnection extends Connection {
    public PubConnection(Channel channel) {
        super(channel);
    }

    public PubConnection(Channel channel, Url url) {
        super(channel, url);
    }

    public PubConnection(Channel channel, ProtocolCode protocolCode, Url url) {
        super(channel, protocolCode, url);
    }

    public PubConnection(Channel channel, ProtocolCode protocolCode, byte version, Url url) {
        super(channel, protocolCode, version, url);
    }
}
