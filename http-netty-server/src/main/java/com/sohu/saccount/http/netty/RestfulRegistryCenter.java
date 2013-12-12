package com.sohu.saccount.http.netty;

import com.babyduncan.pattern.Element;
import com.babyduncan.pattern.PathInput;
import com.babyduncan.pattern.PathPattern;
import com.babyduncan.pattern.StringElement;
import com.babyduncan.servlet.ServletAction;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 11/29/13 16:48
 * restful 表达式中心
 */
public class RestfulRegistryCenter {

    private static final Logger logger = Logger.getLogger(RestfulRegistryCenter.class);
    public static final List<Map<String, List<PathPattern<ServletAction>>>> PATTERN_LIST = new ArrayList<Map<String, List<PathPattern<ServletAction>>>>();

    public static void addRestfulMapping(String restfulPattern, String url) {
        Map<String, List<PathPattern<ServletAction>>> map = new HashMap<String, List<PathPattern<ServletAction>>>();
        ServletAction servletAction = new ServletAction(url, ServletAction.Type.REDIRECT);
        PathPattern<ServletAction> pattern = new PathPattern<ServletAction>(servletAction, restfulPattern, true, 3);
        String key = pattern.getKey();
        List<PathPattern<ServletAction>> list = new ArrayList<PathPattern<ServletAction>>();
        logger.info("register : key is " + key + " and pattern is " + pattern.getTarget().target);
        map.put(key, list);
        list.add(pattern);
        PATTERN_LIST.add(map);
    }

    public static String getRealUrl(String restfulURI, Map<String, String> params, String httpMethod, Charset charset) {
        PathInput input = new PathInput(restfulURI, 1);
        String key___ = input.value();
        for (Map<String, List<PathPattern<ServletAction>>> map : PATTERN_LIST) {
            List<PathPattern<ServletAction>> list___ = map.get(key___);
            if (list___ != null) {
                for (PathPattern<ServletAction> pattern___ : list___) {
                    if (pattern___.match(input)) {
                        List<String> paramsKeys = new ArrayList<String>();
                        List<String> paramsValues = new ArrayList<String>();
                        for (String s___ : input.items) {
                            paramsKeys.add(s___);
                        }
                        for (Element e : pattern___.elements) {
                            if (e instanceof StringElement) {
                                paramsValues.add(((StringElement) e).getProperties());
                            }
                        }
                        // 逆序两个集合
                        Collections.reverse(paramsKeys);
                        Collections.reverse(paramsValues);
                        for (int i = 0; i < paramsValues.size(); i++) {
                            try {
                                params.put(paramsValues.get(i), URLDecoder.decode(paramsKeys.get(i), charset.toString()));
                            } catch (UnsupportedEncodingException e) {
                                logger.error("un support charset " + e.getMessage(), e);
                                params.put(paramsValues.get(i), paramsKeys.get(i));
                            }
                        }
                        if (Strings.isNullOrEmpty(httpMethod) || !httpMethod.equalsIgnoreCase("post")) {
                            params.put("httpMethod", "get");
                        } else {
                            params.put("httpMethod", "post");
                        }
                        return pattern___.getTarget().target;
                    }
                }
            }
        }
        return null;
    }
}
