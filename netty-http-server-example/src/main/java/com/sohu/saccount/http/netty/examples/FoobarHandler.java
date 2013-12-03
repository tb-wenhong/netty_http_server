package com.sohu.saccount.http.netty.examples;

import com.sohu.saccount.http.netty.NettyHttpHandler;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 11/29/13 17:14
 */
public class FoobarHandler implements NettyHttpHandler {

    private static final Logger logger = Logger.getLogger(FoobarHandler.class);

    public static final String URLMAPPING = "/foobar";

    @Override
    public String handle(Map<String, String> stringStringMap) {
        System.out.println("param is " + stringStringMap.toString());
        return "FOOBAR" + stringStringMap.toString();
    }
}
