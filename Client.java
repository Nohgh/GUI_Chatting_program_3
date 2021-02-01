package application;
import java.io.*;
import java.io.IOException;
import java.net.Socket;

//import org.omg.CORBA.portable.InputStream; ------> inputStream을 할때 io에 있는 걸로 써야지 이걸로 쓰면 안됨 절대!

public class Client
{//이름만 client고 서버안에 있는 클래스임
//클라이언트 프로그램이 챗서버안에 들어간다 x, 챗서버가 한명의 클라이언트와 통신을 하기 위해서 필요한 기능들을 클라이언트 클래스 안에 정의하겠다. 
//즉 한명의 클라이언트와 통신하도록 해주는 클라이언트 클래스 
	
	Socket socket ;	//통신을 하기 위해 필요한 소켓을 만들어줌
	
	public Client(Socket socket) 
	{	//생성자 -> 어떠한 변수에 초기화를 위해서 만들어줌
		this.socket = socket;
		receive();
		
	}
	
	//클라이언트로부터 어떠한 메세지를 받을 수 있도록 해주는 메소드   
	//메세지를 전달 받음과 동시에 전달 받은 메세지는 다른 클라이언트 한테 전송해줌
	public void receive() 
	{	//메세지가 전달이 되었을때 Runnable객체를 이용한다.
		//일반적으로 하나의 쓰레드를 만들때는 Runnable쿨래스를  많이 사용한다.
		Runnable thread = new Runnable() 
		{
				@Override
				public void run() 
				{	//Runnable라이브러리 같은경우는 내부에 반드시 run()메소드가 있어야 한다.	
					//하나의 쓰레드가 모듈로써 동작 할것인지 run()안에서 정의를 해준다.
					try {
						//try안에 while(true)를 넣어서 반복적으로 클라이언트로 부터 어떠한 내용을 전달받을 수 있게 해줌
						while(true) {
							
							InputStream in = socket.getInputStream();
							//받을때 ====> getInputStream을 이용해서 어떠한 내용을 read()함수로 받아온다 
							//보낼때 ====> getOutputStream을 이용해서메세지를 전송할 수 있다.
							//어떠한 내용을 전달받을 수 있도록 inputStream객체를 만들어줌`
							
							byte[] buffer = new byte[512];
							//버퍼를 이용해 한번에 512바이트를 받아옴
						
							int length = in.read(buffer); 
							//어떠한 내용을 read()함수로 받아온다 
							//length : 실제로 클라이언트로 부터 어떠한 내용을 전달받아서 버퍼(-> 매개변수)에 담아 주도록 한것이다
							//length: 실제로 메세지에 담긴 크기 
							while(length == -1) throw new IOException();
							//length가 -1이라면 즉, 어떠한 값을 읽을때 오류가 발생했다면 오류가 발생했다는것을 알려주면 된다.IOException
							
							System.out.println( "[메세지 수신 성공]" //메세지 받았다면 수신 성공
							+ socket.getRemoteSocketAddress() //현재 접속한 클라이언트의 ip 주소와 같은 주소 정보를 출력하도록 만들어준다.
							+ ": " + Thread.currentThread().getName() );//쓰레드의 고유한 이름을 출력해준다.
							
							String message = new String (buffer,0,length,"UTF-8");
							//전달받은 값을 utf8로 한글도 포함할 수 있도록 해준다  , 우리가 이 버퍼에서 전달받은 내용을 메세지리는 문자열 변수에 담아서 출력할 수있도록 해줌 
							
							
							//단순하게 메세지를 전달받는것이 아니라 전달받은 메세지를 다른 클라이언트 에게 보낼 수 있도록 만들어준다.
							for(Client client : Main.clients) 
							{//clients: main클래스에 있는 vector로 만들어진 변수--> Client client에 담아서 하나씩 출력 
								client.send(message);
								//밑에 있는 send() 매소드 이용
								//채팅서버를 만드는것이기 때문에 클라이언트로 부터 어떠한 메세지를 받으면 그내용을 다른 클라이언트에게 그대로 보내줌
								//10분 30초
							}
						}//while(true)
						
					}catch(Exception e) {
						
							try //중첩된 형식으로 try-catch문 사용
							{
								System.out.print("[메세지 수신 오류]"
									+socket.getRemoteSocketAddress()	//메세지를 받는 과정에서 오류가 발생했다면 메세지를 보낸 클라이언트의 네트워크 주소를 출력해줌
									+": "+ Thread.currentThread().getName()  //해당쓰레드 고유이름 출력해줌
									);
								
							}catch(Exception e2) 
							{
								e2.printStackTrace();
							}
						
					}//catch
					
				}//run()메소드
		};//Runnable
		
		Main.threadPool.submit(thread);// 쓰레드 풀에 만들어진 하나의 쓰레드(runnable로 만들어진)를 등록시켜 주겠다는것
		//Rannable로 쓰레드 하나를 정의해주고 submit(한글뜻: 제출하다)으로 메인쓰레드에 추가해준다.
		//클라이언트가 접속했을때 쓰레드를 만듬 그 쓰레드를 안정적으로 관리하기 위해서 쓰레드풀을 이용한다.
		
	}//receive() 함수 
	
	
	//클라이언트에게 메시지를 전송하는 메소드
	public void send(String message) 
	{
		Runnable thread = new Runnable() {
			@Override
			public void run() 
			{
				try 
				{
					OutputStream out = socket.getOutputStream();
					//OutputStream을 쓰는 이유: 위와 반대, 메세지를 보내주고자 할떄는 OutputStream을 이용해 메세지를 전송함
					byte[] buffer = message.getBytes("UTF-8");
					
					//오류가 발생하지 않았을때 
					out.write(buffer);	//버퍼에 담긴 내용을 서버에서 클라이언트로 전송을 해준다.
					out.flush();//flush를 써야 여기까지 성공적으로 전송했다는 것을 알려줄 수 있다.
				}
				catch(Exception e) {
					try //여기 try에 있는것들은 오류가 발생했을떄 실행
					{
						System.out.println("[메세지 송신 오류]"+
						socket.getRemoteSocketAddress()	
						//메세지를 받는 과정에서 오류가 발생했다면 메세지를 보낸 클라이언트의 네트워크 주소를 출력해줌
						+": "+ Thread.currentThread().getName()  //해당쓰레드 고유 이름 출력해줌
						);
						
						Main.clients.remove(Client.this);	
						//위와 다르게 별도로 오류가 발생했다면 메인클래스에 있는 clients, 
						//즉 모든 클라이언트에 대한  정보를 담는 일종의 배열에 대해서 현재 존재하는 클라이언트를 지워준다
						//오류가 발생해서 해당 클라이언트가 서버로 부터 접속이 끊겼으니까 당연히 우리 서버 안에서도 해당 클라이언트가 접속이 끊겼다는 정보를 처리할 수 있도록 해준다.
						//클라아이언트 배열에서 해당 오류가 생긴 클라이언트를 제거 해주는 것이다.
						socket.close();//오류가 생긴 클라이언트의 소켓을 닫아준다.  
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}//run
		};//Runnable 
		Main.threadPool.submit(thread);//Rannable로 쓰레드 하나를 정의해주고 submit(한글뜻: 제출하다)으로 메인쓰레드에 추가해준다.
	}//send()메소드
	//여기까지 해서 receive매소드와 send메소드를 이용해서 메세지를 전달받고 전달 할 수 있다.
}//class
 