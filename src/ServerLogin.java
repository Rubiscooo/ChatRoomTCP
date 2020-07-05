import java.awt.event.ActionListener;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;

import java.io.File;
import java.io.IOException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
//服务器登陆界面 让本机成为聊天室服务器 完成 ServerSocket对象的建立
//要求输入端口建立 ServerSocket对象 成功后转入服务器界面
public class ServerLogin extends JFrame
{
	//服务器登陆界面的整体框架
	public static ServerLogin frame = null;
	//服务器登陆界面的面板
	private static JPanel contentPane = null;
	//输入端口的文本框 注意在登陆界面不仅要判断输入的端口是否合法也要判断端口是否被占用
	private static JTextField port = null;
	//显示服务器状态的标签
	public static JLabel status = null;
	//用于获取及显示本机主机名和IP
	public static InetAddress add = null;
	//服务器的socket 
	public static ServerSocket  serverSocket = null;
	//文件传输的实现是用户先把文件上传到服务器 然后客户端再从服务器下载文件 有点类似FTP
	//文件要上传到服务器 再服务器端就需要有一个专门存放用户上传文件的路径 就是FilePath
	//注意修改！！！
	public static String serverFilePath = "D:\\server";
	//生成一个File类的对象 在文件传输时会先用于创建目录 
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			//程序入口点 实现服务器界面的创建及显示 输入端口让本机通过该端口提供聊天室服务
			public void run()
			{
				try
				{
					frame = new ServerLogin();

					frame.setVisible(true);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	//建立服务器登陆的各种组件
	public ServerLogin() throws UnknownHostException
	{
		//服务器登陆界面框架设置
		setTitle("聊天室服务器登陆");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(200, 200, 454, 326);
		
		//面板
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//端口号标签
		JLabel lblNewLabel = new JLabel("端口号:");
		lblNewLabel.setFont(new Font("宋体", Font.PLAIN, 16));
		lblNewLabel.setBounds(50, 107, 75, 33);
		contentPane.add(lblNewLabel);
		
		// *** 输入端口的文本框
		port = new JTextField();
		port.setBounds(135, 108, 240, 33);
		contentPane.add(port);
		port.setColumns(10);
		
		// *** 登陆按钮  按下按钮的事件是开启服务器界面 开始之前会判断端口是否被占用
		JButton btnNewButton = new JButton("启动聊天室服务器");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				turnToServerFrame(port.getText());
				port.setText("");
			}
		});
		btnNewButton.setFont(new Font("宋体", Font.PLAIN, 21));
		btnNewButton.setBounds(103, 192, 245, 48);
		contentPane.add(btnNewButton);
		
		add = InetAddress.getLocalHost();
		// *** 信息标签 显示本机主机名和IP
		JLabel info = new JLabel( "<html><body>本机主机名 : " 
									+ add.getHostName() 
									+ "<br>" + "本机IP : " 
									+ add.getHostAddress()
									+ "<body></html>");
		info.setFont(new Font("微软雅黑", Font.PLAIN, 18));
		info.setHorizontalAlignment(SwingConstants.CENTER);
		info.setBounds(42, 10, 353, 87);
		contentPane.add(info);
		
	}
	
	//关闭登陆窗口 开启服务器界面
	public static void turnToServerFrame(String port)
	{
		//设置服务器状态标签 
		setStatusLabel();
		//判断端口是否合法 
		try
		{	//检查输入的端口是否合法
			if( Integer.parseInt(port) >= 1 && Integer.parseInt(port) <= 65535)
			{
				try
				{
					//建立ServerSocket对象 若建立成功说明服务器已建立
					serverSocket = new ServerSocket(Integer.parseInt(port));
					
					//建立服务器接收文件的目录
					new File(serverFilePath).mkdir();
					
					frame.setVisible(false);
					
					//客户端未连接前这句会阻塞 
					CreateServerFrame.Create(serverSocket);
					
				}
				catch (IOException ioe)
				{
					status.setText("端口被占用，重新输入！");
				}

			}
		}
		catch( NumberFormatException nfe )
		{
			status.setText("端口不合法，重新输入 ！");
		}
	}
	//服务器登陆界面的状态标签 提示端口是否合法、是否被占用
	public static void setStatusLabel()
	{
		if( status == null)
		{
			status = new JLabel();
			status.setBounds(50, 150, 332, 32);
			status.setHorizontalAlignment(SwingConstants.CENTER);
			contentPane.add(status);
		}
	}
}

