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
import java.io.IOException;
// 注册
@WebServlet("/register")  // 使用注解或者 web.xml 都可以把这个类注册到 tomcat 里面
public class RegisterServlet extends HttpServlet {
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
        public String reason;
    }

    // 实际开发中异常处理这样的语法对于处理逻辑中的一些错误情况是非常有帮助的
    // 这样就可以让 try 中包含的都是正常逻辑。 catch 中包含的都是错误处理逻辑
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            // 1.读取 body中的数据
            String body = OrderSystemUtil.readBody(req);
            // 2.把 body 数据解析成 Request 对象 (Gson)
            Request request = gson.fromJson(body,Request.class);
            // 3.查询数据库，看看当前的用户名是否存在（如果已经存在，就提示已经被注册了）
            UserDao userDao = new UserDao();
            User existUser = userDao.selectByName(request.name);
            if (existUser != null) {
                // 当前用户名重复，返回一个表示注册失败的信息
                throw new OrderSystemException("当前用户名已经存在");  // 注册失败直接通过异常来处理
            }
            // 用户名不重复
            // 4.把提交的数据构造成 User 对象，提交给数据库
            User user = new User();
            user.setName(request.name);
            user.setPassword(request.password);
            user.setIsAdmin(0);
            userDao.add(user);
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();  // getMessage():得到的是"当前用户名已经存在"
        }finally {
            // 5.构造响应数据
            String jsonString = gson.toJson(response);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write(jsonString);
        }
    }
}





























