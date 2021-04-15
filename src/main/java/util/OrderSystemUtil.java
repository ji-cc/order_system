package util;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class OrderSystemUtil {
    // 需要实现读取 body 的功能
    // 需要先把整个 body 读取出来，然后才能去解析 json
    public static String readBody(HttpServletRequest request) throws UnsupportedEncodingException {
        // 先去获取 body 的长度(单位是字节)
        int length = request.getContentLength();
        // 创建一个指定长度的字节数组,用这个数组保存读出来的数据
        byte[] buffer = new byte[length];
        // request.getInputStream():得到 request 内置的字节流
        try (InputStream inputStream = request.getInputStream()) {
            inputStream.read(buffer,0,length);  // 第一个参数：缓冲区   0,length：表示从0号下标开始，写 length的长度

        } catch (IOException e) {
            e.printStackTrace();
        }
        // 此处有一个重要的注意事项
        // 构造 String 的时候，必须要指定该字符串的编码方式
        // 这个操作相当于把字节数据转成字符数据
        // 涉及到这样的转换，最好都加上编码方式
        return new String(buffer,"utf-8");
    }
}
