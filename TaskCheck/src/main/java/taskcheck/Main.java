package taskcheck;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Main {

	/** application.properties **/
	private final int timeOut;
	private final int timeToCheck;
	private final String fileToCheck;
	private final String processName;

	/** local **/
	private final LocalDateTime startedTime;
	private final LocalDateTime endTime;
	private final File file;
	private final Log log;
	
	private static final String TASK_LIST_RUNNING_COMMAND = "TASKLIST /FI \"STATUS eq RUNNING\"";
	private static final String RESTART_COMMAND = "schtasks /run /tn ";
	
	private static class Builder {
		private String applicationProperties = null;
		
		public Builder applicationProperties(String fileName){
			this.applicationProperties = fileName;
			return this;
		}
		
		Main build() throws FileNotFoundException, IOException{
			return new Main(this);
		}
	}
	
	private Main(Builder builder) throws FileNotFoundException, IOException {

		if (builder.applicationProperties != null) {
			ApplicationProperties.INSTANCE.setFileName(builder.applicationProperties);
		}else {
			String defaultApplicationProperties = ApplicationProperties.INSTANCE.defaultFileName(); 
			ApplicationProperties.INSTANCE.setFileName(defaultApplicationProperties);
		}
			
		
		/** set app prop variables **/
		this.timeOut = ApplicationProperties.INSTANCE.Timeout();
		this.timeToCheck = ApplicationProperties.INSTANCE.TimeToCheck();
		this.fileToCheck = ApplicationProperties.INSTANCE.FileToCheck();
		this.processName = ApplicationProperties.INSTANCE.ProcessName();

		this.startedTime = LocalDateTime.now();
		this.endTime = this.startedTime.plusMinutes(this.timeOut);
		this.file = new File(this.fileToCheck);
		this.log = new Log.Builder()
				.className(this.getClass().getName())
				.build();
	}

	public static void main(String...args) throws FileNotFoundException, IOException {
		String appPropFile = (args.length > 0) ? appPropFile = args[0] : null; 
		Main taskCheck = new Main.Builder().applicationProperties(appPropFile).build();
		taskCheck.execute();
	}
	
	private void execute() {
		
		log.writeln(file.getAbsolutePath());
		
		/** Execute the taskCheck every TIME_TO_CHECK, until END_TIME **/
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		try {
			
			executor.scheduleAtFixedRate(()-> {

				Result res = taskCheck();

				log.writeln(res.get());

				if (!res.equals(Result.RUNNING) && !res.equals(Result.TO_RESTART)) shutdown(executor);

				if (res.equals(Result.TO_RESTART)) {
					
					/** Send email and restart the process **/
					boolean r = restart(processName);
					if (!r) log.writeln("restart error - the process" + this.processName + " has not been restarted");
				}

			},0, timeToCheck,TimeUnit.MINUTES);

		} catch (Exception ex) {
			shutdown(executor);
		}
    }

	private Result taskCheck() {
		
		/** Exit if end time passed **/
		if (timeFinished(this.endTime)) {
			log.writeln("NOW:" + LocalDateTime.now() + " END TIME:" + this.endTime);
			return Result.TIME_FINISHED;
		}

		/** Read the file and do the check **/
		FileCheck fileCheck = new FileCheck(file);

		/** Return TO_RESTART if it's present an error into the log file **/
		if (fileCheck.checkError()) {
			return Result.TO_RESTART;	
		}

		/** Check if the process is still running **/
		if (processRunning(processName)) return Result.RUNNING;
		
		return Result.RES_NOT_FOUND;
	}

	/** Check if it's the time to finish: after END_TIME hours from STARTED_TIME **/
	private static boolean timeFinished(LocalDateTime endTime) {
		return LocalDateTime.now().isAfter(endTime);
	}
	
	/** Check if the process is still running **/
	private static boolean processRunning(String pName) {
		try {

			boolean found = false;

			Process p = Runtime.getRuntime().exec(TASK_LIST_RUNNING_COMMAND);
			
			String task;
			BufferedReader buff = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((task = buff.readLine()) != null && !found)
	        {
	        	if (task.contains(pName)) found = true;
	        }
			
			return found;

		} catch (Exception ex) {
			return false;
		}
	}
	
	/** Re-start the task **/
	private static boolean restart(String pName) {
		try {
			
			if (!processRunning(pName)) { 
				Runtime.getRuntime().exec(RESTART_COMMAND + pName);
				return true;
			}
			
			return false;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/** Shutdown the executor **/
	private static void shutdown(ScheduledExecutorService executor) {
		
		executor.shutdown();
		
		try {
		    if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
		        executor.shutdownNow();
		    } 
		} catch (InterruptedException ie) {
		    executor.shutdownNow();
		}		
	}
}