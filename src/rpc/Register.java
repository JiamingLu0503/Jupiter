package rpc;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Register
 */
@WebServlet("/register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Register() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		DBConnection dbConnection = DBConnectionFactory.getConnection();

		JSONObject resObj = new JSONObject();
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			String firstName = input.getString("first_name");
			String lastName = input.getString("last_name");
			if(dbConnection.registerUser(userId, password, firstName, lastName)) {
				resObj.put("status", "Register Successfully");
			} else {
				resObj.put("status", "Registration Failed");
			}
			
			RpcHelper.writeJsonObject(response,  resObj);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			dbConnection.close();
		}
	}

}