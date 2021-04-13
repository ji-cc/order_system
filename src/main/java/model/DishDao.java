package model;

import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 操作菜品表
// 1.新增菜品
// 2.删除菜品
// 3.查询所有菜品
// 4.查询指定菜品
// 修改菜品信息（主要修改价格）
public class DishDao {

    // 1.新增菜品
    public void add(Dish dish) throws OrderSystemException {
        // 1. 先获取和数据库的连接（DataSource）
        Connection connection = DBUtil.getConnection();   // DBUtil 不是标准库中的，是自己封装的，面试时不能提
        // 2.拼装 SQL 语句(PrepareStatament)
        String sql = "insert into dishes values(null,?,?)";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setString(1,dish.getName());
            statement.setInt(2,dish.getPrice());
            // 3.执行 SQL 语句(executeQuery，executeUpdate)
            int ret = statement.executeUpdate();  // 表示影响的行数，若插入成功，则影响一行
            if (ret != 1) {
                throw new OrderSystemException("插入菜品失败");
            }
            System.out.println("插入菜品成功");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入菜品失败");
        }finally {
            // 4.关闭连接(close)  （查询语句，还需要遍历结果集合）
            DBUtil.close(connection,statement,null);
        }
    }

    // 2.根据dishId 删除菜品
    public void delete (int dishId) throws OrderSystemException {
        // 1. 先获取和数据库的连接（DataSource）
        Connection connection = DBUtil.getConnection();   // DBUtil 不是标准库中的，是自己封装的，面试时不能提
        // 2.拼装 SQL 语句(PrepareStatament)
        String sql = "delete from dishes where dishId = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,dishId);
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("删除菜品失败");
            }
            System.out.println("删除菜品成功");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("删除菜品失败");
        }finally {
            DBUtil.close(connection,statement,null);
        }
    }

    // 查找所有菜品
    public List<Dish> selectAll() throws OrderSystemException {
        // 1.建立数据库连接
        Connection connection = DBUtil.getConnection();
        // 2.拼装 SQL
        String sql = "select * from dishes";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Dish> dishes = new ArrayList<>();
        try {
            statement = connection.prepareStatement(sql);
            // 3.执行 SQL
            resultSet = statement.executeQuery();
            // 4.遍历结果集,由于此时预期查找到多个记录，需要进行循环
            while (resultSet.next()) {
                Dish dish = new Dish();
                dish.setDishId(resultSet.getInt("dishId"));
                dish.setName(resultSet.getString("name"));
                dish.setPrice(resultSet.getInt("price"));
                dishes.add(dish);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("查找所有菜品失败");
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return dishes;
    }

    // 根据菜品 dishId 查找菜品
    public Dish selectById(int dishId) throws OrderSystemException {
        // 1.建立数据库连接
        Connection connection = DBUtil.getConnection();
        // 2.拼装 SQL
        String sql = "select * from dishes where dishId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,dishId);
            // 3.执行 SQL
            resultSet = statement.executeQuery();
            // 4.遍历结果集
            if (resultSet.next()) {
                Dish dish = new Dish();
                dish.setDishId(resultSet.getInt("dishId"));
                dish.setName(resultSet.getString("name"));
                dish.setPrice(resultSet.getInt("price"));
                return dish;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("按照 id 查找菜品");
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return null;
    }


}
