package taskcheck;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

enum ApplicationProperties {
	INSTANCE;

	private static final String DEFAULT_PROP_FILENAME = "//src//main//resources//application.properties";
	private String fileName = null;
	private static final Path ROOT_PATH = Paths.get("").toAbsolutePath();
	private final Properties appProp = new Properties();
	
	void setFileName(String fileName) throws FileNotFoundException, IOException{
		if (this.fileName == null) {
			this.fileName = fileName;
			String appConfigPath = ROOT_PATH.toString().concat(fileName);
			appProp.load(new FileInputStream(appConfigPath));
		}
	}
	
	String defaultFileName() {
		return ApplicationProperties.DEFAULT_PROP_FILENAME;
	}

	int Timeout() {
		return Integer.valueOf(appProp.getProperty("timeout"));
	}
	
	int TimeToCheck() {
		return Integer.valueOf(appProp.getProperty("timeToCheck"));
	}
	
	String FileToCheck() {
		return appProp.getProperty("fileToCheck").trim();
	}
	
	String ProcessName() {
		return appProp.getProperty("processName").trim();
	}
	
	String LogFileName() {
		return appProp.getProperty("logFileName").trim();
	}
	
	long LogSize() {
		return Long.valueOf(appProp.getProperty("logSize"));
	}
}
