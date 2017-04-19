package loea.sched.util;/*
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
*/
import org.json.JSONObject;
import org.json.XML;
import java.io.*;
import java.nio.charset.Charset;



public class Json2Xml{

	public static String json2xml(String jsonString){
/*
		XMLSerializer xmlSerializer = new XMLSerializer();
		xmlSerializer.setTypeHintsEnabled(false);
		return xmlSerializer.write(JSONObject.fromObject(jsonString));
*/
		JSONObject jsonObj = new JSONObject(jsonString);
		String xml = XML.toString(jsonObj);
		return  xml;
	}
	
	
	public static String ReadFile(String path){
		try{
			File file=new File(path);
			BufferedReader reader=null;
			String laststr="";
			reader=new BufferedReader(new FileReader(file));
			String tempString=null;
			while((tempString=reader.readLine())!=null){
				tempString = tempString.replace(" ","").replace("\n", "");
				laststr=laststr+tempString;
			}
			reader.close();
			return laststr;

		} catch(IOException e){
			return new String("IOException");
		}

	}
	
//	public static Document xml2doc(String xmlFilePath) throws Exception{
//		File file=new File(xmlFilePath);
//		return (new SAXBuilder()).build(file);
//	}
	
	public static void writeFile(String outPath, String sets) throws IOException{
		FileWriter fw=new FileWriter(outPath);
		fw.write(sets);
		fw.close();
	}
	public static String readFile(String filepath) throws FileNotFoundException, IOException
	{

		StringBuilder sb = new StringBuilder();
		InputStream in = new FileInputStream(filepath);
		Charset encoding = Charset.defaultCharset();

		Reader reader = new InputStreamReader(in, encoding);

		int r = 0;
		while ((r = reader.read()) != -1)//Note! use read() rather than readLine()
		//Can process much larger files with read()
		{
			char ch = (char) r;
			sb.append(ch);
		}

		in.close();
		reader.close();

		return "["+sb.toString()+"]";
	}
	/*public static void main(String[] args){
		try{
			String path="test.json";
			String outPath="test_out.xml";
			FileWriter writer=new FileWriter(outPath);
			String jsonStr=ReadFile(path);
			System.out.printf(jsonStr);
			String res=json2xml(jsonStr);
			//System.out.println(res);
			writeFile(outPath, res);
				
		} catch(Exception e){
			e.printStackTrace();
		}
		
		
	}*/
	
}




