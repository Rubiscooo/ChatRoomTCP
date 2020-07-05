import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.Socket;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

//�ͻ����߳� ����ӿͻ��˷�������Ϣ
class Client extends Thread
{
	//�����ı���
	public static JTextArea messege = null;
	public static JTextArea input = null;
	
	//�����û��б� ��dlm����ʹ��
	public static JList<String> clientList = null;
	public static DefaultListModel<String> dlmclientList = null;
	
	//����Ⱥ���б� ��dlm����ʹ��
	public static JList<String> groupList = null;
	public static DefaultListModel<String> dlmgroupList = null;
	
	//�ļ��б� ��dlm����ʹ��
	public static JList<String> fileList = null;
	public static DefaultListModel<String> dlmfileList = null;
	
	//ѡȡ��Ⱥ��Ա�б� ��dlm����ʹ��
	public static JList<String> memberList = null;
	public static DefaultListModel<String> dlmmemberList = null;
	
	//���ְ�ť
	public static JButton sendmsgButton = null;
	public static JButton clearButton = null;
	public static JButton fileButton = null;
	public static JButton publicButton = null;
	public static JButton createGroupButton = null;
	public static JButton resetmemberButton = null;
	public static JButton fileRefreshButton = null;
	public static JButton openfolderButton = null;
	
	//����ģʽ��ǩ
	public static JLabel chatModel = null;
	
	//���ֳ�Ա����
	public static DataOutputStream dout = null;
	public static DataInputStream din = null;
	public static Socket socket = null;
	public static String username = null;
	
	public static ClientListen ls = null;
	
	public Client(ClientListen clientListen) throws IOException
	{
		messege = CreateClientFrame.messege;
		input = CreateClientFrame.input;
		groupList = CreateClientFrame.groupList;
		
		//�����û��б�������dlm
		clientList = CreateClientFrame.clientList;
		dlmclientList = CreateClientFrame.dlmclientList;
		
		//����Ⱥ���б� ��dlm����ʹ��
		groupList = CreateClientFrame.groupList;
		dlmgroupList = CreateClientFrame.dlmgroupList;
		
		//�ļ��б�������dlm
		fileList = CreateClientFrame.fileList;
		dlmfileList = CreateClientFrame.dlmfileList;
		
		//��Ա�б�������dlm
		memberList = CreateClientFrame.memberList;
		dlmmemberList = CreateClientFrame.dlmmemberList;
		
		sendmsgButton = CreateClientFrame.sendmsgButton;
		clearButton = CreateClientFrame.clearButton;
		fileButton = CreateClientFrame.fileButton;
		publicButton = CreateClientFrame.publicButton;
		createGroupButton = CreateClientFrame.createGroupButton;
		resetmemberButton = CreateClientFrame.resetmemberButton;
		fileRefreshButton = CreateClientFrame.fileRefreshButton;
		openfolderButton = CreateClientFrame.openfolderButton;
		
		chatModel = CreateClientFrame.chatModel;
		
		socket = ClientLogin.socket;
		din = new DataInputStream(socket.getInputStream());
		dout = new DataOutputStream(socket.getOutputStream());
		
		ls = clientListen;
		
		username = CreateClientFrame.username;
		
		
	}
	//Ϊ���ְ�ť����¼�����ɿͻ��˵Ĳ���
	public void run()
	{
		messege.append("Hello " + "[" + username + "]" + " you're online now.\n");
		//ԭ���ⲿ�ַ�������ѭ�� while��true���� ����ѭ����Ч���൱�ڵȴ��û����°�ť
		
		//�����Ͱ�ť����¼�
		sendmsgButton.addActionListener(new ActionListener()
		{	//�ͻ�����Ϣ�������е�½
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					sendMsg(input.getText());
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		
		//������Ⱥ�鰴ť����¼�
		createGroupButton.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent e)
			{
				int size = dlmmemberList.size();
				if( size >= 1 )//ѡȡ������һ���û����ܴ���Ⱥ��
				{
					HashSet<String> set = new HashSet<String>();
					StringBuffer msg = new StringBuffer("-cg ");
					String name = null;
					for(int i = 0 ; i <= size - 1 ; i++ )
					{
						name = dlmmemberList.get(i);
						//��set�ж������Ƿ��ظ� �ظ��˾Ͳ��Ӵ��û�
						if( set.add(name) )
							msg.append(dlmmemberList.get(i) + "&&");
					}
					try
					{
						//����һ������Ⱥ������
						dout.writeUTF(msg.toString());
					} 
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					dlmmemberList.removeAllElements();
				}
				else 
				{
					messege.append("û��ѡ���κ�Ⱥ��Ա.\n");
				}
			}
		});
		
		//��������ť����¼�
		clearButton.addActionListener(new ActionListener()
		{	//�ͻ�����Ϣ�������е�½
			public void actionPerformed(ActionEvent e)
			{
				clear();
			}
		});
		
