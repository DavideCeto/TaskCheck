package taskcheck;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

final class Log {

	private static final String NEWLINE = "\n";
	private static final String OPEN_BRACKET = "[";
	private static final String CLOSED_BRACKET = "]";
	
	private final String fileName;
	private final long logSize;
	private final String className;
	private final Path filePath;
	
	static class Builder{
		private String className = null;
		
		Builder className(String className) {
			this.className = className;
			return this;
		}

		Log build() {
			if (this.className == null) return null;
			return new Log(this);
		}
	}

	private Log(Builder builder){
		this.fileName = ApplicationProperties.INSTANCE.LogFileName();
		this.logSize = ApplicationProperties.INSTANCE.LogSize();
		this.className = builder.className;
		this.filePath = Path.of(this.fileName);
	}

	void writeln(String logString) {
		String log = logString.concat(NEWLINE);
		write(log);
	}
	
	void write(String logString) {
		try {
			String msgLog = LocalDateTime.now(Clock.system(ZoneId.systemDefault())).toString().concat(OPEN_BRACKET).concat(this.className).concat(CLOSED_BRACKET).concat(logString);
			if (!Files.exists(this.filePath)) {
				writeNewLog(msgLog);
				return;
			}
			if (isOverSized()) {
				saveLog();
				writeNewLog(msgLog);
			}else {
				appendLog(msgLog);
			}
		} catch (IOException e) {
			System.out.println("Write log error:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private long getLogSize() throws IOException {
		if (Files.exists(this.filePath)) return Files.size(this.filePath);
		return 0;
	}
	
	private boolean isOverSized() throws IOException {
		return getLogSize() > this.logSize;
	}
	
	private void appendLog(String msgLog) throws IOException {
		Files.writeString(this.filePath, msgLog, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
	}
	
	private void writeNewLog(String msgLog) throws IOException {
		Files.writeString(this.filePath, msgLog, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
	}
	
	private void saveLog() throws IOException {
		LocalDateTime fileTime = Files.getLastModifiedTime(this.filePath).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		String fileName = this.fileName.concat(fileTime.toString().replace(":",""));
		Path newPath = Path.of(fileName);
		Files.copy(this.filePath, newPath);
	}
}