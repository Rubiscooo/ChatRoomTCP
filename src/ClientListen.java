import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextArea;


//客户端监听线程 处理来自服务器的信息 能让客户端在发送消息的同时接收消息
public class ClientListen extends Thread
{
	//各种文本框
	public static JTextArea messege = null;
	
	//在线用户列表 和dlm配套使用
	public static JList<String> clientList = null;
	public static DefaultListModel<String> dlmclientList = null;
	
	//在线群组列表 和dlm配套使用
	public static JList<String> groupList = null;
	public static DefaultListModel<String> dlmgroupList = null;
	
	//文件列表 和dlm配套使用
	public static JList<String> fileList = null;
	public static DefaultListModel<String> dlmfileList = null;
	
	public static Socket listen = null;
	public static DataOutputStream dout = null;
	public static DataInputStream din = null;

	public ClientListen() throws IOException
	{
		messege = CreateClientFrame.messege;
		groupList = CreateClientFrame.groupList;
		
		//在线用户列表 和dlm配套使用
		clientList = CreateClientFrame.clientList;
		dlmclientList = CreateClientFrame.dlmclientList;
		
		//在线群组列表 和dlm配套使用
		groupList = CreateClientFrame.groupList;
		dlmgroupList = CreateClientFrame.dlmgroupList;
		
		//文件列表 和dlm配套使用
		fileList = CreateClientFrame.fileList;
		dlmfileList = CreateClientFrame.dlmfileList;
		
		listen = ClientLogin.socket;
		din = new DataInputStream(listen.getInputStream());
		dout = new DataOutputStream(listen.getOutputStream());
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
	
	//客户端下载服务器发来的文件
	public static void receiveFileTCP(long num,String filename) throws IOException
	{
		messege.append("Client receive file.\n");
		String recPath = ClientLogin.clientrootpath + "\\" + filename;
		File file = new File(recPath);
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file);
		
		int len = 0;
		byte[] buf = new byte[8192];
		
		for(long i = 1 ; i <= num ; i++)
		{
			len = din.read(buf);
			fos.write(buf, 0, len);
			messege.append("receving file..." + Double.toString(100*(double)i/num) + "%\n");
		}
		
		fos.close();
				
		messege.append("Client receive file over.\n");
	}

	public void run()
	{

		while (true)
		{
			try
			{
				//readUTF()是阻塞方法 会阻塞直到服务器发来消息
				//通过接收服务器发来的各种不同的状态码客户端进行相应的操作
				//如果直接发这些状态码是不会出问题的因为服务器限定了格式
				String msg = din.readUTF();
				String[] msgs = msg.split("\\s+");
				//更新在线用户列表
				if("100".equals(msgs[0]))
				{
					dlmclientList.removeAllElements();
					for(int i = 1 ; i <= msgs.length - 1 ; i++)
					{
						dlmclientList.addElement(msgs[i]);
					}
				}
				//更新在线群组列表
				else if("101".equals(msgs[0]))
				{
					dlmgroupList.removeAllElements();
					for(int i = 1 ; i <= msgs.length-1 ; i++)
					{
						if(msgs[i].charAt(0) == '!')
						{
							dlmgroupList.addElement("Group: " + msgs[i].substring(1) + " \n");
						}
						else 
						{
							dlmgroupList.addElement(msgs[i]);
						}
					}
				}
				//更新文件列表
				else if("102".equals(msgs[0]))
				{
					dlmfileList.removeAllElements();
					for(int i = 1 ; i <= msgs.length-1 ; i++)
					{
						dlmfileList.addElement(msgs[i]+"\n");
					}
				}
				else if("-sf".equals(msgs[0]))
				{
					receiveFileTCP(Long.parseLong(msgs[1]), gatherMsg(msgs, 2));
				}
				else
				{
					messege.append(msg + "\n");
				}
			} 
			catch (IOException e)
			{
				//客户端是双线程的 两个线程都要关闭
				messege.append("Listen thread interrupt.\nClient disconnect!\n");
				break;
			}
		}
	}
}