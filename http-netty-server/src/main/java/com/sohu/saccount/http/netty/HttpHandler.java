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
     * 默认使用GBK作为传输编码格式
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
            // 首先获取用户自定义编码，然后再读取参数
            QueryStringDecoder decoderQuery = new QueryStringDecoder(req.getUri());
            if (decoderQuery.parameters() != null && decoderQuery.parameters().containsKey("_input_encode")) {
                if (Charset.isSupported(decoderQuery.parameters().get("_input_encode").get(0))) {
                    user_defined_input_charset = Charset.forName(decoderQuery.parameters().get("_input_encode").get(0));
                    logger.info("user defined charset is " + user_defined_input_charset);
                } else {
                    logger.warn("unsupported user define charset " + user_defined_input_charset);
                }
            }

            if (decoderQuery.parameters() != null && decoderQuery.parameters().containsKey("_input_encode")) {
                if (Charset.isSupported(decoderQuery.parameters().get("_output_encode").get(0))) {
                    user_defined_output_charset = Charset.forName(decoderQuery.parameters().get("_output_encode").get(0));
                    logger.info("user defined charset is " + user_defined_output_charset);
                } else {
                    logger.warn("unsupported user define charset " + user_defined_output_charset);
                }
            }

            if (req.getMethod().equals(HttpMethod.GET)) {
                nettyHttpHandler = urlMapping.get(mappingUri);
                requestParameters = getRequestParameters(req, user_defined_input_charset);
                if (nettyHttpHandler == null) {
                    if (mappingUri.contains("?")) {
                        mappingUri = mappingUri.substring(0, mappingUri.indexOf("?"));
                        nettyHttpHandler = urlMapping.get(mappingUri);
                        requestParameters = getRequestParameters(req, user_defined_input_charset);
                    } else {
                        // handle restful url
                        String realURL = RestfulRegistryCenter.getRealUrl(req.getUri(), requestParameters);
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
            }


            if (nettyHttpHandler == null) {
                result = "no mapping uri handler";
            } else {
                result = nettyHttpHandler.handle(requestParameters);
            }

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(result.getBytes()));
            response.headers().set(CONTENT_TYPE, "text/plain;charset=" + user_defined_output_charset);
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

            ctx.write(response).addListener(ChannelFutureListener.CLOSE);

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
            return parameters;
        } else {
            return parameters;
        }

    }

}

