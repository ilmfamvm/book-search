import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;


public class SearchProcessor {

	//TODO stream
	static List<Map<String, Object>> query(String query) {
		List<Map<String, Object>> result = new LinkedList<>();
		RestHighLevelClient client = ESClientManager.get();

		query = query.replaceAll("\\+", " ");

		SearchRequest searchRequest = new SearchRequest(Constants.INDEX_NAME);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		// add highlighter
		HighlightBuilder highlightBuilder = new HighlightBuilder();
		HighlightBuilder.Field highlightContent = new HighlightBuilder.Field(Constants.FIELD_CONTENT);
		highlightContent.highlighterType("unified");
		highlightBuilder.field(highlightContent);
		sourceBuilder.highlighter(highlightBuilder);

		// query
		sourceBuilder.query(QueryBuilders.multiMatchQuery(query, Constants.FIELD_TITLE, Constants.FIELD_AUTHOR, Constants.FIELD_CONTENT, Constants.FIELD_FILE_NAME));
		searchRequest.source(sourceBuilder);

		try {
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			SearchHit[] searchHits = searchResponse.getHits().getHits();

			CacheManager.flush();
			for (SearchHit searchHit : searchHits) {
				Map<String, Object> book = searchHit.getSourceAsMap();
				book.put(Constants.FIELD_HIGHLIGHTER, getHighlighterContent(searchHit.getHighlightFields()));
				result.add(book);
				CacheManager.put((String) book.get(Constants.FIELD_FILE_NAME), book);
				Log.info("Query: " + query + ", book: " + book.get(Constants.FIELD_TITLE) + ", score: " + searchHit.getScore());
			}
		} catch (IOException e) {
			Log.error("Failed to search " + query + ", " + e.getMessage());
		}

		return result;
	}

	private static String getHighlighterContent(Map<String, HighlightField> map) {
		if (map == null || map.size() == 0)
			return "";

		Text[] texts = map.get(Constants.FIELD_CONTENT).getFragments();
		StringBuilder builder = new StringBuilder();
		for (Text t : texts) {
			builder.append(t.string().replaceAll("<em>", "<b>").replaceAll("</em>", "</b>"));
			builder.append("<br/>");
		}

		return builder.toString();
	}

	static String getBook(String fileName) {
		Object book = CacheManager.get(fileName);
		if (book == null)
			return null;

		Map<String, Object> map = (Map<String, Object>) book;
		StringBuilder result = new StringBuilder();
		result.append("Title: ");
		result.append(map.get(Constants.FIELD_TITLE));
		result.append("\n\n");
		result.append("Author: ");
		result.append(map.get(Constants.FIELD_AUTHOR));
		result.append("\n\n");
		result.append(map.get(Constants.FIELD_CONTENT));

		return result.toString();
	}

	public static void main(String[] args) {
		List<Map<String, Object>> result = SearchProcessor.query("bible");
		Log.info((String) result.get(0).get(Constants.FIELD_CONTENT));
	}
}
