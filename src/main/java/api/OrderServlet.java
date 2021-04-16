package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Dish;
import model.Order;
import model.OrderDao;
import model.User;
import util.OrderSystemException;
import util.OrderSystemUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {
    // 先创建一个 gson 的实例
    private Gson gson = new GsonBuilder().create();

    // 构造的 JSON 响应对象
    static class Response {  // 响应类
        public int ok;
        public String reason;
    }

    // 新增订单  -- 带有 body
    // body 是整型数组，不要专门创建专门的类表示请求的 body 内容了
    // 普通用户才能新增，管理员不能新增
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            // 1.用户的登录状态
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("当前未登录");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("当前未登录");
            }
            // 2.检测用户是否是管理员
            if (user.getIsAdmin() == 1) {
                // 是管理员，禁止新增订单
                throw new OrderSystemException("您是管理员");
            }
            // 3.读取 body 中的数据，进行解析
            String body = OrderSystemUtil.readBody(req);
            // 4.按照 JSON 格式解析 body
            // 要转成一个整型数组：Integer[].class
            Integer[] dishIds = gson.fromJson(body, Integer[].class);

            //           // 如果要转回一个整型的 list
//            //List<Integer> dishIds = gson.fromJson(body,new TypeToken<List<Integer>>() {}.getType());
            // 5.构造订单对象
            // 此处的 orderId,time,isDone,Dish 中的 name,price 都不需要填充
            // 不影响订单的插入
            Order order = new Order();
            order.setUserId(user.getUserId());
            List<Dish> dishes = new ArrayList<>();
            for (Integer dishId : dishIds) {
                Dish dish = new Dish();
                dish.setDishId(dishId);
                dishes.add(dish);
            }
            order.setDishes(dishes);
            // 6.把 order 对象插入到数据库中
            OrderDao orderDao = new OrderDao();
            orderDao.add(order);
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }

    // 查看所有订单
    // 查找指定订单详情
    // 查找指定订单详情API和查看所有订单API都是 GET 方法，都是 order 路径，就需要在 OrderServlet在中根据参数来区分
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        DishServlet.Response response = new DishServlet.Response();
        try {
            // 1.用户的登录状态
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("当前未登录");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("当前未登录");
            }
            // 2.检测用户是管理员还是普通用户
            // 3.读取 orderId 字段，看该字段是否存在，不存在则为查找所有订单API,存在orderId，则实现指定详情 API
            String orderIdStr = req.getParameter("orderId");
            OrderDao orderDao = new OrderDao();
            if (orderIdStr == null) {
                // 4.查找数据库，查找所有订单
                List<Order> orders = null;
                if (user.getIsAdmin() == 0) {
                    // 普通用户，只能查看自己的订单
                    orders = orderDao.selectByUserId(user.getUserId());

                } else {
                    // 管理员,查看所有的订单
                    orders = orderDao.selectAll();
                }
                // 5.构造响应结果
                String jsonString = gson.toJson(orders);
                resp.getWriter().write(jsonString);
            } else{
                // 4.查找数据库，查找指定订单
                int orderId = Integer.parseInt(orderIdStr);
                Order order = orderDao.selectById(orderId);
                // 此处可以改进
                // 如果是普通用户，查找时发现自身的 userId 和订单的UserId 不相符，
                // 5.构造响应结果
                String jsonString = gson.toJson(order);
                resp.getWriter().write(jsonString);
            }
        } catch (OrderSystemException e) {
            // 6.处理异常情况
            response.ok = 0;
            response.reason = e.getMessage();
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }


}


