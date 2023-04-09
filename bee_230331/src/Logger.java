import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private final String logPath;
	private final int maxLogFileSizeInBytes;
	private final Level level;
	private volatile BufferedWriter writer;
	private volatile File currentLogFile;
	private volatile LocalDateTime currentLogDate;
	private volatile int logFileCount;

	public enum Level {
		DEBUG, INFO, WARNING, ERROR
	}

	public Logger(String logPath, int maxLogFileSizeInBytes, Level level) throws IOException {
		this.logPath = logPath;
		this.maxLogFileSizeInBytes = maxLogFileSizeInBytes;
		this.level = level;
		rotateLogFileIfNeeded(LocalDateTime.now());
	}

	public synchronized void log(Level level, String message) {
		if (level.ordinal() < this.level.ordinal())
			return;

		try
		{
			LocalDateTime logTime = LocalDateTime.now();
			String timestamp = logTime.format(dateTimeFormatter);
			rotateLogFileIfNeeded(logTime);
			writer.write("[" + timestamp + "] " + level.toString() + ": " + message + "\n");
			writer.flush();
		}
		catch (IOException e)
		{
			System.err.println("Failed to write log message: " + e.getMessage());
		}
	}

	private synchronized void rotateLogFileIfNeeded(LocalDateTime logTime)
	{
		if (currentLogDate != null &&
			logTime.toLocalDate() == currentLogDate.toLocalDate() &&
			currentLogFile.length() < maxLogFileSizeInBytes)
			return;

		String logFileName;
		do {
			logFileName = logTime.format(dateFormatter)
					+ (logFileCount > 0 ? String.format("_%03d", logFileCount) : "")
					+ ".log";
			logFileCount++;
		} while (new File(logPath, logFileName).exists());
		try
		{
			File newLogFile = new File(logPath, logFileName);
			writer = new BufferedWriter(new FileWriter(newLogFile));
			currentLogFile = newLogFile;
			currentLogDate = logTime;
			logFileCount = 0;
		}
		catch (IOException e)
		{
			System.err.println("Failed to create log file: " + e.getMessage());
		}
	}
}
