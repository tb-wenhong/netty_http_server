package com.sohu.saccount.http.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.Map;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 11/29/13 17:04
 */
public class HttpServer<T extends NettyHttpHandler> {

    private int port;
    private int threads;

    private Map<String, T> urlMapping = new HashMap<String, T>();

    public HttpServer(int port, int threads) {
        this.port = port;
        this.threads = threads;
    }

    public HttpServer(int port) {
        this.port = port;
        this.threads = 4;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HttpServer register(String s, T t) {
        this.urlMapping.put(s, t);
        return this;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(threads);
        EventLoopGroup workerGroup = new NioEventLoopGroup(threads);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.option(ChannelOption.SO_TIMEOUT, 1800);
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1800);
            b.option(ChannelOption.TCP_NODELAY, true);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HttpAggregatorInitializer(urlMapping));

            Channel ch = b.bind(port).sync().channel();
            System.out.println("http server started on port " + port + " ...");
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

//    public static void main(String[] args) throws Exception {
//        if (args.length != 1) {
//            System.err.println(
//                    "Usage: " + HttpServer.class.getSimpleName() + " <port>");
//            System.exit(1);
//        }
//        int port = Integer.parseInt(args[0]);
//        new HttpServer(port).register("/foobar", new SimpleNettyHttpHandler()).start();
//    }
}
