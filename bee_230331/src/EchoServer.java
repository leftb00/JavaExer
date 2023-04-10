import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer
{
	private AsynchronousServerSocketChannel serverChannel_;
	private ExecutorService executorService_;
	//private int poolSize_;

	public EchoServer(int port, int poolSize) throws IOException
	{
		//this.poolSize_ = poolSize;
		serverChannel_ = AsynchronousServerSocketChannel.open();
		serverChannel_.bind(new InetSocketAddress(port));
		executorService_ = Executors.newFixedThreadPool(poolSize);
	}

	public void start()
	{
		serverChannel_.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>()
		{
			@Override
			public void completed(AsynchronousSocketChannel clientChannel_, Void attachment)
			{
				serverChannel_.accept(null, this);
				executorService_.submit(new SocketHandler(clientChannel_));
			}

			@Override
			public void failed(Throwable exc, Void attachment)
			{
				// handle exception
			}
		});
	}

	public static void main(String[] args) throws IOException
	{
		EchoServer server = new EchoServer(8080, 10);
		server.start();
	}

	private class SocketHandler implements Runnable
	{
		private AsynchronousSocketChannel clientChannel_;
		private ByteBuffer buffer_;

		public SocketHandler(AsynchronousSocketChannel clientChannel)
		{
			clientChannel_ = clientChannel;
			buffer_ = ByteBuffer.allocate(1024);
		}

		@Override
		public void run()
		{
			clientChannel_.read(buffer_, buffer_, new CompletionHandler<Integer, ByteBuffer>()
			{
				@Override
				public void completed(Integer result, ByteBuffer attachment)
				{
					if (result == -1)
					{
						try
						{
							clientChannel_.close();
						}
						catch (IOException e)
						{
							// handle exception
						}
						return;
					}

					if (result > 0)
					{
						attachment.flip();
						String message = new String(attachment.array(), 0, result);
						System.out.println("Received from client: " + message);

						String response = message.toUpperCase();
						attachment.clear();
						attachment.put(response.getBytes());
						attachment.flip();

						clientChannel_.write(attachment, attachment, this);
					}

					attachment.clear();
					clientChannel_.read(attachment, attachment, this);
				}

				@Override
				public void failed(Throwable exc, ByteBuffer attachment)
				{
					// handle exception
				}
			});
		}
	}
}