		//���򿪰�ť����¼�
		openfolderButton.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					java.awt.Desktop.getDesktop().open(new File("D:\\client\\" + username));
				} 
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		});
		
		//��ˢ�°�ť����¼�
		fileRefreshButton.addActionListener(new ActionListener()
		{	//�ͻ�����Ϣ�������е�½
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					dout.writeUTF("--cf");
				} 
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		
		//���������찴ť����¼�
		publicButton.addActionListener(new ActionListener()
		{	//�ͻ�����Ϣ�������е�½
			public void actionPerformed(ActionEvent e)
			{
				chatModel.setText("��������");
			}
		});
		
		//����ѡ��Ա��ť����¼� ���Ⱥ��Ա�б�
		resetmemberButton.addActionListener(new ActionListener()
		{	//�ͻ�����Ϣ�������е�½
			public void actionPerformed(ActionEvent e)
			{
				dlmmemberList.removeAllElements();
			}
		});
		
		//�������ļ���ť����¼�
		fileButton.addActionListener(new ActionListener()
		{	
			public void actionPerformed(ActionEvent e)
			{
				// ��ʾ�򿪵��ļ��Ի���
				JFileChooser jfc = new JFileChooser(new File("D:\\server"));
				JFrame frmIpa = new JFrame();
				jfc.showSaveDialog(frmIpa);
				try
				{
					// ʹ���ļ����ȡѡ����ѡ����ļ�
					File file = jfc.getSelectedFile();
					sendFileTCP(file);
				}
				catch (Exception e2)
				{
					JPanel panel3 = new JPanel();
					JOptionPane.showMessageDialog(panel3, "û��ѡ���κ��ļ�", "��ʾ", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		//���ļ��б����˫���¼� ˫���ļ����Ϳ��������ļ�
		MouseListener mouseListenerFile = new MouseAdapter() 
		{
		     public void mouseClicked(MouseEvent e) 
		     {
		         if (e.getClickCount() == 2)
		         {
		             int index = fileList.locationToIndex(e.getPoint());
		             try
					 {
						dout.writeUTF("-df " + dlmfileList.get(index));
					 } 
		             catch (IOException e1)
					 {
						e1.printStackTrace();
					 }
		          }
		     }
		 };
		 fileList.addMouseListener(mouseListenerFile);
		 
		 //�������û��б����˫���¼� ˫���б��е��û��������л���˽��ģʽ
		MouseListener mouseListenerClient = new MouseAdapter() 
			{
			     public void mouseClicked(MouseEvent e) 
			     {
			         if (e.getClickCount() == 2)
			         {
			        	 //ͨ���޸�����ģʽ��ǩ��������ģʽ
			        	 int index = clientList.locationToIndex(e.getPoint());
			        	 String name = dlmclientList.get(index);
			             chatModel.setText("��[" + name + "]˽��");
			             dlmmemberList.addElement(name);
			         }
			     }
			 };
		clientList.addMouseListener(mouseListenerClient);
		
		//������Ⱥ���б����˫���¼� ˫��Ⱥ���е��û����ͽ���Ⱥ��
		MouseListener mouseListenerGroup = new MouseAdapter() 
		{
		     public void mouseClicked(MouseEvent e) 
		     {
		         if (e.getClickCount() == 2)
		         {
		        	 //ͨ���޸�����ģʽ��ǩ��������ģʽ
		        	 int index = groupList.locationToIndex(e.getPoint());
		        	 String string = null;
		        	 String[] strings = null;
		        	 for(int i = index ; i >= 0 ; i-- )
		        	 {
		        		 string = dlmgroupList.get(i);
		        		 strings = string.split("\\s+");
		        		 if( strings[0].equals("Group:") )
		        		 {
		        			 chatModel.setText("��Ⱥ��[" + Integer.parseInt(strings[1]) + "]Ⱥ��");
		        			 break;
		        		 }
		        	 }
		         }
		     }
		 };
		 groupList.addMouseListener(mouseListenerGroup);
		 
	}
	//������Ϣ��ʵ��
	public void sendMsg(String msg) throws IOException
	{
		String[] msgs = msg.split("\\s+");
		
		String model = chatModel.getText();
		
		//˽��ģʽ��ʵ��
		if( model.endsWith("˽��") && !"".equals(msg)    )
		{
				messege.append("���Լ�˵ : " + msg + "\n");
				int beg = model.indexOf('[');//��ȡ˽�ĵ��û���
				int end = model.indexOf(']');
				String name = model.substring(beg+1, end);
				dout.writeUTF("-p " + name + " " + msg);
		}
		//Ⱥ��ģʽ��ʵ��
		else if( model.endsWith("Ⱥ��") && !"".equals(msg)   )
		{
				int beg = model.indexOf('[');//��ȡȺ��
				int end = model.indexOf(']');
				int groupnum = Integer.parseInt( model.substring(beg+1, end) );
				dout.writeUTF("-g " + groupnum + " " + msg);
		}
		else
		{
			dout.writeUTF("[" + Client.username + "]˵��" + msg);
		}
		input.setText("");
	}
	//����
	public void clear()
	{
		messege.setText("");
	}
	
	//�����ļ� �ļ��������пո�
	public static void sendFileTCP(File file) throws IOException, InterruptedException
	{
		messege.append("Cliet send file to server.\n");

		FileInputStream fis = new FileInputStream(file);

		byte[] buffer = new byte[8192];
		long filelen = file.length();
		long writenum = filelen%8192==0?filelen/8192:1+filelen/8192;
		//�ȷ��ͷ�������
		dout.writeUTF("-sf " + String.valueOf(writenum) + " " + file.getName() );
		
		int len = 0;
		
		for(int i = 1 ; i <= writenum ; i++)
		{
			TimeUnit.MICROSECONDS.sleep(100);
			len = fis.read(buffer);
			dout.write(buffer, 0, len);
		}

		fis.close();
		
		messege.append("Client send file finished.\n");
	}
	
}
