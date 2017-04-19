package loea.sched;
// /**
import loea.sched.util.Json2Xml;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import loea.sched.simulator.ScalableSimulatorCAEFT;
import org.json.JSONObject;
/* Created by aoshi on 2017/4/16.
 */


import static java.lang.Thread.sleep;

@WebServlet("/scheduler")
public class RunSchedServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        StringBuffer jb = new StringBuffer();
        String line = null;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) { /*report an error*/ }
        System.out.println(String.valueOf(jb));


        response.setCharacterEncoding("utf-8");
        /*try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        PrintWriter out = response.getWriter();
        String data = Json2Xml.json2xml(String.valueOf(jb));//"{\"result\":\"success\"}";
        System.out.println(data);
        //将数据拼接成JSON格式
        HashMap<String,String> simuResult = ScalableSimulatorCAEFT.runScheduler("prov_h4.xml","task_s01_t1_st1000_e10000.xml","vm_v4.xml");
        System.out.println(simuResult);
        out.print(new JSONObject(simuResult));
        out.flush();
        out.close();
    }
}
