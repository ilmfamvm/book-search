import java.util.HashMap;

public class CacheManager {
	private static HashMap<String, Object> cache = new HashMap<>();
	private static int count = 0;
	private static final int limit = 100;

	private CacheManager() {
	}

	public static boolean put(String key, Object value) {
		if (count < limit) {
			count++;
			cache.put(key, value);
			return true;
		}

		return false;
	}

	public static void flush() {
		cache.clear();
		count = 0;
	}

	public static Object get(String key) {
		return cache.get(key);
	}
}
