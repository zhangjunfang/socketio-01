package bhz.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server implements Runnable {

	//1 读取缓冲区
	private ByteBuffer readBuf = ByteBuffer.allocate(1024);
	//2 写入缓冲区
	private ByteBuffer writeBuf = ByteBuffer.allocate(1024);
	//2 多路复用器
	private Selector selector;
	
	public Server(int port) {
		try {
			//1 打开多路复用器
			this.selector = Selector.open();
			//2 打开服务器端的通道
			ServerSocketChannel ssc = ServerSocketChannel.open();
			//3 设置通道的阻塞模式
			ssc.configureBlocking(false);
			//4 绑定地址
			ssc.bind(new InetSocketAddress(port));
			//5 把服务器通道注册到多路复用器上, 并且监听阻塞事件
			ssc.register(this.selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(true){
			try {
				//1 必须要让多路复用器开始监听
				this.selector.select();
				//2 返回多路复用器里所有注册的通道Key
				Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
				//3 遍历获取的key
				while(it.hasNext()){
					//4 接受key值
					SelectionKey key = it.next();
					//5 从容器中移除已经被选中的key
					it.remove();
					//6 验证操作:判断key是否有效
					if(key.isValid()){
						//SelectionKey.OP_CONNECT
						//SelectionKey.OP_ACCEPT
						//SelectionKey.OP_READ
						//SelectionKey.OP_WRITE

						//7 如果为阻塞状态: OP_ACCEPT
						if(key.isAcceptable()){
							this.accept(key);
						}
						//8 如果为可读状态: OP_READ
						if(key.isReadable()){
							this.read(key);
						}
						//9...
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 监听可读状态方法执行
	 * @param key
	 */
	private void read(SelectionKey key) {
		try {
			//1 对缓冲区进行清空
			this.readBuf.clear();
			//2 获取之前注册的socketChannel通道对象
			SocketChannel sc = (SocketChannel) key.channel();
			//3 从通道里获取数据 放入缓冲区
			int index = sc.read(this.readBuf);
			if(index == -1){
				key.channel().close();
				key.cancel();
				return;
			}
			//4 由于sc通道里的数据流入到readBuf容器中,所以 readBuf里面的position一定发生了变化，必须要进行复位
			this.readBuf.flip();
			//读取readBuf数据 然后打印到控制台
			byte[] bytes = new byte[this.readBuf.remaining()];
			this.readBuf.get(bytes);
			
			String body = new String(bytes).trim();
			System.err.println("服务器接收数据: " + body);
			
			//5 写出数据：
			//...
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * 监听阻塞状态方法执行
	 * @param key
	 */
	private void accept(SelectionKey key) {
		try {
			//1 由于目前是server端，那么一定是server端启动 并且处于阻塞状态 所以获取阻塞状态为的key 一定是:ServerSocketChannel
			ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
			//2 通过调用 accept()方法, 返回一个具体的客户端连接句柄
			SocketChannel sc = ssc.accept();
			//3 设置客户端通道为非阻塞
			sc.configureBlocking(false);
			//4 设置当前获取的客户端连接句柄为可读状态位
			sc.register(this.selector, SelectionKey.OP_READ);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		new Thread(new Server(8765)).start();
		
	}

}
