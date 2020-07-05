import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.JTextArea;

//相比cmd实现的聊天室 获取用户输入是在文本区获取 也就是由CreateServerFrame传来的文本区对象
//聊天室服务器启动 开始为客户端提供服务
class Server extends Thread
{
	
	//用于存放所有客户的表 每个客户看成是一个ClientThread类的对象
	public static ArrayList<ClientThread> clientList = new ArrayList<ClientThread>();
	
	//用于存储所有客户用户名的集合 方便验证重复
	public static HashSet<String> clientNames = new HashSet<String>();
	
	//用于存储所有群组 用线程安全的Hashtable实现 群号->群组
	public static Hashtable<Integer, HashSet<String>> groups = new Hashtable<Integer, HashSet<String>>();
	
	//群组个数 相当于群号 群号从1开始生成 每生成一个群群号就+1    可以用随机数生成群号作为改进
	public static int groupnum = 0;
	
	// 文件传输的实现是用户先把文件上传到服务器 然后客户端再从服务器下载文件 有点类似FTP
	// 文件要上传到服务器 再服务器端就需要有一个专门存放用户上传文件的路径 就是FilePath
	public static String serverFilePath = "D:\\server";
	// 生成一个File类的对象 在文件传输时会先用于创建目录
	public static File root = new File(serverFilePath);
	
	//消息区对象
	public static JTextArea messege = null;
	//在线用户文本区对象
	public static JTextArea onlineClients = null;
    //服务器文件文本区
    public static JTextArea onlineFiles = null;
	//服务器的socket 由CreateServerFrame传入
	public static ServerSocket  serverSocket = null;
	
	public Server()
	{
		serverSocket = ServerLogin.serverSocket;
		messege = CreateServerFrame.messege;
		onlineClients = CreateServerFrame.onlineClients;
		onlineFiles = CreateServerFrame.onlineFiles;
		updateFileList();
		messege.append("服务器已成功启动！\n");
	}

	public static void updateFileList()
	{
		onlineFiles.setText("");
		// 使用File类的方法查看上传到服务器的所有文件
		for (String string : Server.root.list())
		{
			onlineFiles.append(string + "\n");
		}
	}
	
	//传递参数 功能实现类似 CS.Server 主要注意修改如何获取用户信息
	public void run() 
	{
		
		while(true)
		{
			//accept()会阻塞直到有客户端连接
			try
			{
				ClientThread client = new ClientThread(serverSocket.accept());

				client.start();
			} 
			catch (IOException e)
			{
				messege.append("接收到登陆请求但登陆失败了.\n");
				e.printStackTrace();
			}
		}
		
	}
}
