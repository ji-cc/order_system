package api;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.User;
import model.UserDao;
import util.OrderSystemException;
import util.OrderSystemUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    // 先创建一个 gson 的实例
    private Gson gson = new GsonBuilder().create();

    // 读取的 JSON 请求对象
    static class Request {  // 请求类
        public String name;
        public String password;
    }
    // 构造的 JSON 响应对象
    static class Response {  // 响应类
        public int ok;
        public String reson;
        public String name;
        public int isAdmin;
    }

    // 登录
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        // ctrl + alt + t
        try {
            // 1.读取 body中的数据
            String body = OrderSystemUtil.readBody(req);
            // 2.把 body 数据解析成 Request 对象 (Gson)
            Request request = gson.fromJson(body, Request.class);
            // 3.按照用户名进行查找，并校验密码
            UserDao userDao = new UserDao();
            User user = userDao.selectByName(request.name);
            if (user == null || !user.getPassword().equals(request.password)) {
                // 用户不存在或者密码不匹配
                throw new OrderSystemException("用户名或密码错误");
            }
            // 5.如果登录成功，就创建 session 对象 【重要】
            HttpSession session = req.getSession(true);  // 创建一个新的 session 对象
            session.setAttribute("user",user);   // user 对象存进去
            response.ok = 1;
            response.reson = "";
            response.name = user.getName();
            response.isAdmin = user.getIsAdmin();
        } catch (OrderSystemException e) {
            // 4.如果登录失败，就返回错误提示
            response.ok = 0;
            response.reson = e.getMessage();  // "用户名或密码错误"
        } finally {
            // 6.构造响应数据
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);

        }
    }
    // 对应到验证登录状态 API
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Response response = new Response();
        // 响应格式一样，所以可以用一个 Response
        try {
            // 1.获取用户当前的 session, 如果 session 不存在，认为未登录状态
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("当前未登录");
            }
            // 2.从 session 中获取 user对象
            User user = (User)session.getAttribute("name");
            if (user == null) {
                throw new OrderSystemException("当前未登录");
            }
            // 3.把 user 中的信息填充进返回值结果中
            response.ok = 1;
            response.reson = "";
            response.name = user.getName();
            response.isAdmin = user.getIsAdmin();
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reson = e.getMessage();
        } finally {
            resp.setContentType("application/json; charset=utf-8");
            String jsonString = gson.toJson(response);
            resp.getWriter().write(jsonString);
        }
    }
}
