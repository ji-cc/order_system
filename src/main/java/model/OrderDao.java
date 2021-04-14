package model;
// 操作订单
// 1.新增订单
// 2.查看所有订单（管理员，商家）
// 3.查看指定用户订单（普通用户，顾客）
// 4.查看指定订单的详细信息
// 5.修改订单状态（订单是否已经完成）

import util.OrderSystemException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "insert into order_user values(null,?,now(),0)";   // now():获取到现在的时间  0表示订单是未完成状态
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            // 加上 RETURN_GENERATED_KEYS 选项，插入的同时就会把数据库自动生成的自增主键的值获取到
            statement = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1,order.getUserId());
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("插入订单失败");
            }
            // 把自增主键的值读取出来
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) { // 预期生成一个自增主键
                // 理解参数1 ：读取 resultSet 的结果时，可以使用列名，也可以使用下标
                // 由于一个表中的自增列可以有很多个，返回的时候都返回回来了。
                // 下标填成1 ，‘就表示想获取到第一个自增列生成的值
                order.setOrderId(resultSet.getInt(1));
            }
            System.out.println("插入订单第一步成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("插入订单失败");
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
    }
    // order_dish 把菜品信息给插入到表中
    private void addOrderDish(Order order) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "insert into order_dish values(?,?)";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection.setAutoCommit(false); // 关闭自动提交   默认调用 executeXXX 就自动把请求 SQL 发给服务器
            statement = connection.prepareStatement(sql);
            // 由于一个订单对应到多个菜品，就需要遍历 Order 中的菜品数组，把每个记录都取出来
            List<Dish> dishes = order.getDishes();
            for (Dish dish : dishes) {
                // OrderId 是在刚刚进行插入 Order_User 表的时候获取到的自增主键
                statement.setInt(1,order.getOrderId());
                statement.setInt(2,dish.getDishId());
                statement.addBatch(); // 给 sql 新增一个片段，新增一组 values ,就可以把多组数据合并成一个 SQL 语句
            }
            statement.executeBatch(); // 执行 SQL (不是真正的执行)
            connection.commit();   // 真正执行（发送给服务器） 手动关闭 autoCommit 就可以一次给服务器发送多个 SQL 来执行了
        } catch (SQLException e) {
            e.printStackTrace();
            // 如果上面的操作出现异常，就认为整体的新增订单操作失败，回滚之前的插入 order_user 表的内容
            deletaOrderUser(order.getOrderId()); 

        }finally {
            DBUtil.close(connection,statement,resultSet);
        }

    }
    // 这个方法用于删除 order_user 表中的记录
    private void deletaOrderUser(int orderId) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "delete from order_user where orderId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,orderId);
            int ret = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("回滚失败");
            }
            System.out.println("回滚成功");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("回滚失败");
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }

    }

    // 2.查看所有订单信息（管理员，商家）
    // Order 对象里面，有 orderId, userId 这些属性，直接借助order_user表就获取到了
    // 还有一个重要属性 dishes (List<Dish>)
    // 详细信息需要先根据 order_dish 表，获取到所有相关的 dishId, 然后根据 dishId 去dishes 表中查
    // 这里的订单获取，不需要获取那么详细的内容，只获取到订单的一些基本内容就行了
    // 菜品信息有一个专门的查看指定订单的详细信息的接口
    // 当前这个接口返回的 Order 对象中，不包含 dishes 详细数据的
    // 这样做是为了让代码更简单高效
    public List<Order> selectAll() {
        List<Order> orders = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_user";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            // 遍历结果集
            while(resultSet.next()) {
                // 此时 order 对象中，没有 dishes 字段的
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return orders;
    }

    // 3.查看指定用户订单（普通用户，顾客）
    public List<Order> selectByUserId(int userId) {
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_user where userId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<Order> orders = new ArrayList<>();
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,userId);
            resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return orders;
    }

    // 4.查看指定订单的详细信息
    // 这个方法要把Order 对象完整的填写进去
    // 包括 Order 中有哪些菜品，以及菜品的详情
    public Order selectById(int orderId) throws OrderSystemException {
        // 1.先根据 orderId 得到一个 Order 对象
        Order order = buildOrder(orderId);
        // 2.根据 orderId 得到该 orderId 对应放入菜品 id 列表
        List<Integer> dishIds = selectDishIds(orderId);
        // 3.根据菜品 id 列表，查询 dishes 表，获取带菜品详情
        order = getDishDetail(order,dishIds);
        return order;
    }

    // 根据 orderId 查询 order_user 对应的 Order 对象的基本信息
    private Order buildOrder(int orderId) {
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_user where orderId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,orderId);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {  // OrderId是唯一的主键，只能查询到一条结果
                Order order = new Order();
                order.setOrderId(resultSet.getInt("orderId"));
                order.setUserId(resultSet.getInt("userId"));
                order.setTime(resultSet.getTimestamp("time"));
                order.setIsDone(resultSet.getInt("isDone"));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return null;
    }

    // 查找 order_dish 表
    // 2.根据 orderId 得到该 orderId 对应放入菜品 id 列表
    private List<Integer> selectDishIds(int orderId) {
        List<Integer> dishIds = new ArrayList<>();
        Connection connection = DBUtil.getConnection();
        String sql = "select * from order_dish where orderId = ?";
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,orderId);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                dishIds.add(resultSet.getInt("dishId"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            DBUtil.close(connection,statement,resultSet);
        }
        return dishIds;
    }

    // 3.根据菜品 id 列表，查询 dishes 表，获取带菜品详情
    private Order getDishDetail(Order order, List<Integer> dishIds) throws OrderSystemException {
        // 1.准备要返回的结果
        List<Dish> dishes = new ArrayList<>();
        // 2.遍历 dishIds 在 dishes 表中查（前面有现成的方法）
        DishDao dishDao = new DishDao();
        for (Integer dishId : dishIds) {
            Dish dish = dishDao.selectById(dishId);
            dishes.add(dish);
        }
        // 3.把 dishes 设置到 order 对象中
        order.setDishes(dishes);
        return order;
    }

    // 5.修改订单状态（订单是否已经完成）
    public void changeState(int orderId,int isDone) throws OrderSystemException {
        Connection connection = DBUtil.getConnection();
        String sql = "update order_user set isDone = ? where orderId = ?";
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.setInt(1,isDone);
            statement.setInt(2,orderId);
            int ret  = statement.executeUpdate();
            if (ret != 1) {
                throw new OrderSystemException("修改订单状态失败");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new OrderSystemException("修改订单状态失败");
        }finally {
            DBUtil.close(connection,statement,null);
        }
    }
}




















