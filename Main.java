package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
//클라이언트의 모듈 개발
//클라이언트 프로그램 같은 경우는 서버프로그램 과 다르게 굳이 여러개의 쓰레드가 동시다발절으로 생기는 경우X-->쓰레드 풀 X
//기본적인 쓰레드 
//서버로 메세지를 전달하기 위한 쓰레드, 전달받는 쓰레드 총 2개
public class Main extends Application 
{
	
	
	Socket socket;
	TextArea textArea;
	
	//클라이언트 프로그램 동작 메소드 
	public void startClient(String IP, int port) //어떤 IP와 port번호 로 접속을 할지 설정해준다.
	{
		Thread thread = new Thread() //쓰레드 객체 사용 
		{
			public void run() 
			{
				try {
					socket = new Socket(IP,port);//try구문 안에서 소켓 초기화 
					receive();//초기화 이후 서버로 부터 메세지를 전달받기 위해 receive()
					
				}catch(Exception e) {
					
					if( !socket.isClosed() ){//오류가 발생한 경우 
						stopClient();//클라이언트 종료 
						System.out.println("[서버 접속 실패]");
						Platform.exit();//프로그램 자체 종료
					}
				}
			}
		};
		thread.start();  
	}
	
	//클라이언트 프로그랢 종료 메소드
	public void stopClient() {
		try {
			if(socket != null && socket.isClosed()) //만약에 소켓이 열려 있는 상태라면 
			{
				socket.close();//소켓객체 자원 해제             
			}
		}
		catch(Exception e) 
		{
			e.printStackTrace(); 
		}
	}
	
	
	//서버로 부터 메세지를 전달 받는 메소드
	public void receive() {
		while(true) {
			try {
				//소켓에서 InputStream을 열어서 현재 서버로 부터 메세지를 전달 받을 수 있게 만들어줌
				InputStream in = socket.getInputStream();
				
				byte[] buffer = new byte[512];//버퍼를 만들어서 총 512바이트 만큼 계속해서 끊어서 버퍼에 담을 수 있게 만들어 준다.
				int length = in.read(buffer);//read함수를 이용해 실제로 입력을 받도록 해준다
				if(length == -1) throw new IOException();//입력 받는 도중 오류가 발생한다면 IOException
				String message = new String(buffer,0,length,"UTF-8");//실제로 버퍼에 있는 정보를 length만큼 메세지라는 변수에 담아서 출력을 시킨다.
				
				Platform.runLater
				(
					()->{ 
						textArea.appendText(message);
						}
				);
			}catch(Exception e) {
				stopClient();
				break;//오류가 있을 경우 클라이언트 종료 후,무한 루프 빠져나옴
			}
		}
	}
	
	//서버로 메세지를 전송하는 메소드
	public void send(String message) 
	{
		
		Thread thread = new Thread() 
		{
			public void run() 
			{
				try 
				{
					OutputStream out = socket.getOutputStream();//메세지 전송
					byte[] buffer = message.getBytes("UTF-8");//보내고자 하는 메세지를 utf-8로 인코딩 해서 보야함(서버에서 전달 받을 때도 utf-8로 받음)
					out.write(buffer);//메세지 전송
					out.flush();//메세지 전송 끝 알리기 
				}
				catch(Exception e)
				{
					stopClient();
				}
			}
		};
		thread.start();
	}
	
	//실제로 프로그램을 동작 시키는 메소드
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		HBox hbox = new HBox();
		hbox.setSpacing(5);
		
		TextField userName = new TextField();
		userName.setPrefWidth(150);
		
		userName.setPromptText("닉네임을 입력하세요.");
		HBox.setHgrow(userName,Priority.ALWAYS);
		
		TextField IPText = new TextField("127.0.0.1");
		TextField portText = new TextField("9876");
		portText.setPrefWidth(80);
		
		hbox.getChildren().addAll(userName,IPText,portText);
		root.setTop(hbox);
		
		textArea = new TextArea();
		textArea.setEditable(false);
		root.setCenter(textArea);//3:02

		
		
		
		
		
		
		
		
		
		
		
		
	}//start()
	
	//프로그램 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
