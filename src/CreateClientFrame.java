import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.EventQueue;

import java.io.IOException;

@SuppressWarnings("serial")
//客户端聊天界面 作为客户端登陆和启动客户端的媒介 
//需要多线程实现 
//1.打开客户端聊天界面 
//2.启动客户端支持客户端相应的服务 
//3.客户端并发让客户端能同时接收和发送消息
//4.生成客户端聊天界面的各种组件
public class CreateClientFrame extends JFrame
{
	//聊天界面
	public static CreateClientFrame frame = null;
			
	//各种文本框
	public static JTextArea messege = null;
	public static JTextArea input = null;
	
	//在线用户列表 和dlm配套使用
	public static JList<String> clientList = null;
	public static DefaultListModel<String> dlmclientList = null;
	
	//在线群组列表 和dlm配套使用
	public static JList<String> groupList = null;
	public static DefaultListModel<String> dlmgroupList = null;
	
	//文件列表 和dlm配套使用
	public static JList<String> fileList = null;
	public static DefaultListModel<String> dlmfileList = null;
	
	//选取的群成员列表
	public static JList<String> memberList = null;
	public static DefaultListModel<String> dlmmemberList = null;
	
	//各种按钮
	public static JButton sendmsgButton = null;
	public static JButton clearButton = null;
	public static JButton fileButton = null;
	public static JButton exitButton = null;
	public static JButton publicButton = null;
	public static JButton createGroupButton = null;
	public static JButton resetmemberButton = null;
	public static JButton fileRefreshButton = null;
	public static JButton openfolderButton = null;
	
	//聊天模式标签 用于决定聊天模式
	public static JLabel chatModel = null;
	
	//其他成员信息
	public static String username = null;
	
	public static void Create()
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					username = ClientLogin.username;
					//设置聊天界面可见
					frame = new CreateClientFrame();
					frame.setVisible(true);
					//客户端先开启监听
					ClientListen clientListen = new ClientListen();
					clientListen.start();
					
					Client client = new Client(clientListen);
					client.start();
					
					
					
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public CreateClientFrame()
	{
		super("聊天室客户端 - " + username);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try
				{
					ClientLogin.dout.writeUTF("--exit");
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		});
		setBounds(100, 100, 943, 606);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		//各种滚动条
		JScrollPane messegeScrollPane = new JScrollPane();
		messegeScrollPane.setBounds(10, 39, 533, 322);
		getContentPane().add(messegeScrollPane);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(553, 40, 168, 145);
		getContentPane().add(scrollPane);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(759, 40, 158, 145);
		getContentPane().add(scrollPane_1);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(10, 391, 533, 108);
		getContentPane().add(scrollPane_2);
		
		
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(553, 213, 168, 148);
		getContentPane().add(scrollPane_3);
		
		JScrollPane scrollPane_4 = new JScrollPane();
		scrollPane_4.setBounds(759, 214, 158, 145);
		getContentPane().add(scrollPane_4);
		
		messege = new JTextArea();
		messegeScrollPane.setViewportView(messege);
		messege.setEditable(false);
		
		input = new JTextArea();
		scrollPane_2.setViewportView(input);
		
		//在线用户列表
		dlmclientList = new DefaultListModel<String>();
		clientList = new JList<String>();
		clientList.setModel(dlmclientList);
		scrollPane.setViewportView(clientList);
		
		//在线群组列表
		dlmgroupList = new DefaultListModel<String>();
		groupList = new JList<String>();
		groupList.setModel(dlmgroupList);
		scrollPane_1.setViewportView(groupList);
		
		//文件列表
		dlmfileList = new DefaultListModel<String>();
		fileList = new JList<String>();
		fileList.setModel(dlmfileList);
		scrollPane_3.setViewportView(fileList);
		
		//选取的组成员列表
		dlmmemberList = new DefaultListModel<String>();
		memberList = new JList<String>();
		memberList.setModel(dlmmemberList);
		scrollPane_4.setViewportView(memberList);
		
		//各种按钮
		sendmsgButton = new JButton("\u53D1\u9001\u6D88\u606F");
		sendmsgButton.setBounds(10, 509, 91, 48);
		getContentPane().add(sendmsgButton);
		
		clearButton = new JButton("\u6E05\u5C4F");
		clearButton.setBounds(451, 509, 91, 48);
		getContentPane().add(clearButton);
		
		fileButton = new JButton("\u53D1\u9001\u6587\u4EF6");
		fileButton.setBounds(188, 509, 91, 48);
		getContentPane().add(fileButton);
		
		publicButton = new JButton("\u516C\u5C4F\u804A\u5929");
		publicButton.setBounds(99, 509, 91, 48);
		getContentPane().add(publicButton);
		
		createGroupButton = new JButton("\u521B\u5EFA\u7FA4\u7EC4");
		createGroupButton.setBounds(277, 509, 91, 48);
		getContentPane().add(createGroupButton);
		
		resetmemberButton = new JButton("\u91CD\u9009\u6210\u5458");
		resetmemberButton.setBounds(362, 509, 91, 48);
		getContentPane().add(resetmemberButton);
		
		fileRefreshButton = new JButton("\u5237\u65B0");
		fileRefreshButton.setBounds(553, 364, 71, 23);
		getContentPane().add(fileRefreshButton);
		
		openfolderButton = new JButton("\u6253\u5F00");
		openfolderButton.setBounds(634, 364, 80, 23);
		getContentPane().add(openfolderButton);
		
		//各种标签
		chatModel = new JLabel("\u516C\u5C4F\u804A\u5929");
		chatModel.setBounds(10, 358, 533, 34);
		getContentPane().add(chatModel);
		
		JLabel noticeLabel = new JLabel("123");
		noticeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		noticeLabel.setBounds(553, 391, 363, 166);
		noticeLabel.setText("<html><body>你的用户名 : " + username
				 		    + "<br>私聊 : 双击要私聊的用户"
							+ "<br>建立群组 : 在在线用户列表中双击想要的群组成员再点创建群组" 
							+ "<br>群聊        : 双击想群聊的群或用户名再发消息" 
							+ "<br>上传文件  : 点发送文件按钮选择文件发送"
							+ "<br>下载文件  : 双击文件列表中的文件" 
							+ "<br>在线用户和群组列表会在创建时自动更新"
							+ "<br>下载的文件保存在D:\\client\\username目录下"
							+ "<body></html>");
		getContentPane().add(noticeLabel);
		
		JLabel clientsLabel = new JLabel("\u5F53\u524D\u5728\u7EBF\u7528\u6237");
		clientsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		clientsLabel.setBounds(553, 16, 168, 15);
		getContentPane().add(clientsLabel);
		
		JLabel groupLabel = new JLabel("\u5F53\u524D\u7FA4\u7EC4");
		groupLabel.setHorizontalAlignment(SwingConstants.CENTER);
		groupLabel.setBounds(759, 16, 158, 15);
		getContentPane().add(groupLabel);
		
		JLabel lblNewLabel = new JLabel("\u4E0A\u4F20\u5230\u670D\u52A1\u5668\u7684\u6587\u4EF6");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(553, 195, 168, 15);
		getContentPane().add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("\u5DF2\u9009\u62E9\u7684\u7FA4\u6210\u5458");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(759, 195, 158, 15);
		getContentPane().add(lblNewLabel_1);
		
		
		JLabel lblNewLabel_2 = new JLabel("\u6D88\u606F\u533A");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(10, 16, 533, 15);
		getContentPane().add(lblNewLabel_2);
		
		
	}
}

