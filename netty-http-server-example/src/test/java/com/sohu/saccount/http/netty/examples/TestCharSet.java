package com.sohu.saccount.http.netty.examples;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.junit.Test;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 12/2/13 16:43
 */
public class TestCharSet {

    @Test
    public void testGet() {
        get("http://localhost:8085/foobar?a=hello");
        get("http://localhost:8085/foobar?a=%C4%E3%BA%C3&_input_encode=GBK");
        get("http://localhost:8085/foobar?a=%E4%BD%A0%E5%A5%BD&_input_encode=UTF-8");
    }

    public void get(String url) {
        // Create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(3, false));

        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            // Read the response body.
            byte[] responseBody = method.getResponseBody();

            // Deal with the response.
            // Use caution: ensure correct character encoding and is not binary data
            System.out.println(new String(responseBody));

        } catch (HttpException e) {
            System.err.println("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    }

    @Test
    public void testPost() throws IOException {
//        Post(null, "hello");
        Post("gb2312", URLDecoder.decode("%C4%E3%BA%C3", "gb2312"));
//        System.out.println(URLDecoder.decode("%E4%BD%A0%E5%A5%BD", "UTF-8"));
        Post("UTF-8", URLDecoder.decode("%E4%BD%A0%E5%A5%BD", "UTF-8"));
    }

    private void Post(String encode, String value) throws IOException {
        PostMethod postMethod = new PostMethod("http://localhost:8085/foobar?_input_encode=" + encode + "&_output_encode=gb2312");
        NameValuePair[] data = {new NameValuePair("hello", value)};
        postMethod.addRequestHeader("Content-Type", "text/html;charset=" + encode);
        postMethod.setRequestBody(data);
        HttpClient client = new HttpClient();
        int status = client.executeMethod(postMethod);
        if (status == HttpStatus.SC_OK) {
            System.out.println(postMethod.getResponseBodyAsString());
        } else {
            System.out.println("fail" + status);
        }
    }

}
