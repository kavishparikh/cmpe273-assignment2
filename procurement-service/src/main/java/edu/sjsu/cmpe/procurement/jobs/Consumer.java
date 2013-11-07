package edu.sjsu.cmpe.procurement.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;
import org.fusesource.stomp.jms.message.StompJmsMessage;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


class Consumer {

 //   @SuppressWarnings("static-access")
	public static void bookOrder() throws JMSException, InterruptedException {
	String user = env("APOLLO_USER", "admin");
	String password = env("APOLLO_PASSWORD", "password");
	String host = env("APOLLO_HOST", "54.215.210.214");
	int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
	String queue = "/queue/69858.book.orders";
	String args[] = new  String[]{};
	String destination = arg(args, 0, queue);

	long waitUntil = 5000; // wait for 5 sec
	
	List<String> orderList = new ArrayList<String>();
	
	StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
	factory.setBrokerURI("tcp://" + host + ":" + port);

	Connection connection = factory.createConnection(user, password);
	connection.start();
	Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	Destination dest = new StompJmsDestination(destination);

	MessageConsumer consumer = session.createConsumer(dest);
	System.out.println("Waiting for messages from " + queue + "...");
	while(true) {
	    Message msg = consumer.receive(waitUntil);
	    if( msg instanceof  TextMessage ) {
		String body = ((TextMessage) msg).getText();
		System.out.println("Received message = " + body);
		orderList.add(body);
	    } 
	    
	    else if (msg instanceof StompJmsMessage) {
		StompJmsMessage smsg = ((StompJmsMessage) msg);
		String body = smsg.getFrame().contentAsString();
		System.out.println("Received message = " + body);
		orderList.add(body);
	    } 
	    
	    
	    else if(msg == null)
	    {
    		  System.out.println("No new messages. Existing due to timeout - " + waitUntil / 1000 + " sec");
    		  break;
	    }
	    
	    else {
		System.out.println("Unexpected message type: "+msg.getClass());
	    }
	}
	System.out.println("connection has been closed");
	connection.close();
	int[] isbnArray = new int[orderList.size()];
	int i = 0;
	for(String input : orderList)
	{
		System.out.println(input);
		if(input.contains(":"))
		{
			String[] parts = input.split("\\:");
			String part2 = parts[1];
			int isbn = Integer.parseInt(part2);
			System.out.println(isbn);
			isbnArray[i++] = isbn;
		}
		else
		{
			throw new IllegalArgumentException("");
		}
	}
	
	System.out.println("The array is: " +Arrays.toString(isbnArray));
	if(isbnArray.length != 0)
	{
		doPost(isbnArray);
	}
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
    
    
  // Sending HTTP POST request using jersey client
    public static void doPost(int[] isbnArray)
    {
    	try {
            Client client = Client.create();
            String baseuri = "http://54.215.210.214:9000/orders";
            WebResource webResource = client.resource(baseuri);

//            MultivaluedMap formData = new MultivaluedMapImpl();
//            formData.add("id", "69858");
//            formData.add("order_book_isbns", isbnArray);
            String input = "{\"id\": \"69858\", "
                    + "\"order_book_isbns\": "+Arrays.toString(isbnArray)+"}";

            // POST method
            ClientResponse response = webResource.accept("application/json")
                    .type("application/json").post(ClientResponse.class, input);

            // check response status code
            if (response.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }

            // display response
            String output = response.getEntity(String.class);
            System.out.println("Output from Publisher .... ");
            System.out.println(output + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
}