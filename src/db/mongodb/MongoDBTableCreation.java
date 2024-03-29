package db.mongodb;

import java.text.ParseException;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoDBTableCreation {
  // Run as Java application to create MongoDB collections with index.
  public static void main(String[] args) throws ParseException {

		// Step 1, connection to MongoDB
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);

		// Step 2, remove old collections.
		db.getCollection("users").drop();
		db.getCollection("items").drop();

		// Step 3, create new collections with index restriction.
		IndexOptions options = new IndexOptions().unique(true);
		db.getCollection("users").createIndex(new Document("user_id", 1), options);
		db.getCollection("items").createIndex(new Document("item_id", 1), options);

		mongoClient.close();
		System.out.println("Import is done successfully.");

  }
}
