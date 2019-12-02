package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterClient;

public class MySQLConnection implements DBConnection {
	private Connection conn;
	   
	public MySQLConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if(conn != null) {
			try {
				conn.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if(conn == null) {
			System.err.println("DB connection err");
			return;
		}
				
		try {
			String sql = "INSERT IGNORE INTO histroy(user_id, item_id) VALUES(?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for(String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if(conn == null) {
			System.err.println("DB connection err");
			return;
		}
				
		try {
			String sql = "DELETE FROM histroy(user_id, item_id) where user_id=? and item_id=?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for(String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection err");
			return new HashSet<>();
		}
		
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		
		try {
			String sql = "SELECT * FROM items WHERE item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				ps.setString(1, itemId);
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					ItemBuilder builder = new ItemBuilder();
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setDistance(rs.getDouble("distance"));
					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItems;
	}
	
	/**
	 * A helper function to getFavoriteItems
	 */
	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection err");
			return new HashSet<>();
		}
		
		Set<String> favoriteItems = new HashSet<>();
		
		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();	
			while (rs.next()) {
				favoriteItems.add(rs.getString("item_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return favoriteItems;
	}
	
	/**
	 * A helper function to getFavoriteItems
	 */
	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			System.err.println("DB connection err");
			return new HashSet<>();
		}
		
		Set<String> categories = new HashSet<>();
		
		try {
			String sql = "SELECT category from categories WHERE item_id = ? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, itemId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String keyword, Integer radius) {
		TicketMasterClient client = new TicketMasterClient();
		List<Item> items = client.search(lat, lon, keyword, radius);
		for(Item item : items) {
			saveItem(item);
		}
		return items;
	}
	
	/**
	 * A helper function to searchItems
	 */
	@Override
	public void saveItem(Item item) {
		if(conn == null) {
			System.err.println("DB connection err");
			return;
		}
		
		try {
			String sql = "INSERT IGNORE INTO items(item_id, name, address, image_url, url, distance) VALUES(?,?,?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			ps.setString(2, item.getName());
			ps.setString(3, item.getAddress());
			ps.setString(4, item.getImageUrl());
			ps.setString(5, item.getUrl());
			ps.setDouble(6, item.getDistance());
			ps.execute();
			
			sql = "INSERT IGNORE INTO categories(item_id, category) VALUES(?,?)";
			ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			for(String category : item.getCategories()) {
				ps.setString(2, category);
				ps.execute();
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getFullname(String userId) {
		if(conn == null) {
			System.err.println("DB connection err");
			return null;
		}
		
		String fullName = null;

		try {
			String sql = "SELECT first_name, last_name from users WHERE user_id = ? ";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				fullName = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return fullName;				
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if(conn == null) {
			System.err.println("DB connection err");
			return false;
		}
		
		try {
			String sql = "SELECT * from users WHERE user_id = ? and password = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
			System.err.println("DB connection err");
			return false;
		}

		try {
			String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, password);
			ps.setString(3, firstname);
			ps.setString(4, lastname);
			return ps.executeUpdate() == 1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;	
	}


}
