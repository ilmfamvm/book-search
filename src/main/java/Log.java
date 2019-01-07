public class Log {

	private enum level {
		INFO, ERROR, WARNING
	}

	public static void info(String msg) {
		System.out.println("(" + level.INFO + ") " + msg);
	}

	public static void error(String msg) {
		System.out.println("(" + level.ERROR + ") " + msg);
	}

	public static void warning(String msg) {
		System.out.println("(" + level.WARNING + ") " + msg);
	}

	public static void main(String[] args) {
		Log.info("test");
		Log.error("test");
		Log.warning("test");
	}
}
