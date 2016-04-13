package duola;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * 1����������ͬʱ���ʣ����̣߳�
 * 2.����ṩ����socket��
 * 3����η�����Ӧ��IO��
 * ɽկweb������
 */
public class TomDog {
	
	private static int PORT = 8080;
	
	public static void main(String[] args){
		/*
		 * ͨ�����������ö˿�
		 */
		int p = (args.length > 0)?Integer.parseInt(args[0]):PORT;
		new TomDog().start(p);
	}
	
	/*
	 * ������������
	 * ����socket������
	 */
	public void start(int port){
		try{
			ServerSocket ss = new ServerSocket(port);
			System.out.println("----------------����[" + port + "]�˿ڵķ���������----------------");
			while(true){
				Socket socket = ss.accept();
				System.out.println("-------------�пͻ�������------------");
				//new Thread(new HandlerRquestThread(socket));//�̵߳ķ�����Ч��һ��
				/*
				 * ������ǳأ��������ύ���̳߳�ȥ����
				 */
				ExecutorService pool = Executors.newFixedThreadPool(100);//�̳߳أ�Ԥ��100��ͬʱ��
				pool.submit(new HandlerRquestThread(socket));//���������ʹӳ�������ȡ��һ���߳�
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
