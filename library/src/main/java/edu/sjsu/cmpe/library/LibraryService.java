package edu.sjsu.cmpe.library;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

import edu.sjsu.cmpe.library.api.resources.BookResource;
import edu.sjsu.cmpe.library.api.resources.RootResource;
import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;
import edu.sjsu.cmpe.library.ui.resources.HomeResource;
import edu.sjsu.cmpe.library.message.broker.Listener;

public class LibraryService extends Service<LibraryServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws Exception {
    	
    	new LibraryService().run(args);
    }

    @Override
    public void initialize(Bootstrap<LibraryServiceConfiguration> bootstrap) {
	bootstrap.setName("library-service");
	bootstrap.addBundle(new ViewBundle());
	bootstrap.addBundle(new AssetsBundle());
    }


    public String apolloUser = null;
    public String apolloPassword = null;
    public String apolloHost = null;
    public String apolloPort = null;
    public String queueName = null;
    public String libraryName = null;
    
    @Override
    public void run(LibraryServiceConfiguration configuration,
	    Environment environment) throws Exception {
    	BookRepositoryInterface bookRepository = new BookRepository();	
    	backgroundTask(configuration,bookRepository);
	// This is how you pull the configurations from library_x_config.yml
    String queueName = configuration.getStompQueueName();
	String topicName = configuration.getStompTopicName();
	log.debug("{} - Queue name is {}. Topic name is {}",
		configuration.getLibraryName(), queueName,
		topicName);
//	Producer producer = new Producer();
//	producer.setConfig(configuration);
	// TODO: Apollo STOMP Broker URL and login
	 apolloUser = configuration.getApolloUser();
	 apolloPassword = configuration.getApolloPassword();
	 apolloHost = configuration.getApolloHost();
	 apolloPort = configuration.getApolloPort();
	System.out.println(apolloUser
			+apolloPassword
			+apolloHost
			+apolloPort);

	/** Root API */
	environment.addResource(RootResource.class);
	/** Books APIs */
//	BookRepositoryInterface bookRepository = new BookRepository();
	environment.addResource(new BookResource(bookRepository,configuration));

	/** UI Resources */
	environment.addResource(new HomeResource(bookRepository));
    }
    
    public void backgroundTask(final LibraryServiceConfiguration configuration, final BookRepositoryInterface bookrepository)
    {
    	int numThreads = 1;
	    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
 
	    Runnable backgroundTask = new Runnable() {
 
    	    @Override
    	    public void run() {
    	    	try {
					Listener.pubSubListener(configuration,bookrepository);
				} catch (JMSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
    
    	};
 
    	System.out.println("About to submit the background task");
    	executor.execute(backgroundTask);
    	System.out.println("Submitted the background task");
    
    	executor.shutdown();
    	System.out.println("Finished the background task");
    }
}
