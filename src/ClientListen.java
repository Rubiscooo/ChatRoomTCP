import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextArea;


//�ͻ��˼����߳� �������Է���������Ϣ ���ÿͻ����ڷ�����Ϣ��ͬʱ������Ϣ
public class ClientListen extends Thread
{
	//�����ı���
	public static JTextArea messege = null;
	
	//�����û��б� ��dlm����ʹ��
	public static JList<String> clientList = null;
	public static DefaultListModel<String> dlmclientList = null;
	
	//����Ⱥ���б� ��dlm����ʹ��
	public static JList<String> groupList = null;
	public static DefaultListModel<String> dlmgroupList = null;
	
	//�ļ��б� ��dlm����ʹ��
	public static JList<String> fileList = null;
	public static DefaultListModel<String> dlmfileList = null;
	
	public static Socket listen = null;
	public static DataOutputStream dout = null;
	public static DataInputStream din = null;

	public ClientListen() throws IOException
	{
		messege = CreateClientFrame.messege;
		groupList = CreateClientFrame.groupList;
		
		//�����û��б� ��dlm����ʹ��
		clientList = CreateClientFrame.clientList;
		dlmclientList = CreateClientFrame.dlmclientList;
		
		//����Ⱥ���б� ��dlm����ʹ��
		groupList = CreateClientFrame.groupList;
		dlmgroupList = CreateClientFrame.dlmgroupList;
		
		//�ļ��б� ��dlm����ʹ��
		fileList = CreateClientFrame.fileList;
		dlmfileList = CreateClientFrame.dlmfileList;
		
		listen = ClientLogin.socket;
		din = new DataInputStream(listen.getInputStream());
		dout = new DataOutputStream(listen.getOutputStream());
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
	
	//�ͻ������ط������������ļ�
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
				//readUTF()���������� ������ֱ��������������Ϣ
				//ͨ�����շ����������ĸ��ֲ�ͬ��״̬��ͻ��˽�����Ӧ�Ĳ���
				//���ֱ�ӷ���Щ״̬���ǲ�����������Ϊ�������޶��˸�ʽ
				String msg = din.readUTF();
				String[] msgs = msg.split("\\s+");
				//���������û��б�
				if("100".equals(msgs[0]))
				{
					dlmclientList.removeAllElements();
					for(int i = 1 ; i <= msgs.length - 1 ; i++)
					{
						dlmclientList.addElement(msgs[i]);
					}
				}
				//��������Ⱥ���б�
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
				//�����ļ��б�
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
				//�ͻ�����˫�̵߳� �����̶߳�Ҫ�ر�
				messege.append("Listen thread interrupt.\nClient disconnect!\n");
				break;
			}
		}
	}
}