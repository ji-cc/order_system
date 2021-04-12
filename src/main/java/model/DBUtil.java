package model;

import com.mysql.cj.jdbc.MysqlDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 管理数据库连接
// 1) 建立连接
// 2）断开连接
// JDBC 中使用 DataSource 来管理连接
// DBUtil 相当于是对DataSource 再稍微包装一层
// DataSource 每个应用程序只应该有一个实例（单例）
// DBUtil 本质上就是实现了一个单例模式，管理了唯一的一个DataSource
// 单例模式的实现有两种风格：
// 1.饿汉模式
// 2.懒汉模式
// 此处使用懒汉模式
public class DBUtil {
    private static  volatile DataSource dataSource = null;
    private static  final  String URL = "jdbc:mysql://127.0.0.1:3306/order_system?serverTimezone=UTC&useSSL=true";
    private  static final  String USERNAME = "root";
    private  static  final  String PASSWORD = "1111";

    // 懒汉实现的单例模式线程不安全
    // 如何保证线程安全
    // 1.加锁
    // 2.双重if判定
    // 3.volatile
    public  static  DataSource getDataSource() {
        // ctrl+alt+t：surround功能（选中的语句被if等包围）
        if (dataSource == null) {
            synchronized (DBUtil.class) {
                if (dataSource == null) {
                    dataSource= new MysqlDataSource();
                    // 还需要给 DataSource 设置一些属性
                    ((MysqlDataSource)dataSource).setURL(URL);
                    ((MysqlDataSource)dataSource).setUser(USERNAME);
                    ((MysqlDataSource)dataSource).setPassword(PASSWORD);

                }
            }
        }
        return  dataSource;
    }
    // 通过这个方法来获取连接
    public static Connection getConnection() {
        try {
            return getDataSource().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("数据库连接失败，请检查数据库是否启动正确，url是否正确");
        return null;
    }
    // 通过这个方法来断开连接
    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}