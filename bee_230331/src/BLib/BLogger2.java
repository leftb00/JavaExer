package BLib;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BLogger2 extends Logger
{

	private final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private final String logDirPath_;
	private final int logFileMaxSize_;
	private final Level logLevel_;
	private final Charset charset_;

	private volatile String currentLogDate_;
	private volatile Path currentLogFilePath_;

	public BLogger2(String logDirPath, int logFileMaxSize, Level logLevel)
			throws IOException
	{
		super(BLogger.class.getName(), null);
		logDirPath_ = logDirPath;
		logFileMaxSize_ = logFileMaxSize;
		logLevel_ = logLevel;
		charset_ = StandardCharsets.UTF_8;
		currentLogDate_ = "";

		createNewLogFileIfNeeded(LocalDateTime.now().format(DATE_FORMAT));
		Handler logHandler = new LogHandler();
		logHandler.setFormatter(new SimpleFormatter());
		addHandler(logHandler);
	}

	public void log(Level level, String message)
	{
		if (level.intValue() >= logLevel_.intValue())
		{
			String timestamp = LocalDateTime.now().format(TIME_FORMAT);
			String logMessage = String.format("%s [%s] %s\n",
							timestamp, level.getName(), message);
			log(new LogRecord(level, logMessage));
		}
	}

	private void createNewLogFileIfNeeded(String logDate) throws IOException
	{
		if (logDate == currentLogDate_
				&& Files.exists(currentLogFilePath_)
				&& Files.size(currentLogFilePath_) < logFileMaxSize_)
		{
			return;
		}

		String logFileName;
		Path logFilePath;
		int logFileCount = 0;
		do
		{
			logFileName = logDate + (logFileCount > 0 ? String.format("_%03d", logFileCount) : "") + ".log";
			logFilePath = Paths.get(logDirPath_, logFileName);
			logFileCount++;
		} while (Files.exists(logFilePath));

		try {
			currentLogFilePath_ = logFilePath;
			currentLogDate_ = logDate;
			Files.createFile(currentLogFilePath_);
		} catch (IOException e) {
			System.err.println("Failed to create log file: " + e.getMessage());
		}
	}

	private class LogHandler extends Handler
	{
		@Override
		public void publish(LogRecord record) {
			try {
				String logMessage = getFormatter().format(record);
				int dateFormatLen = DATE_FORMAT.toString().length();
				String logDate = logMessage.substring(0, dateFormatLen);
				Files.write(currentLogFilePath_,
						logMessage.substring(dateFormatLen + 1).getBytes(charset_),
						java.nio.file.StandardOpenOption.APPEND);
				createNewLogFileIfNeeded(logDate);
			} catch (IOException e) {
				System.err.println("Failed to write log message: " + e.getMessage());
			}
		}

		@Override
		public void flush()
		{
		}

		@Override
		public void close() throws SecurityException
		{
		}

	}

	public enum LogLevel
	{
		DEBUG(0), INFO(1), WARNING(2), ERROR(3);

		private final int level;

		LogLevel(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}
	}
}
