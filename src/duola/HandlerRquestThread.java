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
 * ר�Ŵ���ͬ�Ŀͻ�������Ķ��߳���
 */
public class HandlerRquestThread implements Runnable{
	private InputStream in = null;
	//��Ϊ��ɫ����������ݵ����������ʹ����printStream ��׼�����
	private PrintStream out = null;
	public static final String WEB_ROOT = "H:"+File.separator+"AppServ"+File.separator+"www"+File.separator;
	public static final boolean DirShow = true;//�Ƿ������г�Ŀ¼�ļ�
	
	/*
	 * ͨ�����������Socket
	 * ��ͨ��Socket��ȡ�Կͻ��˵�����������
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
	 * ��������ͷ����ÿͻ����������Դ����
	 * in ������
	 * return �������Դ����
	 */
	public String parseRequestHead(InputStream in) throws IOException{
		//�ͻ��˷�������ὫһЩ�������ݰ���������ͷ��
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		//����ͷ�ĵ�һ�н���������ķ�ʽ����Դ��Э��
		String headContent = br.readLine();
		String[] heads = headContent.split(" ");//�ո�ָ������
		System.out.println("��������ͷ��");
		System.out.println(headContent);
		if(heads[1].equals("/"))
			heads[1] = heads[1]+"index.html";
		return heads[1];
	}
	
	
	public void getFile(String fileName) throws IOException{
		File file = new File(WEB_ROOT+fileName);
		System.out.println("�����ļ���");
		System.out.println(file.toString());
		if(!file.exists()){
			//System.out.println("������");
			sendError("404", "���������Դ["+fileName+"]�����ڣ�");
		}else if(file.isDirectory()){
			File temp = new File(WEB_ROOT+fileName+File.separator+"index.html");
			if(temp.exists()){	//��ҳ���ڣ���ʾ��ҳ
				getFile(fileName+File.separator+"index.html");
			}else if(DirShow){
//				if(fileName.endsWith("/")){
//				//����ļ�����/��β���ļ������Ӳ�����fileΪ��ǰĿ¼�����ǽ�β��/�ģ���������301�ض��򣬺�����../file/�㶨��
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
	 * 301�ض�������ͷ����д�ͺ���
	 * "HTTP/1.1 301 Moved Permanently" ) ;
����	 *"Location: http://www.bloghuman.com" );
	 */
	
	/*
	 * �г��ļ�Ŀ¼
	 * 
	 */
	public void Dirshow(File file){
		//String[] filenames = file.list();
		File[] files = file.listFiles();
		StringBuilder sb = new StringBuilder("<html><head><title>"+file.getName()+"������</title>");
		sb.append("<meta http-equiv='Content-Type' content='text/html;charset=gbk'></head>");
		sb.append("<body>");
		sb.append("<center><h1><font color='green'>"+file.getName()+"������</font></h1></center>");
		sb.append("<hr color=red>");
		
		sb.append("<img src=/images/folder.png style='width:20px;height:20px;'> ");
		sb.append("<a href=..><font size=4>������һ��</font></a><br/>");
		
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
	 * ���������Ϣ
	 * ��ţ���Ϣ
	 */
	public void sendError(String errorNumber, String errorMsg){
		StringBuilder sb = new StringBuilder("<html><head><title>����ҳ��</title>");
		
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
//		out.close();//���洦������������պ͹ر�
	}
	
	@SuppressWarnings("deprecation")
	public void run(){
		System.out.println("------------��ʼ�����û�����------------");
		try {
			String fileName = parseRequestHead(this.in);
			fileName = URLDecoder.decode(fileName, "utf-8");//url���룬����Ŀ¼�ļ�����Ҫ
			getFile(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
