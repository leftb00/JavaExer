package BLib;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class BLogger
{
	private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private final String logDirPath_;
	private final int logFileMaxSize_;
	private final LogLevel logLevel_;
	private final Charset charset_;
	private final ExecutorService executorService_;
	private final BlockingQueue<String> logQueue_;

	private volatile boolean running_;
	private volatile String currentLogDate_;
	private volatile Path currentLogFilePath_;
	private PrintStream printStream_;

	public BLogger(String logDirPath, int logFileMaxSize, LogLevel logLevel)
			throws IOException
	{
		logDirPath_ = logDirPath;
		logFileMaxSize_ = logFileMaxSize;
		logLevel_ = logLevel;
		charset_ = StandardCharsets.UTF_8;
		logQueue_ = new LinkedBlockingQueue<>();
		executorService_ = Executors.newSingleThreadExecutor();
		currentLogDate_ = "";

		createNewLogFileIfNeeded(LocalDateTime.now().format(DATE_FORMAT));
		startLogWriterThread();
	}

	public void log(LogLevel level, String message)
	{
		if (level.getLevel() < logLevel_.getLevel())
			return;

		String timestamp = LocalDateTime.now().format(TIME_FORMAT);
		String logMessage = String.format("%s [%s] %s\n", timestamp, level.name(), message);
		try
		{
			logQueue_.put(logMessage);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new RuntimeException("Log message interrupted.", e);
		}
	}

	public void error(String message)
	{
		log(LogLevel.ERROR, message);
	}

	public void warning(String message)
	{
		log(LogLevel.WARNING, message);
	}

	public void info(String message)
	{
		log(LogLevel.INFO, message);
	}

	public void debug(String message)
	{
		log(LogLevel.DEBUG, message);
	}

	// 화면 출력용 stream
	public void setOutputStream(PrintStream printStream)
	{
		printStream_ = printStream;
	}

	private void createNewLogFileIfNeeded(String logDate)
			throws IOException
	{
		if (logDate == currentLogDate_ &&
				Files.exists(currentLogFilePath_) &&
				Files.size(currentLogFilePath_) < logFileMaxSize_)
			return;

		String logFileName;
		Path logFilePath;
		int logFileCount = 0;
		do
		{
			logFileName = logDate
					+ (logFileCount > 0 ? String.format("_%03d", logFileCount) : "")
					+ ".log";
			logFilePath = Paths.get(logDirPath_, logFileName);
			logFileCount++;
		} while (Files.exists(logFilePath));

		try
		{
			currentLogFilePath_ = logFilePath;
			currentLogDate_ = logDate;
			Files.createFile(currentLogFilePath_);
		}
		catch (IOException e)
		{
			System.err.println("Failed to create log file: " + e.getMessage());
		}
	}

	private void startLogWriterThread()
	{
		running_ = true;
		executorService_.submit(() ->
		{
			try
			{
				while (running_)
				{
					String logMessage = logQueue_.take();
					int dateFormatLen = DATE_FORMAT.toString().length();
					String logDate = logMessage.substring(0, dateFormatLen);
					Files.write(currentLogFilePath_,
							logMessage.substring(dateFormatLen+1).getBytes(charset_),
							StandardOpenOption.APPEND);
					if(printStream_ != null)
						printStream_.print(logMessage.substring(TIME_FORMAT.toString().length()+1));
					createNewLogFileIfNeeded(logDate);
				}
			}
			catch (IOException | InterruptedException e)
			{
				throw new RuntimeException("Log writer thread failed.", e);
			}
		});
	}

	public void close()
	{
		running_ = false;
		executorService_.shutdown();
		try
		{
			executorService_.awaitTermination(1, java.util.concurrent.TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
	}

	public enum LogLevel
	{
		DEBUG(0), INFO(1), WARNING(2), ERROR(3);

		private final int level;

		LogLevel(int level)
		{
			this.level = level;
		}

		public int getLevel()
		{
			return level;
		}
	}
}
