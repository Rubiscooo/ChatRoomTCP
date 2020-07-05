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

//�����ҷ����������߳�    ����ʵ��Ϊ�ͻ����ṩ�ĸ��ַ���
public class ClientThread extends Thread
{
	// ����Ҫ�ǵ� �������ʱ����һ�� �����ٴ���һ��
	public Socket socket = null;
	public DataInputStream din = null;
	public DataOutputStream dout = null;
	public String clientName = null;

	// �������û���һ��ӳ���ϵ �û���->�û�
	public static Hashtable<String, ClientThread> nametouser = new Hashtable<String, ClientThread>();

	// ��Ϣ������
	public static JTextArea messege = null;
	// �����û��ı�������
	public static JTextArea onlineClients = null;
	// ����Ⱥ���ı�������
	public static JTextArea onlineGroups = null;
	   //�������ļ��ı���
    public static JTextArea onlineFiles = null;
    

	public ClientThread(Socket s) throws IOException
	{
		socket = s;
		messege = CreateServerFrame.messege;
		onlineClients = CreateServerFrame.onlineClients;
		onlineGroups = CreateServerFrame.onlineGroups;
		onlineFiles = CreateServerFrame.onlineFiles;
		
		// ��ȡ����ͻ������������
		try
		{
			din = new DataInputStream(socket.getInputStream());
			dout = new DataOutputStream(socket.getOutputStream());
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		// socket�����Ѿ����� ��������Ҫ��֤�ͻ����û�����û���ظ�
		// �ͻ��ĵ�¼��֤ Ҫ�����ֲ������ظ�
		while (true)
		{
			clientName = din.readUTF();
			// �������ȴ�״̬ �ȴ��ͻ�������Name
			if (clientName == null || "".equals(clientName) )
				continue;
			//����set�����ж��û����Ƿ��ظ�
			else if (!Server.clientNames.add(clientName) )
				continue;
			else
			{
				dout.writeUTF("ok");
				break;
			}
		}
		// �����û�
		nametouser.put(clientName, this);
		
		//�ͻ�����ļ���
		Server.clientList.add(this);
		
		messege.append("[" + clientName + "]" + " is online.\n");
		
		//�������˸��������û� 
		updateOnlineClients();
		
		//֪ͨ���������û��б��Ⱥ���б�
		//informUpdateOnlineClients();
		informUpdateOnlineClientsTCP();
		informUpdateGroupsTCP();
		informUpdateFileListTCP();
		
	}
	
	//���������û��б� ���û����ߺ�����ʱ�����
	public static void updateOnlineClients()
	{
		onlineClients.setText("");
		for( String username : Server.clientNames )
		{
			onlineClients.append(username + "\n");
		}
	}
	//����Ⱥ��� ��Ⱥ�鴴�����û�����ʱ���ڷ������˸���
	public static void updateOnlineGroups()
	{
		onlineGroups.setText("");
		
		HashSet<String> members = null;
		
		Iterator<Integer> iterator = Server.groups.keySet().iterator();
		
		while( iterator.hasNext() )
		{
			int groupnum = iterator.next();
			//��ȡ��ǰȺ��
			members = Server.groups.get(groupnum);
			//��������0�������ʾ
			if( members.size() > 0 )
			{
				onlineGroups.append("Group : " + groupnum + "\n");
				//�ٷ�ÿ����ĳ�Ա
				for( String name : members )
				{
					onlineGroups.append(name + "\n");
				}
			}
			else
			{
				//һ�߱���һ���޸������ڵ�ֵ�˻ᱨ�쳣 �õ������Ͳ�����쳣��
				iterator.remove();
			}
		}
		
	}
	//���·������ļ��б� �ڽ��յ��ͻ����ϴ����ļ�ʱ�����
	public static void updateFileList()
	{
		onlineFiles.setText("");
		// ʹ��File��ķ����鿴�ϴ����������������ļ�
		for (String string : Server.root.list())
		{
			onlineFiles.append(string + "\n");
		}
	}
	
	//֪ͨ���пͻ��˸����û��б�
	public static void informUpdateOnlineClientsTCP() throws IOException
	{
		StringBuilder msg = new StringBuilder("100");
		for(String name : Server.clientNames)
		{
			msg.append(" " + name);
		}
		sendMsgAllTCP(msg.toString());
	}
	
	//֪ͨ���пͻ��˸���Ⱥ���б�
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
	

	//֪ͨ���пͻ��˸����ļ��б�
	public static void informUpdateFileListTCP() throws IOException
	{
		StringBuilder msg = new StringBuilder("102");
		for( String filepath : Server.root.list() )
		{
			msg.append(" " + filepath);
		}
		sendMsgAllTCP(msg.toString());
	}
	
	// �����ӵ������������пͻ��˷���Ϣ
	public static void sendMsgAllTCP(String msg) throws IOException
	{
		// ���������û��б� ��ȡ�������ͨ��ÿ���û����������ÿ���ͻ�����Ϣ
		for (ClientThread clients : Server.clientList)
		{
			clients.dout.writeUTF(msg);
		}
	}
	
	// ˽�� ���ض��ͻ��˷���Ϣ -p name msg
	public void sendPriMsgTCP(String msg,String[] msgs) throws IOException
	{
		DataOutputStream dos = nametouser.get(msgs[1]).dout;
		dos.writeUTF("[" + clientName + "]" + " ����˵ : " + gatherMsg(msgs, 2));
	}
	
	// ����������������Ŀͻ��˷�����Ϣ
	public void ReturnMsg(String msg) throws IOException
	{
		dout.writeUTF(msg);
	}

	// ����Ⱥ�鲢����������г�Ա �û���֮����&&���� -cg name1&&name2&&name3
	public void createGroup(String namestring) throws IOException
	{
		//�ж��Լ��ڲ�������
		boolean in = false;
		String[] names = namestring.split("&&");
		HashSet<String> group = new HashSet<String>();

		//����û����Ƿ񶼴��� �Ż�������set����ʵ�� ȡ��������������Ĵ�С�ǲ���һ��
		for (String name : names)
		{ // ��Щ���������ھͷ���
			if (!Server.clientNames.contains(name))
				return;
			else
				group.add(name);
			if( name.equals(clientName) )
				in = true;
		}
		//����Լ�����Ⱥ����Ͳ��ܴ���Ⱥ��
		if( !in )
			return;
		// Ⱥ�鴴���ɹ� �����ĳ�Ա
		Server.groups.put(++Server.groupnum, group);

		updateOnlineGroups();
		informUpdateGroupsTCP();	
	}

	// �û�����ʱ�Զ��˳������������Ⱥ��
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

	// �ͻ����ߵĴ��� ���������������û�����Ϣ �Ӹ����������Ƴ�����û� �ر�����socket
	public void clientOfflineTCP() throws IOException
	{
		// �Ȱ���������û�����Ϣȥ�� �ٸ������û���������Ϣ
		nametouser.remove(clientName);
		Server.clientList.remove(this);
		Server.clientNames.remove(clientName);
		exitAllGroup();
		
		updateOnlineClients();
		updateOnlineGroups();
		
		sendMsgAllTCP(clientName + " offline.");
		messege.append(clientName + " offline.\n");
		
		//���û�����ʱ��֪�����û������û��б��Ⱥ��
		informUpdateOnlineClientsTCP();
		informUpdateGroupsTCP();
		
		//�ر������������socket
		this.dout.close();
		this.din.close();
		this.socket.close();
	}
	
	public void sendGroupMsgTCP(int groupnum, String[] msgs) throws IOException
	{
		// �ж������治����  && ������ǲ������Ⱥ�������
		HashSet<String> members = Server.groups.get(groupnum);
		if (members == null || !members.contains(this.clientName))
			return;

		// ����Ⱥ���е��û� ͨ�����ǵ��������Ⱥ���������˷���Ϣ
		DataOutputStream sdos = null;
		for (String name : members)
		{
			sdos = nametouser.get(name).dout;
			sdos.writeUTF("[ Group : " + groupnum + " ] " + gatherMsg(msgs, 2));
		}
	}

	// �����ַ����������Ϣ  start�ǿ�ʼ���ϵ��±�
	public static String gatherMsg(String[] msgs, int start)
	{
		StringBuffer msg = new StringBuffer();

		for (int i = start; i <= msgs.length - 1; i++)
		{
			msg.append(msgs[i] + " ");
		}
		return msg.toString();
	}
	
	//���������տͻ����ϴ����ļ�
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

	//��ͻ��˷����ļ�
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

	// ÿ���ͻ��൱����һ���߳� ��������ͨ��run()����Ϊ�ͻ��ṩ����
	public void run()
	{
		String msg = null;
		String[] msgs = null;

		while (true)
		{
			try
			{ // �Կͻ��˷�������Ϣ���д��� read()��һ�������������������пͻ�ͨ�����������Ϣ
				if ((msg = din.readUTF()) != null)
				{
					msgs = msg.split("\\s+");
					// �˳���Ϣ --exit
					// �˳�ʱ�ɿͻ��˹ر�socker�����������
					if ("--exit".equals(msg))
					{
						clientOfflineTCP();
						break;
					}
					// �ϴ��ļ����� -sf num filename
					else if ("-sf".equals(msgs[0]))
					{
						receiveFileTCP(Long.parseLong(msgs[1]), gatherMsg(msgs, 2));
					}
					// �����ļ�mingl -df path
					else if ("-df".equals(msgs[0]))
					{
						sendFileTCP(msgs[1]);
					}
					// ˽������ -p name message
					else if ("-p".equals(msgs[0]))
					{
						sendPriMsgTCP(msg, msgs);
					}
					// Ⱥ������ -g Ⱥ�� message ��Ҫ����
					else if ("-g".equals(msgs[0]))
					{
						sendGroupMsgTCP(Integer.parseInt(msgs[1]), msgs);
					}
					// ����Ⱥ������ -cg name1&&name2&&name3
					else if ("-cg".equals(msgs[0]))
					{
						createGroup(msgs[1]);
					}
					//ˢ���ļ��б�
					else if("--cf".equals(msgs[0]))
					{
						informUpdateFileListTCP();
					}
					//��������
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