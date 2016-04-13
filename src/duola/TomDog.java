package duola;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * 1，多个浏览器同时访问（多线程）
 * 2.如何提供服务（socket）
 * 3，如何返回响应（IO）
 * 山寨web服务器
 */
public class TomDog {
	
	private static int PORT = 8080;
	
	public static void main(String[] args){
		/*
		 * 通过命令行设置端口
		 */
		int p = (args.length > 0)?Integer.parseInt(args[0]):PORT;
		new TomDog().start(p);
	}
	
	/*
	 * 服务启动方法
	 * 创建socket服务器
	 */
	public void start(int port){
		try{
			ServerSocket ss = new ServerSocket(port);
			System.out.println("----------------监听[" + port + "]端口的服务器启动----------------");
			while(true){
				Socket socket = ss.accept();
				System.out.println("-------------有客户端请求------------");
				//new Thread(new HandlerRquestThread(socket));//线程的方法，效率一般
				/*
				 * 创建向城池，将任务提交给线程池去处理
				 */
				ExecutorService pool = Executors.newFixedThreadPool(100);//线程池，预计100个同时来
				pool.submit(new HandlerRquestThread(socket));//有人来，就从池子里面取出一个线程
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
