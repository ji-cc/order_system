package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// 实现三个功能
// 1.插入用户  -- 注册时使用
// 2.按姓名查找用户  -- 登录时使用
// 3.按照用户 id 查找  -- 展示信息时使用
public class UserDao {
    public void add(User user) {
        // 1. 先获取和数据库的连接（DataSource）
        Connection connection = DBUtil.getConnection();   // DBUtil 不是标准库中的，是自己封装的，面试时不能提
        // 2.拼装 SQL 语句(PrepareStatament)
        String sql = "insert into user values(null,?,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1,user.getName());
            statement.setString(2,user.getPassword());
            statement.setInt(3,user.getIsAdmin());
            // 3.执行 SQL 语句(executeQuery，executeUpdate)
            int ret = statement.executeUpdate();  // 表示影响的行数，若插入成功，则影响一行
            if (ret != 1) {


            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        // 4.关闭连接(close)  （查询语句，还需要遍历结果集合）

    }

}
