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
	private AsynchronousServerSocketChannel serverChannel;
	private ExecutorService executorService = Executors.newFixedThreadPool(10);

	public void start(int port) throws IOException
	{
		serverChannel = AsynchronousServerSocketChannel.open();
		serverChannel.bind(new InetSocketAddress(port));
		serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>()
		{
			@Override
			public void completed(AsynchronousSocketChannel clientChannel, Void attachment)
			{
				serverChannel.accept(null, this);
				executorService.submit(new SocketHandler(clientChannel));
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
		EchoServer server = new EchoServer();
		server.start(8080);
	}

	private static class SocketHandler implements CompletionHandler<Integer, ByteBuffer>
	{
		private final AsynchronousSocketChannel clientChannel;
		private final ByteBuffer buffer;

		public SocketHandler(AsynchronousSocketChannel clientChannel)
		{
			this.clientChannel = clientChannel;
			buffer = ByteBuffer.allocate(1024);
		}

		@Override
		public void completed(Integer result, ByteBuffer attachment)
		{
			if (result == -1)
			{
				try
				{
					clientChannel.close();
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

				clientChannel.write(attachment, attachment, this);
			}

			attachment.clear();
			clientChannel.read(attachment, attachment, this);
		}

		@Override
		public void failed(Throwable exc, ByteBuffer attachment) {
			// handle exception
		}
	}
}
