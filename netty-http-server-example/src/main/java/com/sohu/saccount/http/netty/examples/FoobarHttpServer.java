package com.sohu.saccount.http.netty.examples;

import com.sohu.saccount.http.netty.HttpServer;
import com.sohu.saccount.http.netty.RestfulRegistryCenter;
import org.apache.log4j.Logger;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 11/29/13 17:29
 * 一个简单的http server
 */
public class FoobarHttpServer extends HttpServer {

    private static final Logger logger = Logger.getLogger(FoobarHttpServer.class);

    public FoobarHttpServer(int port) {
        super(port);
    }

    //server 的启动方法
    public static void main(String... args) throws Exception {
        //如果需要对某一个handler添加restful url的支持，需要再RestfulRegistryCenter进行注册。
        RestfulRegistryCenter.addRestfulMapping("/foobar/$name", FoobarHandler.URLMAPPING);
        logger.info("foobar server is started on port 8085 ... ");
        //把相应的handler注册到对应的uri上
        new FoobarHttpServer(8085).register(FoobarHandler.URLMAPPING, new FoobarHandler()).start();
    }

}
