package com.sohu.saccount.http.netty;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MixedAttribute;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 11/27/13 10:33
 * 处理分发http请求
 */

public class HttpHandler<T extends NettyHttpHandler> extends ChannelInboundHandlerAdapter {

    /**
     * url 映射关系
     */
    private Map<String, T> urlMapping;

    public HttpHandler(Map<String, T> urlMapping) {
        this.urlMapping = urlMapping;
    }

    private static final Logger logger = Logger.getLogger(HttpHandler.class);

    /**
     * 默认使用UTF-8作为传输编码格式
     */
    private static final Charset DEFAULT_ENCODE = Charset.forName("UTF-8");

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            String result = "";
            // 是否使用自定义编码
            Charset user_defined_input_charset = DEFAULT_ENCODE;
            Charset user_defined_output_charset = DEFAULT_ENCODE;
            HttpRequest req = (HttpRequest) msg;
            System.out.println("request url is " + req.getUri());
            String mappingUri = req.getUri();
            NettyHttpHandler nettyHttpHandler = null;
            Map<String, String> requestParameters = new HashMap<String, String>();
            Map<String, String> requestParameters_rest = new HashMap<String, String>();
            try {
                // 首先获取用户自定义编码，然后再读取参数
                QueryStringDecoder decoderQuery = new QueryStringDecoder(req.getUri());
                if (decoderQuery.parameters() != null && (decoderQuery.parameters().containsKey("_input_encode") || decoderQuery.parameters().containsKey("__input_encode"))) {
                    String ud_input_encode = "";
                    if (decoderQuery.parameters().get("_input_encode") != null && decoderQuery.parameters().get("_input_encode").size() > 0) {
                        ud_input_encode = decoderQuery.parameters().get("_input_encode").get(0);
                    } else if (decoderQuery.parameters().get("__input_encode") != null && decoderQuery.parameters().get("__input_encode").size() > 0) {
                        ud_input_encode = decoderQuery.parameters().get("__input_encode").get(0);
                    }
                    if (Charset.isSupported(ud_input_encode)) {
                        user_defined_input_charset = Charset.forName(ud_input_encode);
                        logger.info("user defined charset is " + user_defined_input_charset);
                    } else {
                        logger.warn("unsupported user define charset " + user_defined_input_charset);
                    }
                }

                if (decoderQuery.parameters() != null && (decoderQuery.parameters().containsKey("_output_encode") || decoderQuery.parameters().containsKey("__output_encode"))) {
                    String ud_output_encode = "";
                    if (decoderQuery.parameters().get("_output_encode") != null && decoderQuery.parameters().get("_output_encode").size() > 0) {
                        ud_output_encode = decoderQuery.parameters().get("_output_encode").get(0);
                    } else if (decoderQuery.parameters().get("__output_encode") != null && decoderQuery.parameters().get("__output_encode").size() > 0) {
                        ud_output_encode = decoderQuery.parameters().get("__output_encode").get(0);
                    }
                    if (Charset.isSupported(ud_output_encode)) {
                        user_defined_output_charset = Charset.forName(ud_output_encode);
                        logger.info("user defined charset is " + user_defined_output_charset);
                    } else {
                        logger.warn("unsupported user define charset " + user_defined_output_charset);
                    }
                }

                if (req.getMethod().equals(HttpMethod.GET)) {
                    // 无参数的get查询模式
                    nettyHttpHandler = urlMapping.get(mappingUri);
                    requestParameters = getRequestParameters(req, user_defined_input_charset);
                    // 有参数的get查询模式
                    if (nettyHttpHandler == null) {
                        if (mappingUri.contains("?")) {
                            mappingUri = mappingUri.substring(0, mappingUri.indexOf("?"));
                            nettyHttpHandler = urlMapping.get(mappingUri);
                            requestParameters = getRequestParameters(req, user_defined_input_charset);
                        } else {
                            //restful url的有参数get查询模式
                            // handle restful url
                            String realURL = RestfulRegistryCenter.getRealUrl(req.getUri(), requestParameters_rest, "get", user_defined_input_charset);
                            if (realURL != null) {
                                nettyHttpHandler = urlMapping.get(realURL);
                            }
                        }
                    }
                } else {
                    //post 混合提交模式
                    if (mappingUri.contains("?")) {
                        mappingUri = mappingUri.substring(0, mappingUri.indexOf("?"));
                    }
                    nettyHttpHandler = urlMapping.get(mappingUri);
                    requestParameters = getRequestParameters(req, user_defined_input_charset);
                    if (nettyHttpHandler == null) {
                        // 如果post提交方式还没有获得handler
                        String realURL = RestfulRegistryCenter.getRealUrl(mappingUri, requestParameters_rest, "post", user_defined_input_charset);
                        if (realURL != null) {
                            nettyHttpHandler = urlMapping.get(realURL);
                        }
                    }
                }
                //获得ip
                String ip = req.headers().get("x-forwarded-for");
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = req.headers().get("Proxy-Client-IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    ip = req.headers().get("WL-Proxy-Client-IP");
                }
                if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                    SocketAddress remoteAddress = ctx.channel().remoteAddress();
                    String ip___ = remoteAddress.toString();  // /127.0.0.1:64855
                    if (ip___.length() > 1 && ip___.contains(":")) {
                        ip = ip___.substring(1, ip___.indexOf(":"));
                    } else {
                        ip = "unknown";
                    }
                }
                if (ip.indexOf(",") > 0) {
                    ip = ip.substring(0, ip.indexOf(","));
                }
                requestParameters.put("remoteIp", ip);
                //加入ip完毕

