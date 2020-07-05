import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextArea;

//聊天室服务器处理线程    具体实现为客户端提供的各种服务
public class ClientThread extends Thread
{
	// 处理要记得 构造对象时处理一次 下线再处理一次
	public Socket socket = null;
	public DataInputStream din = null;
	public DataOutputStream dout = null;
	public String clientName = null;

	// 对所有用户的一个映射关系 用户名->用户
	public static Hashtable<String, ClientThread> nametouser = new Hashtable<String, ClientThread>();

	// 消息区对象
	public static JTextArea messege = null;
	// 在线用户文本区对象
	public static JTextArea onlineClients = null;
	// 在线群组文本区对象
	public static JTextArea onlineGroups = null;
	   //服务器文件文本区
    public static JTextArea onlineFiles = null;
    

	public ClientThread(Socket s) throws IOException
	{
		socket = s;
		messege = CreateServerFrame.messege;
		onlineClients = CreateServerFrame.onlineClients;
		onlineGroups = CreateServerFrame.onlineGroups;
		onlineFiles = CreateServerFrame.onlineFiles;
		
		// 获取这个客户的输入输出流
		try
		{
			din = new DataInputStream(socket.getInputStream());
			dout = new DataOutputStream(socket.getOutputStream());
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		// socket至此已经建立 接下来需要验证客户的用户名有没有重复
		// 客户的登录验证 要求名字不能有重复
		while (true)
		{
			clientName = din.readUTF();
			// 服务器等待状态 等待客户端输入Name
			if (clientName == null || "".equals(clientName) )
				continue;
			//利用set特性判断用户名是否重复
			else if (!Server.clientNames.add(clientName) )
				continue;
			else
			{
				dout.writeUTF("ok");
				break;
			}
		}
		// 加入用户
		nametouser.put(clientName, this);
		
		//客户对象的加入
		Server.clientList.add(this);
		
		messege.append("[" + clientName + "]" + " is online.\n");
		
		//服务器端更新在线用户 
		updateOnlineClients();
		
		//通知更新在线用户列表和群组列表
		//informUpdateOnlineClients();
		informUpdateOnlineClientsTCP();
		informUpdateGroupsTCP();
		informUpdateFileListTCP();
		
	}
	
	//更新在线用户列表 在用户上线和下线时会更新
	public static void updateOnlineClients()
	{
		onlineClients.setText("");
		for( String username : Server.clientNames )
		{
			onlineClients.append(username + "\n");
		}
	}
	//更新群组表 在群组创建和用户下线时会在服务区端更新
	public static void updateOnlineGroups()
	{
		onlineGroups.setText("");
		
		HashSet<String> members = null;
		
		Iterator<Integer> iterator = Server.groups.keySet().iterator();
		
		while( iterator.hasNext() )
		{
			int groupnum = iterator.next();
			//获取当前群组
			members = Server.groups.get(groupnum);
			//人数大于0的组才显示
			if( members.size() > 0 )
			{
				onlineGroups.append("Group : " + groupnum + "\n");
				//再发每个组的成员
				for( String name : members )
				{
					onlineGroups.append(name + "\n");
				}
			}
			else
			{
				//一边遍历一边修改容器内的值了会报异常 用迭代器就不会出异常了
				iterator.remove();
			}
		}
		
	}
	//更新服务器文件列表 在接收到客户端上传的文件时会更新
	public static void updateFileList()
	{
		onlineFiles.setText("");
		// 使用File类的方法查看上传到服务器的所有文件
		for (String string : Server.root.list())
		{
			onlineFiles.append(string + "\n");
		}
	}
	
	//通知所有客户端更新用户列表
	public static void informUpdateOnlineClientsTCP() throws IOException
	{
		StringBuilder msg = new StringBuilder("100");
		for(String name : Server.clientNames)
		{
			msg.append(" " + name);
		}
		sendMsgAllTCP(msg.toString());
	}
	
	//通知所有客户端更新群组列表
	public static void informUpdateGroupsTCP() throws IOException
	{
		StringBuilder msg = new StringBuilder("101");
		Set<Integer> groupnums = Server.groups.keySet();
		
		for(int groupnum : groupnums)
		{
			msg.append(" !" + String.valueOf(groupnum));
			for(String name : Server.groups.get(groupnum))
			{
				msg.append(" " + name);
			}
		}
		
		sendMsgAllTCP(msg.toString());
	}
	

	//通知所有客户端更新文件列表
	public static void informUpdateFileListTCP() throws IOException
	{
		StringBuilder msg = new StringBuilder("102");
		for( String filepath : Server.root.list() )
		{
			msg.append(" " + filepath);
		}
		sendMsgAllTCP(msg.toString());
	}
	
	// 给连接到服务器的所有客户端发消息
	public static void sendMsgAllTCP(String msg) throws IOException
	{
		// 遍历所有用户列表 获取其输出流通过每个用户的输出流给每个客户发消息
		for (ClientThread clients : Server.clientList)
		{
			clients.dout.writeUTF(msg);
		}
	}
	
	// 私聊 给特定客户端发消息 -p name msg
	public void sendPriMsgTCP(String msg,String[] msgs) throws IOException
	{
		DataOutputStream dos = nametouser.get(msgs[1]).dout;
		dos.writeUTF("[" + clientName + "]" + " 对你说 : " + gatherMsg(msgs, 2));
	}
	
	// 给发命令给服务器的客户端返回信息
	public void ReturnMsg(String msg) throws IOException
	{
		dout.writeUTF(msg);
	}

	// 创建群组并输出组内所有成员 用户名之间用&&连接 -cg name1&&name2&&name3
	public void createGroup(String namestring) throws IOException
	{
		//判断自己在不在里面
		boolean in = false;
		String[] names = namestring.split("&&");
		HashSet<String> group = new HashSet<String>();

		//检查用户名是否都存在 优化：可用set方法实现 取交集看看交集后的大小是不是一样
		for (String name : names)
		{ // 有些人名不存在就返回
			if (!Server.clientNames.contains(name))
				return;
			else
				group.add(name);
			if( name.equals(clientName) )
				in = true;
		}
		//如果自己不在群组里就不能创建群组
		if( !in )
			return;
		// 群组创建成功 输出组的成员
		Server.groups.put(++Server.groupnum, group);

		updateOnlineGroups();
		informUpdateGroupsTCP();	
	}

	// 用户下线时自动退出所加入的所有群组
	public void exitAllGroup()
	{
		for (int i = 1; i <= Server.groupnum; i++)
		{
			HashSet<String> members = Server.groups.get(i);
			if (members == null || members.size() == 0)
				continue;
			members.remove(clientName);
		}
	}

	// 客户下线的处理 包括给所有在线用户发信息 从各种容器中移除这个用户 关闭流和socket
	public void clientOfflineTCP() throws IOException
	{
		// 先把这个下线用户的信息去除 再给其他用户发下线信息
		nametouser.remove(clientName);
		Server.clientList.remove(this);
		Server.clientNames.remove(clientName);
		exitAllGroup();
		
		updateOnlineClients();
		updateOnlineGroups();
		
		sendMsgAllTCP(clientName + " offline.");
		messege.append(clientName + " offline.\n");
		
		//有用户下线时告知其他用户更新用户列表和群组
		informUpdateOnlineClientsTCP();
		informUpdateGroupsTCP();
		
		//关闭输入输出流及socket
		this.dout.close();
		this.din.close();
		this.socket.close();
	}
	
	public void sendGroupMsgTCP(int groupnum, String[] msgs) throws IOException
	{
		// 判断这个组存不存在  && 这个人是不是这个群组里的人
		HashSet<String> members = Server.groups.get(groupnum);
		if (members == null || !members.contains(this.clientName))
			return;

		// 遍历群组中的用户 通过他们的输出流给群组中所有人发消息
		DataOutputStream sdos = null;
		for (String name : members)
		{
			sdos = nametouser.get(name).dout;
			sdos.writeUTF("[ Group : " + groupnum + " ] " + gatherMsg(msgs, 2));
		}
	}

	// 整合字符串数组的信息  start是开始整合的下标
	public static String gatherMsg(String[] msgs, int start)
	{
		StringBuffer msg = new StringBuffer();

		for (int i = start; i <= msgs.length - 1; i++)
		{
			msg.append(msgs[i] + " ");
		}
		return msg.toString();
	}
	
	//服务器接收客户端上传的文件
	public void receiveFileTCP(long num,String filename) throws IOException
	{
		messege.append("Server receive file.\n");
		String recPath = Server.serverFilePath + "\\" + filename;
		File file = new File(recPath);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		
		int len = 0;
		byte[] buf = new byte[8192];
		
		for(long i = 1 ; i <= num ; i++)
		{
			len = this.din.read(buf);
			fos.write(buf, 0, len);
			messege.append("receving file..." + Double.toString(100*(double)i/num) + "%\n");
		}
		
		fos.close();
		
		updateFileList();
		informUpdateFileListTCP();
		
		messege.append("Server receive file finished.\n");
	}

	//向客户端发送文件
	public void sendFileTCP(String filename) throws IOException, InterruptedException
	{
		messege.append("Server send file to client.\n");
		String localPath = Server.serverFilePath + "\\" + filename;
		File file = new File(localPath);
		FileInputStream fis = new FileInputStream(file);
		int buflen = 8192;
		long filelen = file.length();
		byte[] buf = new byte[buflen];
		long num = (filelen%8192==0)?filelen/8192:1+filelen/8192;

		this.dout.writeUTF("-sf " + String.valueOf(num) + " " + filename);
		
		int len = 0;
		
		for(long i = 1 ; i <= num ; i++)
		{
			TimeUnit.MICROSECONDS.sleep(100);
			len = fis.read(buf);
			dout.write(buf, 0, len);
		}
		
		fis.close();
		messege.append("Server send file finished.\n");
	}

	// 每个客户相当于是一个线程 服务器就通过run()方法为客户提供服务
	public void run()
	{
		String msg = null;
		String[] msgs = null;

		while (true)
		{
			try
			{ // 对客户端发来的信息进行处理 read()是一个阻塞方法会阻塞到有客户通过输出流发消息
				if ((msg = din.readUTF()) != null)
				{
					msgs = msg.split("\\s+");
					// 退出消息 --exit
					// 退出时由客户端关闭socker及输入输出流
					if ("--exit".equals(msg))
					{
						clientOfflineTCP();
						break;
					}
					// 上传文件命令 -sf num filename
					else if ("-sf".equals(msgs[0]))
					{
						receiveFileTCP(Long.parseLong(msgs[1]), gatherMsg(msgs, 2));
					}
					// 下载文件mingl -df path
					else if ("-df".equals(msgs[0]))
					{
						sendFileTCP(msgs[1]);
					}
					// 私聊命令 -p name message
					else if ("-p".equals(msgs[0]))
					{
						sendPriMsgTCP(msg, msgs);
					}
					// 群聊命令 -g 群号 message 组要存在
					else if ("-g".equals(msgs[0]))
					{
						sendGroupMsgTCP(Integer.parseInt(msgs[1]), msgs);
					}
					// 建立群组命令 -cg name1&&name2&&name3
					else if ("-cg".equals(msgs[0]))
					{
						createGroup(msgs[1]);
					}
					//刷新文件列表
					else if("--cf".equals(msgs[0]))
					{
						informUpdateFileListTCP();
					}
					//公屏聊天
					else
					{
						sendMsgAllTCP(msg);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

}