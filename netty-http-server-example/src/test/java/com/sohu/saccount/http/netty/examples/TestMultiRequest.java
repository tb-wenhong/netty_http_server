package com.sohu.saccount.http.netty.examples;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Test;

import java.io.IOException;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 12/2/13 15:32
 */
public class TestMultiRequest {

    @Test
    //测试混合提交的参数获取
    public void test() throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8085/foobar?a=bbb");
        NameValuePair[] data = {new NameValuePair("hello", "world"), new NameValuePair("foo", "bar")};
        // 将表单的值放入postMethod中
        postMethod.setRequestBody(data);
        // 执行postMethod
        HttpClient client = new HttpClient();
        int status = client.executeMethod(postMethod);
        if (status == HttpStatus.SC_OK) {
            System.out.println(postMethod.getResponseBodyAsString());
        } else {
            System.out.println("fail" + status);
        }
    }

}
