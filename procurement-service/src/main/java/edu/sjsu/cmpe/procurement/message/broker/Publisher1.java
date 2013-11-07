package edu.sjsu.cmpe.procurement.message.broker;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Publisher1 {
	
	 public static void main(String[] args) {
		 
		 try {
			 
				Client client = Client.create();
		 
				WebResource webResource = client
				   .resource("http://54.215.210.214:9000/orders/69858");
		 
				ClientResponse response = webResource.accept("application/json")
		                   .get(ClientResponse.class);
		 
				if (response.getStatus() != 200) {
				   throw new RuntimeException("Failed : HTTP error code : "
					+ response.getStatus());
				}
		 
				String output = response.getEntity(String.class);
		 
				System.out.println("Output from Server .... \n");
				System.out.println(output);
				JSONObject json = (JSONObject) JSONSerializer.toJSON(output);  
		//		 JSONObject json1 = json.getJSONObject("shipped_books");
				 JSONArray shipped_books = json.getJSONArray("shipped_books");
				 System.out.println(shipped_books);
				 String[][] books = new String[shipped_books.size()][2];
				 for(int i=0; i<shipped_books.size();i++)
				 {
					 String book1 = shipped_books.getString(i);
					 JSONObject json1 = (JSONObject) JSONSerializer.toJSON(book1);  
					 int isbn = json1.getInt("isbn");
					 String title = json1.getString("title");
					 String category = json1.getString("category");
					 String coverImage = json1.getString("coverimage");
					 books[i][0]=isbn+":"+title+":"+category+":"+coverImage;
					 books[i][1]=category;
					 System.out.println(books[i][0]);
				 }
				 publishIt(books);
			  } catch (Exception e) {
		 
				e.printStackTrace();
		 
			  }		 
			}
	 
	 public static void publishIt(String[][] books) throws JMSException
	 {
		 	String user = env("APOLLO_USER", "admin");
			String password = env("APOLLO_PASSWORD", "password");
			String host = env("APOLLO_HOST", "54.215.210.214");
			int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
			String[] args = new String[]{};
			
			StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
			factory.setBrokerURI("tcp://" + host + ":" + port);

			Connection connection = factory.createConnection(user, password);
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			for(int i=0;i<books.length;i++)
			{
	
				String destination = arg(args,0,"/topic/69858.book."+books[i][1]);
				Destination dest = new StompJmsDestination(destination);
				MessageProducer producer = session.createProducer(dest);
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				TextMessage msg = session.createTextMessage(books[i][0]);
				msg.setLongProperty("id", System.currentTimeMillis());
				producer.send(msg);
			}
			
			connection.close();

		    }

		    private static String env(String key, String defaultValue) {
			String rc = System.getenv(key);
			if( rc== null ) {
			    return defaultValue;
			}
			return rc;
		    }

		    private static String arg(String []args, int index, String defaultValue) {
			if( index < args.length ) {
			    return args[index];
			} else {
			    return defaultValue;
			}
		   }

}
