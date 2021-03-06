package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import util.OrderSystemException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
// 注销
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    // 先创建一个 gson 的实例
    private Gson gson = new GsonBuilder().create();

    // 构造的 JSON 响应对象
    static class Response {  // 响应类
        public int ok;
        public String reason;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("utf-8");
        Response response = new Response();
        try {
            // 1.根据 sessionId 找对应的 session 对象
            HttpSession session = req.getSession(false);
            if (session == null) {
                throw new OrderSystemException("您尚未登录");
            }
            // 2.把 session 对象中存的 user 信息删掉即可(直接删掉 session 中的对应的键值对也行)
            // 如果是想删除这个 session 键值对本身，就需要设置一个过期时间，让 session 立刻过期即可
            session.removeAttribute("user");
            response.ok = 1;
            response.reason = "";
        } catch (OrderSystemException e) {
            response.ok = 0;
            response.reason = e.getMessage();
        } finally {
            String jsonString = gson.toJson(response);
            resp.setContentType("application/json; charset=utf-8");
            resp.getWriter().write(jsonString);
        }
    }
}













