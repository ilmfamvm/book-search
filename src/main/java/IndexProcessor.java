import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.RestStatus;

public class IndexProcessor {

	private static final String booksDir = "src/resources/books";

	public static boolean indexAll() {
		deleteIndex();
		if (!createIndex())
			return false;

		RestHighLevelClient client = ESClientManager.get();

		boolean hasException = false;
		File directory = new File(booksDir);
		int id = 1;
		for (File f : directory.listFiles()) {
			try {
				if (!f.isDirectory() && f.getName().indexOf("txt") > 0) {
					Map<String, String> bookMap = parseBook(f);
					IndexRequest indexRequest = new IndexRequest(Constants.INDEX_NAME, "doc", String.valueOf(id++))
							.source(bookMap);
					IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
					if (response.status() == RestStatus.CREATED) {
						Log.info("Indexed book " + f.getName());
					} else {
						Log.error("Failed to index " + f.getName());
						hasException = true;
					}
				}
			} catch (IOException e) {
				Log.error("Failed to index book " + f.getName() + " : " + e.getMessage());
				hasException = true;
			}
		}

		return !hasException;
	}

	private static Map<String, String> parseBook(File f) {
		Map<String, String> bookMap = new HashMap<>();
		bookMap.put(Constants.FIELD_FILE_NAME, f.getName());
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			StringBuilder bookBuffer = new StringBuilder();

			Pattern pTitle = Pattern.compile("^Title:.*$");
			Pattern pAuthor = Pattern.compile("^Author:.*$");
			Pattern pStart = Pattern.compile("^*START OF (THIS|THE) PROJECT GUTENBERG EBOOK*");
			Pattern pEnd = Pattern.compile("^*END OF (THIS|THE) PROJECT GUTENBERG EBOOK*");

			boolean findStart = false;

			while (true) {
				String line = br.readLine();
				if (line == null)
					break;

				Matcher mTitle = pTitle.matcher(line);
				if (mTitle.find()) {
					bookMap.put(Constants.FIELD_TITLE, mTitle.group(0).split(": ")[1]);
				}

				Matcher mAuthor = pAuthor.matcher(line);
				if (mAuthor.find()) {
					bookMap.put(Constants.FIELD_AUTHOR, mAuthor.group(0).split(": ")[1]);
				}

				Matcher mStart = pStart.matcher(line);
				if (mStart.find()) {
					findStart = true;
					continue;
				}

				Matcher mEnd = pEnd.matcher(line);
				if (mEnd.find()) {
					break;
				}

				if (findStart && line.length() > 0) {
					bookBuffer.append(line);
					bookBuffer.append("\n");
				}
			}

			bookMap.put(Constants.FIELD_CONTENT, bookBuffer.toString());

		} catch (IOException e) {
			Log.error("File not found " + f.getName());
		}


		return bookMap;
	}

	public static boolean createIndex() {
		RestHighLevelClient client = ESClientManager.get();
		CreateIndexRequest request = new CreateIndexRequest(Constants.INDEX_NAME);
		request.settings(Settings.builder()
				.put("index.number_of_shards", 1)
				.put("index.number_of_replicas", 1)
		);
		try {
			CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
			return createIndexResponse.isAcknowledged();
		} catch (IOException e) {
			Log.error("Failed to create index, " + e.getMessage());
		}

		return false;
	}

	public static void deleteIndex() {
		RestHighLevelClient client = ESClientManager.get();
		DeleteIndexRequest request = new DeleteIndexRequest(Constants.INDEX_NAME);
		try {
			client.indices().delete(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			Log.error("Failed to delete index, " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		IndexProcessor.indexAll();
	}
}
