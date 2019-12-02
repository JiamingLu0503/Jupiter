package rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Login() {
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
		DBConnection dbConnection = DBConnectionFactory.getConnection();
		
		try {
			HttpSession session = request.getSession(false);
			JSONObject resObj = new JSONObject();
			if (session != null) {
				String userId = session.getAttribute("user_id").toString();
				resObj.put("status", "Welcome Back").put("user_id", userId).put("name", dbConnection.getFullname(userId));	
			} else {
				response.setStatus(403);
				resObj.put("status", "Invalid Session");
			}

			RpcHelper.writeJsonObject(response, resObj);
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
		DBConnection dbConnection = DBConnectionFactory.getConnection();

		JSONObject resObj = new JSONObject();
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			if(dbConnection.verifyLogin(userId, password)) {
				HttpSession session = request.getSession();
				session.setAttribute("user_id", userId);
				session.setMaxInactiveInterval(600);
				resObj.put("status", "Login Successfully").put("user_id", userId).put("name", dbConnection.getFullname(userId));
			} else {
				response.setStatus(401);
				resObj.put("status", "Invalid Username or Password");
			}
			
			RpcHelper.writeJsonObject(response,  resObj);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			dbConnection.close();
		}
	}

}
