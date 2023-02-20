package taskcheck;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;

final class FileCheck {
	
	private final String fileContent;
	private final Path filePath;
	private static final Log log = new Log.Builder().className(FileCheck.class.getName()).build();
	
	FileCheck(File file) {
		this.filePath = file.toPath();  
		this.fileContent = readContent(this.filePath);
	}

	private static String readContent(Path filePath) {
		try {

			if (!Files.exists(filePath)) {
				log.writeln(filePath.getFileName() + " not exists");
				return null;
			}
			
			final byte[] b = Files.readAllBytes(filePath);
			final String s = String.valueOf(b);
			return s;
		}
		catch (IOException e) {
			log.writeln("error read content:" + e.getMessage());
			return null;
		}
	}

	private static LocalDate readModificationDate(Path filePath) {
		try {
			LocalDate lastModDate = Files.getLastModifiedTime(filePath).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();	
			return lastModDate;
		}
		catch (IOException e) {
			log.writeln("error read modificatin date:" + e.getMessage());
			return null;
		}
	}
	
	/** Check if the file content is not null and it's length is > 0 **/
	private static boolean checkValidFile(String fileContent) {
		return fileContent != null && fileContent.length() > 0;
	}
	
	/** Check if the last modification date of the file is equal to date now **/
	private static boolean checkModificationDate(Path filePath) {
		LocalDate modificationDate = readModificationDate(filePath);
		return modificationDate != null && modificationDate.isEqual(LocalDate.now());
	}
	
	boolean checkError() {
		return checkValidFile(this.fileContent) && checkModificationDate(this.filePath) && (this.fileContent.indexOf("Exception") > 0 || this.fileContent.indexOf("Error") > 0);
	}
}