                if (nettyHttpHandler == null) {
                    result = "no mapping uri handler";
                } else {
                    try {
                        result = nettyHttpHandler.handle(requestParameters, requestParameters_rest);
                    } catch (Exception e) {
                        logger.error("Handler error" + nettyHttpHandler.getClass().toString() + e.getMessage(), e);
                        result = "{\"code\":-999,\"msg\":\"服务器内部错误\"}";
                    }
                }
            } catch (Exception e) {
                logger.error("channelRead error " + e.getMessage(), e);
                result = "{\"code\":-999,\"msg\":\"服务器内部错误\"}";
            }
            boolean keepAlive = isKeepAlive(req);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(result.getBytes(user_defined_output_charset)));
            response.headers().set(CONTENT_TYPE, "text/plain;charset=" + user_defined_output_charset);
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                ctx.write(response);
            }

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private Map<String, String> getRequestParameters(HttpRequest httpRequest, Charset user_define_charset) throws IOException {

        Map<String, String> parameters = new HashMap<String, String>();
        if (httpRequest == null) {
            return parameters;
        }
        if (httpRequest.getMethod().equals(HttpMethod.GET)) {
            QueryStringDecoder decoderQuery = new QueryStringDecoder(httpRequest.getUri(), user_define_charset);
            for (Map.Entry<String, List<String>> entry : decoderQuery.parameters().entrySet()) {
                parameters.put(entry.getKey(), entry.getValue().get(0));
            }
            parameters.put("httpMethod", "get");
            return parameters;
        } else if (httpRequest.getMethod().equals(HttpMethod.POST)) {
            // 有可能是混合提交， 如 post地址为  http://www.foobar.com?a=b  post参数为c=d
            QueryStringDecoder decoderQuery = new QueryStringDecoder(httpRequest.getUri(), user_define_charset);
            for (Map.Entry<String, List<String>> entry : decoderQuery.parameters().entrySet()) {
                parameters.put(entry.getKey(), entry.getValue().get(0));
            }
            HttpPostRequestDecoder httpPostRequestDecoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE, user_define_charset), httpRequest, user_define_charset);
            List<InterfaceHttpData> interfaceHttpDatas = httpPostRequestDecoder.getBodyHttpDatas();
            for (InterfaceHttpData interfaceHttpData : interfaceHttpDatas) {
                if (interfaceHttpData.getHttpDataType().equals(InterfaceHttpData.HttpDataType.Attribute)) {
                    MixedAttribute attribute = (MixedAttribute) interfaceHttpData;
                    parameters.put(interfaceHttpData.getName(), attribute.getValue());
                }
            }
            parameters.put("httpMethod", "post");
            return parameters;
        } else {
            return parameters;
        }

    }

}

