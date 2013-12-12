package com.sohu.saccount.http.netty;

import java.util.Map;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 11/29/13 16:46
 * http Handler 接口
 */
public interface NettyHttpHandler {

    /**
     * 处理http请求，参数是httpRequest的参数
     * 如： 请求是 www.foobar.com/a?b=c&d=e
     * 参数是 {b=c,d=e}
     *
     * @param parameters
     * @return
     */
    public String handle(Map<String, String> parameters, Map<String, String> rest_parameters);

}
