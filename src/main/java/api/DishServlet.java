package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Dish;
import model.DishDao;
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
import java.util.List;

@WebServlet("/dish")
public class DishServlet extends HttpServlet {
    // 先创建一个 gson 的实例
    private Gson gson = new GsonBuilder().create();

    // 读取的 JSON 请求对象
    static class Request {  // 请求类
        public String name;
        public int price;
    }
    // 构造的 JSON 响应对象
    static class Response {  // 响应类
        public int ok;
        public String reason;
    }
    // 管理员新增菜品
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
            if (user.getIsAdmin() == 0) {
                // 不是管理员
                throw new OrderSystemException("您不是管理员");
            }
            // 3.读取请求 body
            String body = OrderSystemUtil.readBody(req);
            // 4.把 body 转成 Request 对象
            Request request = gson.fromJson(body,Request.class);
            // 5.构造 Dish 对象，插入到数据库中
            Dish dish = new Dish();
            dish.setName(request.name);
            dish.setPrice(request.price);
            DishDao dishDao = new DishDao();
            dishDao.add(dish);
            // 6.返回结果给客户端
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
    // 管理员删除菜品
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            // 1.检查用户是否登录
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("当前未登录");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("当前未登录");
            }
            // 2.检测用户是否是管理员
            if (user.getIsAdmin() == 0) {
                // 不是管理员
                throw new OrderSystemException("您不是管理员");
            }
            // 3.读取到 dishId  // dishId 在 url 中   xxxxx/order_system/dish/dishId=1
            String dishIdStr = req.getParameter("dishId");
            if (dishIdStr == null) {
                throw new OrderSystemException("dishId参数不正确");
            }
            // dishIdStr 转成数
            int dishId = Integer.parseInt(dishIdStr);
            // 4.删除数据库中的对应记录
            DishDao dishDao = new DishDao();
            dishDao.delete(dishId);
            // 5.返回一个响应结果
            response.ok = 1;
            response.reason = "";
    } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        }finally {
            String jsonString = gson.toJson(response);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write(jsonString);
        }
    }

    // 查看所有菜品
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        resp.setContentType("application/json; charset=utf-8");
        Response response = new Response();
        try {
            // 1.检查用户是否登录
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("当前未登录");
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                throw new OrderSystemException("当前未登录");
            }
            // 2.从数据库中读取数据
            DishDao dishDao = new DishDao();
            List<Dish> dishes = dishDao.selectAll();
            // 3.把结果返回到页面
            String jsonString = gson.toJson(dishes);
            resp.getWriter().write(jsonString);
    } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }
}

























