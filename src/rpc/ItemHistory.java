package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ItemHistory() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}

		DBConnection dbConnection = DBConnectionFactory.getConnection();
		String userId = request.getParameter("user_id");

		try {
			JSONArray favorites = new JSONArray();
			Set<Item> items = dbConnection.getFavoriteItems(userId);
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.append("favorite", true);
				favorites.put(obj);
			}
			RpcHelper.writeJsonArray(response, favorites);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}

		DBConnection dbConnection = DBConnectionFactory.getConnection();

		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			JSONArray favorites = input.getJSONArray("favorite");
			String userId = input.getString("user_id");
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < favorites.length(); i++) {
				itemIds.add(favorites.getString(i));
			}
			dbConnection.setFavoriteItems(userId, itemIds);
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}

		DBConnection dbConnection = DBConnectionFactory.getConnection();

		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			JSONArray favorites = input.getJSONArray("favorite");
			String userId = input.getString("user_id");
			List<String> itemIds = new ArrayList<>();
			for (int i = 0; i < favorites.length(); ++i) {
				itemIds.add(favorites.getString(i));
			}
			dbConnection.unsetFavoriteItems(userId, itemIds);
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			dbConnection.close();
		}
	}

}
