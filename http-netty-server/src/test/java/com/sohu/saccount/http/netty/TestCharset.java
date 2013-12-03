package com.sohu.saccount.http.netty;


import org.junit.Test;

import java.io.*;

/**
 * User: guohaozhao (guohaozhao116008@sohu-inc.com)
 * Date: 12/2/13 14:15
 */
public class TestCharset {

    @Test
    public void test() throws IOException {

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                new FileOutputStream(new File("/Users/babyduncan/foo.txt")));

        bufferedOutputStream.write("你好".getBytes("GBK"));
        bufferedOutputStream.flush();
    }

    @Test
    public void testREAD() throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("/Users/babyduncan/foo.txt")));
        FileInputStream inputStream = new FileInputStream(new File("/Users/babyduncan/foo.txt"));
        byte[] bytes = new byte[1024];

        inputStream.read(bytes);
        System.out.println(new String(bytes, "UTF-8"));

    }

}
