import java.awt.EventQueue;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

//服务器端的界面 用于显示服务器界面 作为登陆界面和启动服务器的媒介 为真正启动服务器做准备 传参数
@SuppressWarnings("serial")
class CreateServerFrame extends JFrame
{
	//地址
	public static InetAddress add = null;
	//服务器界面框架
	public static CreateServerFrame frame = null;
	//面板
	public static JPanel contentPane = null;
	//端口状态
	public static JLabel portStatus = null;
	//消息区
	public static JTextArea messege = null;
	//在线用户文本区
	public static JTextArea onlineClients = null;
	//在线群组文本区
    public static JTextArea onlineGroups = null;
    //服务器文件文本区
    public static JTextArea onlineFiles = null;
	//服务器的socket 由服务器登陆界面传入
	public static ServerSocket  serverSocket = null;
	//用于获取及显示本机主机名和IP

	public static void Create(ServerSocket ss)
	{
		EventQueue.invokeLater(new Runnable()
		{
			
			public void run()
			{
				try
				{
					serverSocket = ss;
					//显示服务器界面
					frame = new CreateServerFrame();
					
					frame.setVisible(true);
					
					//关于界面的刷新 
					//由于服务器会调用accept()方法监听客户端的请求
					//下面的Server.runServer()会阻塞直到有客户端的请求

					//构造Server对象  送变量至Server 包括serverSocket 文本框 在线用户框
					Server server = new Server();
					//真正启动服务器 让服务器开始监听客户端的请求 
					//用多线程实现  让服务器的运行和创建界面在两个线程进行 解决了swing单线程 界面不显示的问题
					server.start();
					
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	//创建并显示服务器界面
	public CreateServerFrame() throws UnknownHostException
	{
		//设置服务器界面的框架
		setTitle("聊天室服务器");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 764, 538);
		
		//面板
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//滚动条
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 81, 492, 238);
		contentPane.add(scrollPane);
		
		//滚动条
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(512, 81, 229, 238);
		contentPane.add(scrollPane_1);
		
		//滚动条
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(512, 354, 226, 135);
		contentPane.add(scrollPane_2);
		
		//在线用户文本框上面的标签
		JLabel lblNewLabel = new JLabel("\u5F53\u524D\u5728\u7EBF\u7528\u6237");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(512, 42, 229, 32);
		contentPane.add(lblNewLabel);
		
		//显示本机主机名和IP信息的标签
		add = InetAddress.getLocalHost(); //放最外面没法处理异常？
		JLabel info = new JLabel("<html><body>本机主机名 : "  + add.getHostName() 
									+ "<br>" + "本机IP : "  + add.getHostAddress()
									+ "<br>" + "服务器端口 : "+ serverSocket.getLocalPort()
									+ "<body></html>");
		info.setHorizontalAlignment(SwingConstants.CENTER);
		info.setBounds(10, 10, 194, 72);
		contentPane.add(info);
		
		//显示服务器状态的标签
		portStatus = new JLabel("portstatus");
		portStatus.setHorizontalAlignment(SwingConstants.CENTER);
		portStatus.setBounds(235, 26, 168, 45);
		portStatus.setText("服务器已启动");
		contentPane.add(portStatus);
		
		// *** 在线用户界面的文本框
		onlineClients = new JTextArea();
		onlineClients.setEditable(false);
		scrollPane_1.setViewportView(onlineClients); //这句话已经把文本框加进去了不用再add
		
		// *** 当前在线群组文本框
		onlineGroups = new JTextArea();
		scrollPane_2.setViewportView(onlineGroups);
		onlineGroups.setEditable(false);
		
		// *** 服务器消息区
		messege = new JTextArea();
		messege.setEditable(false);
		scrollPane.setViewportView(messege);
		
		JLabel lblNewLabel_1 = new JLabel("\u5F53\u524D\u5728\u7EBF\u7FA4\u7EC4");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(515, 319, 226, 25);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("\u4E0A\u4F20\u5230\u670D\u52A1\u5668\u7684\u6587\u4EF6");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(10, 329, 492, 15);
		contentPane.add(lblNewLabel_2);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(10, 354, 492, 135);
		contentPane.add(scrollPane_3);
		
		onlineFiles = new JTextArea();
		onlineFiles.setEditable(false);
		scrollPane_3.setViewportView(onlineFiles);
		
	}
}

