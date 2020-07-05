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

//如果局域网连接不上就关了防火墙
//客户端登陆聊天室的界面 生成客户端登陆界面 先生成客户端的socket连接服务器 再输入用户名转入聊天界面 并向界面进行传递参数
public class ClientLogin
{
	//客户端登陆界面的框架
	public static JFrame frame = null;
	//客户端登陆界面的面板
	private static JPanel panel;
	//客户端登陆界面的用户名文本框
	public static JTextField userNameField = null;
	//客户端登陆界面的ip地址文本框
	public static JTextField ipField = null;
	//客户端登陆界面的端口号文本框
	public static JTextField portField = null;
	//客户端登陆状态提示标签
	public static JLabel loginStatus = null;
	//连接服务器按钮
	public static JButton connertServerButton = null;
	//登陆按钮
	public static JButton loginButton = null;
	
	//输出输出流 端口&地址
	public static DataOutputStream dout = null;
	public static DataInputStream din = null;
	public static InetAddress add = null;
	public static Socket socket = null;
	public static String username = null;
	public static byte[] ip = new byte[4];
	public static int port = 0;
	
	//filerootpath 是所有客户端接收文件的根目录
	public static String filerootpath = "D:\\client";
	//clientrootpath 是当前客户端接收的所有文件的根目录
	public static String clientrootpath = null;
	
	public static void main(String[] args)
	{
		// 创建 JFrame 实例
		frame = new JFrame("聊天室客户端登陆");
		frame.setBounds(300, 400, 454, 312);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		panel = new JPanel();
		// 添加面板
		frame.getContentPane().add(panel);
		
		//调用用户定义的方法并添加组件到面板
		placeComponents(panel);

		// *** 输入用户名的文本框
		userNameField = new JTextField(20);
		userNameField.setEditable(false);
		userNameField.setBounds(168, 168, 165, 25);
		
		panel.add(userNameField);
		// *** 输入IP地址的文本框
		ipField = new JTextField();
		ipField.setBounds(173, 25, 165, 25);
		panel.add(ipField);
		ipField.setColumns(10);
		// *** 输入端口号的文本框
		portField = new JTextField();
		portField.setBounds(173, 60, 165, 25);
		panel.add(portField);
		portField.setColumns(10);
		
		//客户端登陆状态提示标签
		loginStatus = new JLabel("");
		loginStatus.setBounds(83, 133, 250, 25);
		loginStatus.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(loginStatus);

		// 设置界面可见
		frame.setVisible(true);
	}
	//建立各种组件
	private static void placeComponents(JPanel panel)
	{
		panel.setLayout(null);

		// 用户名标签
		JLabel userLabel = new JLabel("用户名 : ");
		userLabel.setHorizontalAlignment(SwingConstants.CENTER);
		userLabel.setBounds(83, 168, 80, 25);
		panel.add(userLabel);

		// IP地址标签
		JLabel IP = new JLabel("IP地址:");
		IP.setBounds(83, 25, 80, 25);
		panel.add(IP);

		//端口号标签
		JLabel label = new JLabel("\u7AEF\u53E3\u53F7:");
		label.setBounds(83, 60, 58, 25);
		panel.add(label);
		
		// 创建连接服务器按钮
		connertServerButton = new JButton("\u8FDE\u63A5\u670D\u52A1\u5668");
		connertServerButton.setBounds(83, 98, 250, 25);
		connertServerButton.addActionListener(new ActionListener()
		{	//客户端信息输入后进行登陆
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
	
	//连接服务器 
	public static void connectServer()
	{
		//先判断用户输入的端口是否合法
		try
		{
			port = Integer.parseInt(portField.getText());
			if( port <= 0 || port >= 65536 )
			{
				loginStatus.setText("port = " + port + " 端口不合法重新输入.");
				return;
			}
			
			String ipString = ipField.getText();
			//正则表达式 注意.的转义
			String[] items = ipString.split("\\.");
			
			if( items.length != 4 )
			{
				loginStatus.setText("ip地址不合法重新输入.");
				return;
			}
			//byte的范围是-128~127
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
						loginStatus.setText("ip地址不合法重新输入.");
						return;
					}
				}
			}
			//由于ip和端口的不正确输入可能导致无法登陆 要异常处理
			try
			{
				//建立客户端socket
				socket = new Socket(InetAddress.getByAddress(ip), port);
				
				//若连接成功 获取输入输出流
				din = new DataInputStream(socket.getInputStream());
				dout = new DataOutputStream(socket.getOutputStream());
				//一切正常 成功连接了服务器
				loginStatus.setText("已连接到服务器，请输入用户名.");
				
				//打开和关闭一些按钮和文本框
				ipField.setEditable(false);
				portField.setEditable(false);
				userNameField.setEditable(true);
				connertServerButton.setEnabled(false);
				loginButton.setEnabled(true);
				
			} 
			catch (IOException ioe)
			{
				loginStatus.setText("连接服务器失败，请检查ip和端口是否正确.");
			}
			
		} 
		catch (NumberFormatException nfe)
		{
			loginStatus.setText("端口或ip不合法，重新输入！");
		}
			
	}
	
	//输入用户名并登陆
	public static void login() 
	{
		try
		{
			username = userNameField.getText();

			dout.writeUTF(username);
			
			//阻塞 直到服务器发回响应信息
			String response = din.readUTF();

			if ("ok".equals(response))
			{	
				//此时登陆真正成功 应该转向用户聊天界面
				loginStatus.setText("login success!");
				loginButton.setEnabled(false);
				frame.setVisible(false);
				//建立客户端接收文件的目录
				new File(ClientLogin.filerootpath).mkdir();
				//为当前客户端建立接收文件的目录
				clientrootpath = filerootpath + "\\" + username;
				new File(clientrootpath).mkdir();
				//开启客户端聊天界面 为客户端提供服务
				CreateClientFrame.Create();
			}
			else
			{
				//服务器会阻塞 直到用户再次输入不重复的用户名
				loginStatus.setText("用户名重复或不合法\n 请重新输入\n");
				//客户端也应该阻塞 直到用户再次输入用户名并按下登陆按钮
				userNameField.setText("");
			}
		} 
		catch (IOException ioe)
		{
			loginStatus.setText("输入错误.");
		}
	
	}
	
}
