package util;

// 自定义异常
public class OrderSystemException extends Exception{
// 构造方法  alt + insert
    public OrderSystemException(String message) {
        super(message);
    }
}
