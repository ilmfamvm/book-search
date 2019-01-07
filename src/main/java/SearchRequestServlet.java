import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/", "/search", "/book"})
public class SearchRequestServlet extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String url = request.getRequestURL().toString();
		if (url.indexOf("search") > 0) {
			response.setContentType("text/html; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			String html = getTopSearchBarHTML();
			String query = request.getParameter("q");
			List<Map<String, Object>> result = SearchProcessor.query(query);
			if (result.size() == 0) {
				html += "<div style=\"margin-left: 2%;margin-top: 2%;font-size: x-large;\">No matching books!</div>";
				response.getWriter().print(html);
				return;
			}
			html += "<div style=\"margin-left: 2%;margin-top: 2%;font-size: x-large;\"><ul>";
			for (Map<String, Object> book : result) {
				String info = "<a href=\"\\book?filename=" + book.get(Constants.FIELD_FILE_NAME) + "\" target=\"_blank\">" + book.get(Constants.FIELD_TITLE) + "</a>";
				html += "<li>" + info + "</li>";
				html += "<div style=\"margin-bottom: 1%;font-size: 22px;line-height: 31px;color: gray;\">";
				html += book.get(Constants.FIELD_HIGHLIGHTER);
				html += "</div>";
			}
			html += "</ul></div>";
			response.getWriter().print(html);
		} else if (url.indexOf("book") > 0) {
			response.setContentType("charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			String fileName = request.getParameter("filename");
			String book = SearchProcessor.getBook(fileName);
			if (book == null) {
				response.getWriter().print("book does not exist");
				return;
			}

			OutputStream outputStream = response.getOutputStream();
			outputStream.write(book.getBytes(Charset.forName("UTF-8")));
			outputStream.flush();
		} else {
			response.getWriter().print("Sorry, no page found");
		}
	}

	private String getTopSearchBarHTML() {
		return "<form action=\"/search\" method=\"get\">" +
				"    <div style=\"width: 100%;\">" +
				"        <span style=\"font-size: 33px;color: cadetblue;margin-left: 2%;\">Book Search</span>" +
				"        <input type=\"text\" id=\"q\" name=\"q\"" +
				"               style=\"width: 25%;margin-left: 1%;margin-top: 3%;font-size: x-large;border-width: medium;height: 50px;\"" +
				"               placeholder=\"search\"/>" +
				"    </div>" +
				"</form>";
	}
}
