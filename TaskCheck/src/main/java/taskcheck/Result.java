package taskcheck;

enum Result {
	
	TIME_FINISHED("Time finished!"),
	TO_RESTART("To restart!"),
	RUNNING("Running!"),
	RES_NOT_FOUND("Result not found");
	
	private String message;
	
	Result(String msg){
		this.message = msg;
	}
	public String get() {
		return this.message;
	}
}
