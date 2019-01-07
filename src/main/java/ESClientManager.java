import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class ESClientManager {
	private static RestHighLevelClient CLIENT = new RestHighLevelClient(
			RestClient.builder(new HttpHost("localhost", 9200, "http")));
	private static boolean isClosed = false;

	public static RestHighLevelClient get() {
		if (isClosed) {
			CLIENT = new RestHighLevelClient(
					RestClient.builder(new HttpHost("localhost", 9200, "http")));
			isClosed = false;
		}
		return CLIENT;
	}

	public static void close() {
		try {
			CLIENT.close();
		} catch (IOException e) {
			Log.error("Failed to close client, " + e.getMessage());
		}
		isClosed = true;
	}
}
