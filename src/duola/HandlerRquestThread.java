package duola;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URLDecoder;

/*
 * 专门处理不同的客户端请求的多线程类
 */
public class HandlerRquestThread implements Runnable{
	private InputStream in = null;
	//因为混色机浏览器内容的输出，所以使用了printStream 标准输出流
	private PrintStream out = null;
	public static final String WEB_ROOT = "H:"+File.separator+"AppServ"+File.separator+"www"+File.separator;
	public static final boolean DirShow = true;//是否允许列出目录文件
	
	/*
	 * 通过构造器获得Socket
	 * 并通过Socket获取对客户端的输入和输出流
	 */
	public HandlerRquestThread(Socket socket){
		try {
			in = socket.getInputStream();
			out = new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * 解析请求头，获得客户端请求的资源名称
	 * in 输入流
	 * return 请求的资源名称
	 */
	public String parseRequestHead(InputStream in) throws IOException{
		//客户端发起请求会将一些请求数据包含在请求头中
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		//请求头的第一行将包含请求的方式，资源，协议
		String headContent = br.readLine();
		String[] heads = headContent.split(" ");//空格分割成数组
		System.out.println("解析请求头：");
		System.out.println(headContent);
		if(heads[1].equals("/"))
			heads[1] = heads[1]+"index.html";
		return heads[1];
	}
	
	
	public void getFile(String fileName) throws IOException{
		File file = new File(WEB_ROOT+fileName);
		System.out.println("发送文件：");
		System.out.println(file.toString());
		if(!file.exists()){
			//System.out.println("不存在");
			sendError("404", "您请求的资源["+fileName+"]不存在！");
		}else if(file.isDirectory()){
			File temp = new File(WEB_ROOT+fileName+File.separator+"index.html");
			if(temp.exists()){	//首页存在，显示首页
				getFile(fileName+File.separator+"index.html");
			}else if(DirShow){
//				if(fileName.endsWith("/")){
//				//如果文件不以/结尾，文件超链接不会以file为当前目录，而是结尾有/的，本来想用301重定向，后来用../file/搞定了
//					Dirshow(file);
//				}
				Dirshow(file);
			}else
				sendError("403", "403 Forbidden");
		}else{
			//FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			byte[] buff = new byte[(int)file.length()];
			int len = bis.read(buff);
			
			out.println("HTTP/1.1 200 OK");
			out.println();
			out.write(buff);
		}
		out.flush();
		out.close();
	}
	
	
	/*
	 * 301重定向，请求头这样写就好了
	 * "HTTP/1.1 301 Moved Permanently" ) ;
　　	 *"Location: http://www.bloghuman.com" );
	 */
	
	/*
	 * 列出文件目录
	 * 
	 */
	public void Dirshow(File file){
		//String[] filenames = file.list();
		File[] files = file.listFiles();
		StringBuilder sb = new StringBuilder("<html><head><title>"+file.getName()+"的索引</title>");
		sb.append("<meta http-equiv='Content-Type' content='text/html;charset=gbk'></head>");
		sb.append("<body>");
		sb.append("<center><h1><font color='green'>"+file.getName()+"的索引</font></h1></center>");
		sb.append("<hr color=red>");
		
		sb.append("<img src=/images/folder.png style='width:20px;height:20px;'> ");
		sb.append("<a href=..><font size=4>返回上一级</font></a><br/>");
		
		for (File f : files) {
			if(f.isDirectory()){
				sb.append("<img src=/images/folder.png style='width:20px;height:20px;'> ");
				sb.append("<a href=../"+file.getName()+"/"+f.getName()+"><font size=4>"+f.getName()+"/</font></a><br/>");
			}else{
				sb.append("<img src=/images/file.png style='width:20px;height:20px;'> ");
				sb.append("<a href=../"+file.getName()+"/"+f.getName()+"><font size=4>"+f.getName()+"</font></a><br/>");
			}
		}
		
		sb.append("</body></html>");
		out.println("HTTP/1.1 200 OK");
		out.println();
		out.print(sb.toString());
	}
	
	/*
	 * 输出错误信息
	 * 编号，信息
	 */
	public void sendError(String errorNumber, String errorMsg){
		StringBuilder sb = new StringBuilder("<html><head><title>错误页面</title>");
		
		sb.append("<meta http-equiv='Content-Type' content='text/html;charset=gbk'></head>");
		sb.append("<body>");
		sb.append("<center><h1><font color='green'>"+errorNumber+"</font></h1></center>");
		sb.append("<hr color=red>");
		sb.append("<p>"+errorMsg+"</p>");
		sb.append("<img src=/images/error.jpg>");
		sb.append("</body></html>");
		out.println("HTTP/1.1 404 Not Found");
		out.println();
		out.print(sb.toString());
//		out.flush();
//		out.close();//后面处理函数那里有清空和关闭
	}
	
	@SuppressWarnings("deprecation")
	public void run(){
		System.out.println("------------开始处理用户请求------------");
		try {
			String fileName = parseRequestHead(this.in);
			fileName = URLDecoder.decode(fileName, "utf-8");//url解码，中文目录文件的需要
			getFile(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
