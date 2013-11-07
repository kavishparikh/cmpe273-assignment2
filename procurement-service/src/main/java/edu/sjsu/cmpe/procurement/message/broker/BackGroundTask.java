package edu.sjsu.cmpe.procurement.message.broker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackGroundTask {
	
	 public static void main(String[] args) {
	    	int numThreads = 1;
		    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
	 
		    Runnable backgroundTask = new Runnable() {
	 
	    	    @Override
	    	    public void run() {
	    		System.out.println("Hello World");
	    	    }
	    
	    	};
	 
	    	System.out.println("About to submit the background task");
	    	executor.execute(backgroundTask);
	    	System.out.println("Submitted the background task");
	    
	    	executor.shutdown();
	    	System.out.println("Finished the background task");
	    }

}
