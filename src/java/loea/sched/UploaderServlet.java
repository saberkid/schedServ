package loea.sched;

import loea.sched.util.Json2Xml;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
//import java.io.BufferedReader;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by aoshi on 2017/4/18.
 */
/*@MultipartConfig(location="tmp", fileSizeThreshold=1024*1024,
        maxFileSize=1024*1024*5, maxRequestSize=1024*1024*5*5)*/
@MultipartConfig
public class UploaderServlet extends HttpServlet{
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
        Collection<Part> parts = request.getParts();
        PrintWriter pw = response.getWriter();
        for(Part part:parts){
            //System.out.println(part);
            String jsonData = FileProcess(part);
            //System.out.println(jsonData);
            String taskXml = Json2Xml.json2xml(jsonData);
            //System.out.println(taskXml);
            pw.print(taskXml);
            pw.flush();
            pw.close();
        }

    }
    private String FileProcess(Part part) throws IOException {
        System.out.println("part.getName(): " + part.getName());

        if(part.getName().equals("taskfile")){
            String cd = part.getHeader("Content-Disposition");
            String[] cds = cd.split(";");
            String filename = cds[2].substring(cds[2].indexOf("=")+1).substring(cds[2].lastIndexOf("//")+1).replace("\"", "");
            String ext = filename.substring(filename.lastIndexOf(".")+1);

            System.out.println("filename:" + filename);
            System.out.println("ext:" + ext);

            InputStream is = part.getInputStream();

            if(Arrays.binarySearch(ImageIO.getReaderFormatNames(),ext) >= 0)
                return null;//imageProcess(filename, ext, is);
            else{
                return commonFileProcess(filename, is);
            }




        }
        return null;
    }

    private String commonFileProcess(String filename, InputStream is) {
        FileOutputStream fos = null;
        StringBuilder strBuilder = new StringBuilder();
        try{
            fos=new FileOutputStream(new File(getClass().getResource("/").getPath()+filename));
            int b = 0;
            while((b = is.read())!=-1){
                char ch = (char) b;
                strBuilder.append(ch);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                fos.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return strBuilder.toString();
    }
}
