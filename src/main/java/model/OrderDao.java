package model;
// 操作订单
// 1.新增订单
// 2.查看所有订单（管理员，商家）
// 3.查看指定用户订单（普通用户，顾客）
// 4.查看订单的详细信息
// 5.修改订单状态（订单是否已经完成）

import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderDao {
    // 1.新增订单
    // 订单是和两个表关联的
    // 第一个表示 order_user
    // 第二个表示 order_dish，一个订单中可能涉及点多个菜，就需要给这个表一次性插入多个记录
    public void add(Order order) throws OrderSystemException {
        // 1.先操作 order_user
        addOrderUser(order);
        // 2.再操作 order_dish
        addOrderDish(order);

    }

    // order_user
    private void addOrderUser(Order order) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "insert into order_user values(null,?,now(),0)";   // now():  0表示订单是未完成状态
        PreparedStatement statement = null;
        try {
            // 加上 RETURN_GENERATED_KEYS 选项，插入的同时就会把数据库自动生成的自增主键的值获取到
            statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1,order.getUserId());
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("插入订单失败");
            }
            System.out.println("插入订单第一步成功");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入订单失败");
        }finally {
            DBUtil.close(connection,statement,null);
        }
    }
    // order_dish 把菜品信息给插入到表中
    private void addOrderDish(Order order) {

1:58:44
    }


}
