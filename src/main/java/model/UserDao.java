package model;

import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 实现三个功能
// 1.插入用户  -- 注册时使用
// 2.按姓名查找用户  -- 登录时使用
// 3.按照用户 id 查找  -- 展示信息时使用

public class UserDao {
    // 查入用户
    public void add(User user) throws OrderSystemException{
        // 1. 先获取和数据库的连接（DataSource）
        Connection connection = DBUtil.getConnection();   // DBUtil 不是标准库中的，是自己封装的，面试时不能提
        // 2.拼装 SQL 语句(PrepareStatament)
        String sql = "insert into user values(null,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1,user.getPassword());
            statement.setInt(2,user.getIsAdmin());
            statement.setString(3,user.getName());
            // 3.执行 SQL 语句(executeQuery，executeUpdate)
            int ret = statement.executeUpdate();  // 表示影响的行数，若插入成功，则影响一行
            if (ret != 1) {
                throw new OrderSystemException("插入用户失败");
            }
            System.out.println("插入用户成功");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入用户失败");
        }finally {
            // 4.关闭连接(close)  （查询语句，还需要遍历结果集合）
            DBUtil.close(connection,statement,null);
        }
    }

    // 按 name 查找用户
    public User selectByName(String name) throws OrderSystemException {
        // 1. 先获取和数据库的连接（DataSource）
        Connection connection = DBUtil.getConnection();
        // 2.拼装 SQL 语句(PrepareStatament)
        String sql = "select * from user where name = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1,name);
            // 3.执行 SQL 语句
            resultSet = statement.executeQuery();
            // 4.遍历结果集(按照名字查找，只能查到一个记录，要求名字不能重复)
            if(resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userId"));
                user.setName(resultSet.getString("name"));
                user.setPassword(resultSet.getString("password"));
                user.setIsAdmin(resultSet.getInt("isAdmin"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("按姓名查找用户失败");
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return null;
    }

    // 按 userId 查找用户
    public User selectById(int userId) throws OrderSystemException {
        // 1. 先获取和数据库的连接（DataSource）
        Connection connection = DBUtil.getConnection();
        // 2.拼装 SQL 语句(PrepareStatament)
        String sql = "select * from user where userId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,userId);
            // 3.执行 SQL 语句
            resultSet = statement.executeQuery();
            // 4.遍历结果集(userId 是主键，只能查到一个记录)
            if(resultSet.next()) {
                User user = new User();
                user.setUserId(resultSet.getInt("userId"));
                user.setName(resultSet.getString("name"));
                user.setPassword(resultSet.getString("password"));
                user.setIsAdmin(resultSet.getInt("isAdmin"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("按d查找用户失败");
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return null;
    }

    public static void main(String[] args) throws OrderSystemException {
        // 进行 UserDao 的单元测试
        UserDao userDao = new UserDao();
        // 1.验证插入数据
        User user = new User();
//        user.setUserId();    // UserId 是自增主键，可以不用设
        user.setName("T");
        user.setPassword("123");
        user.setIsAdmin(0);
        userDao.add(user);

    }

}
