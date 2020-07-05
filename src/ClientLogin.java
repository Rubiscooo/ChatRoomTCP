import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

//������������Ӳ��Ͼ͹��˷���ǽ
//�ͻ��˵�½�����ҵĽ��� ���ɿͻ��˵�½���� �����ɿͻ��˵�socket���ӷ����� �������û���ת��������� ���������д��ݲ���
public class ClientLogin
{
	//�ͻ��˵�½����Ŀ��
	public static JFrame frame = null;
	//�ͻ��˵�½��������
	private static JPanel panel;
	//�ͻ��˵�½������û����ı���
	public static JTextField userNameField = null;
	//�ͻ��˵�½�����ip��ַ�ı���
	public static JTextField ipField = null;
	//�ͻ��˵�½����Ķ˿ں��ı���
	public static JTextField portField = null;
	//�ͻ��˵�½״̬��ʾ��ǩ
	public static JLabel loginStatus = null;
	//���ӷ�������ť
	public static JButton connertServerButton = null;
	//��½��ť
	public static JButton loginButton = null;
	
	//�������� �˿�&��ַ
	public static DataOutputStream dout = null;
	public static DataInputStream din = null;
	public static InetAddress add = null;
	public static Socket socket = null;
	public static String username = null;
	public static byte[] ip = new byte[4];
	public static int port = 0;
	
	//filerootpath �����пͻ��˽����ļ��ĸ�Ŀ¼
	public static String filerootpath = "D:\\client";
	//clientrootpath �ǵ�ǰ�ͻ��˽��յ������ļ��ĸ�Ŀ¼
	public static String clientrootpath = null;
	
	public static void main(String[] args)
	{
		// ���� JFrame ʵ��
		frame = new JFrame("�����ҿͻ��˵�½");
		frame.setBounds(300, 400, 454, 312);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel();
		// �������
		frame.getContentPane().add(panel);
		
		//�����û�����ķ�����������������
		placeComponents(panel);

		// *** �����û������ı���
		userNameField = new JTextField(20);
		userNameField.setEditable(false);
		userNameField.setBounds(168, 168, 165, 25);
		
		panel.add(userNameField);
		// *** ����IP��ַ���ı���
		ipField = new JTextField();
		ipField.setBounds(173, 25, 165, 25);
		panel.add(ipField);
		ipField.setColumns(10);
		// *** ����˿ںŵ��ı���
		portField = new JTextField();
		portField.setBounds(173, 60, 165, 25);
		panel.add(portField);
		portField.setColumns(10);
		
		//�ͻ��˵�½״̬��ʾ��ǩ
		loginStatus = new JLabel("");
		loginStatus.setBounds(83, 133, 250, 25);
		loginStatus.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(loginStatus);

		// ���ý���ɼ�
		frame.setVisible(true);
	}
	//�����������
	private static void placeComponents(JPanel panel)
	{
		panel.setLayout(null);

		// �û�����ǩ
		JLabel userLabel = new JLabel("�û��� : ");
		userLabel.setHorizontalAlignment(SwingConstants.CENTER);
		userLabel.setBounds(83, 168, 80, 25);
		panel.add(userLabel);

		// IP��ַ��ǩ
		JLabel IP = new JLabel("IP��ַ:");
		IP.setBounds(83, 25, 80, 25);
		panel.add(IP);

		//�˿ںű�ǩ
		JLabel label = new JLabel("\u7AEF\u53E3\u53F7:");
		label.setBounds(83, 60, 58, 25);
		panel.add(label);
		
		// �������ӷ�������ť
		connertServerButton = new JButton("\u8FDE\u63A5\u670D\u52A1\u5668");
		connertServerButton.setBounds(83, 98, 250, 25);
		connertServerButton.addActionListener(new ActionListener()
		{	//�ͻ�����Ϣ�������е�½
			public void actionPerformed(ActionEvent e)
			{
				connectServer();
			}
		});
		panel.add(connertServerButton);
		
		loginButton = new JButton("\u767B\u9646");
		loginButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				login();
			}
		});
		loginButton.setBounds(71, 203, 262, 30);
		loginButton.setEnabled(false);
		panel.add(loginButton);
		
	}
	
	//���ӷ����� 
	public static void connectServer()
	{
		//���ж��û�����Ķ˿��Ƿ�Ϸ�
		try
		{
			port = Integer.parseInt(portField.getText());
			if( port <= 0 || port >= 65536 )
			{
				loginStatus.setText("port = " + port + " �˿ڲ��Ϸ���������.");
				return;
			}
			
			String ipString = ipField.getText();
			//�������ʽ ע��.��ת��
			String[] items = ipString.split("\\.");
			
			if( items.length != 4 )
			{
				loginStatus.setText("ip��ַ���Ϸ���������.");
				return;
			}
			//byte�ķ�Χ��-128~127
			else 
			{
				for(int i = 0 ; i <= 3 ; i++ )
				{

					int ipnum = Integer.parseInt(items[i]);
					if( ipnum >= 0 && ipnum <= 127 )
					{
						ip[i] = (byte) ipnum;
					}
					else if( ipnum >=128 && ipnum <= 255 )
					{
						ip[i] = (byte)(ipnum - 256);
					}
					else 
					{
						loginStatus.setText("ip��ַ���Ϸ���������.");
						return;
					}
				}
			}
			//����ip�Ͷ˿ڵĲ���ȷ������ܵ����޷���½ Ҫ�쳣����
			try
			{
				//�����ͻ���socket
				socket = new Socket(InetAddress.getByAddress(ip), port);
				
				//�����ӳɹ� ��ȡ���������
				din = new DataInputStream(socket.getInputStream());
				dout = new DataOutputStream(socket.getOutputStream());
				//һ������ �ɹ������˷�����
				loginStatus.setText("�����ӵ����������������û���.");
				
				//�򿪺͹ر�һЩ��ť���ı���
				ipField.setEditable(false);
				portField.setEditable(false);
				userNameField.setEditable(true);
				connertServerButton.setEnabled(false);
				loginButton.setEnabled(true);
				
			} 
			catch (IOException ioe)
			{
				loginStatus.setText("���ӷ�����ʧ�ܣ�����ip�Ͷ˿��Ƿ���ȷ.");
			}
			
		} 
		catch (NumberFormatException nfe)
		{
			loginStatus.setText("�˿ڻ�ip���Ϸ����������룡");
		}
			
	}
	
	//�����û�������½
	public static void login() 
	{
		try
		{
			username = userNameField.getText();

			dout.writeUTF(username);
			
			//���� ֱ��������������Ӧ��Ϣ
			String response = din.readUTF();

			if ("ok".equals(response))
			{	
				//��ʱ��½�����ɹ� Ӧ��ת���û��������
				loginStatus.setText("login success!");
				loginButton.setEnabled(false);
				frame.setVisible(false);
				//�����ͻ��˽����ļ���Ŀ¼
				new File(ClientLogin.filerootpath).mkdir();
				//Ϊ��ǰ�ͻ��˽��������ļ���Ŀ¼
				clientrootpath = filerootpath + "\\" + username;
				new File(clientrootpath).mkdir();
				//�����ͻ���������� Ϊ�ͻ����ṩ����
				CreateClientFrame.Create();
			}
			else
			{
				//������������ ֱ���û��ٴ����벻�ظ����û���
				loginStatus.setText("�û����ظ��򲻺Ϸ�\n ����������\n");
				//�ͻ���ҲӦ������ ֱ���û��ٴ������û��������µ�½��ť
				userNameField.setText("");
			}
		} 
		catch (IOException ioe)
		{
			loginStatus.setText("�������.");
		}
	
	}
	
}