package edu.sjsu.cmpe.procurement.message.broker;

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

public class Listener2 {

	 public static void main(String []args) throws JMSException {

			String user = env("APOLLO_USER", "admin");
			String password = env("APOLLO_PASSWORD", "password");
			String host = env("APOLLO_HOST", "54.215.210.214");
			int port = Integer.parseInt(env("APOLLO_PORT", "61613"));
			String destination = arg(args, 0, "/topic/69858.book.computer");

			StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
			factory.setBrokerURI("tcp://" + host + ":" + port);

			Connection connection = factory.createConnection(user, password);
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination dest = new StompJmsDestination(destination);

			MessageConsumer consumer = session.createConsumer(dest);
			System.currentTimeMillis();
			System.out.println("Waiting for messages...");
			while(true) {
			    Message msg = consumer.receive();
			    if( msg instanceof  TextMessage ) {
				String body = ((TextMessage) msg).getText();
				if( "SHUTDOWN".equals(body)) {
				    break;
				}
				System.out.println("Received message = " + body);

			    } else if (msg instanceof StompJmsMessage) {
				StompJmsMessage smsg = ((StompJmsMessage) msg);
				String body = smsg.getFrame().contentAsString();
				if ("SHUTDOWN".equals(body)) {
				    break;
				}
				System.out.println("Received message = " + body);

			    } else {
				System.out.println("Unexpected message type: "+msg.getClass());
			    }
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
