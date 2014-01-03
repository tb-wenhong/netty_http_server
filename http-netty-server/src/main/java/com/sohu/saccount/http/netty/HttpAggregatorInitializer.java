package com.sohu.saccount.http.netty;


import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.Map;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 11/29/13 16:43
 * 为netty 提供http 支持
 */
public class HttpAggregatorInitializer<T extends NettyHttpHandler> extends ChannelInitializer<Channel> {

    private Map<String, T> urlMapping;

    public HttpAggregatorInitializer(Map<String, T> urlMapping) {
        this.urlMapping = urlMapping;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast("codec", new HttpServerCodec());

        p.addLast("aggegator", new HttpObjectAggregator(512 * 1024));
        // Add HttpObjectAggregator to the ChannelPipeline, using a max message size of 512kb.
        // After the message is getting bigger a TooLongFrameException is thrown.
        p.addLast("idleStateHandler", new IdleStateHandler(1, 1, 1, TimeUnit.SECONDS));
        p.addLast("readTimeoutHandler", new ReadTimeoutHandler(1, TimeUnit.SECONDS));
        p.addLast("writeTimeoutHandler", new WriteTimeoutHandler(1, TimeUnit.SECONDS));
        p.addLast("handler", new HttpHandler(urlMapping));
    }

}
