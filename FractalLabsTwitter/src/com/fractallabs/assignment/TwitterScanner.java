package com.fractallabs.assignment;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterScanner {

	public static class TSValue {
		private final Instant timestamp;
		private final double val;

		public TSValue(Instant timestamp, double val) {
			this.timestamp = timestamp;
			this.val = val;
		}

		public Instant getTimestamp() {
			return timestamp;
		}

		public double getVal() {
			return val;
		}
	}
	
	protected int count = 0;
	protected TreeMap <Instant, Double> storeValues = new TreeMap<>();

	public TwitterScanner(String companyName) {
		 ConfigurationBuilder cb = new ConfigurationBuilder();
	        cb.setJSONStoreEnabled(true)
	        .setOAuthConsumerKey("QQ8vZHPiA5899QYcR1AGT7WR5")
	        .setOAuthConsumerSecret("Beh6mEMfRwLrfSdIlpBI1ee12Ia1HuzIMPYVNtFhynPmswFRWJ")
	        .setOAuthAccessToken("780035227-pr2FGpvNTAGEvRGlECosW1UK97DgVb403rd42RgH")
	        .setOAuthAccessTokenSecret("8Sogh8eCywCpUuFU0ke7dZMGiC7IT3FUm2sp7pJ3wOYC9");
	        
			 StatusListener listener = new StatusListener() {
		
		            public void onStatus(Status status) {
		            	if (status.getText().contains(companyName)) {
		            		count++;
		            		System.out.println(companyName + " COUNT: " + count);
		            	}
		            }
		            
		            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
//		                System.out.println("Status deletion notice id:" + statusDeletionNotice.getStatusId());
		            }
		            
		            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		                System.out.println("Track limitation notice:" + numberOfLimitedStatuses);
		            }
		            
		            public void onException(Exception ex) {
//		            	ex.printStackTrace();
		            }
		            
		            @Override
		            public void onScrubGeo(long userId, long upToStatusId) {
		                System.out.println("Scrub_geo. User id:" + userId + " up to status id:" + upToStatusId);
		            }
		            
		            @Override
		            public void onStallWarning(StallWarning warning) {
		                System.out.println("Stall stall warning:" + warning);
		            }            
		        };
	        
	        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
	        TwitterStream twitterStream = tf.getInstance();
	        twitterStream.addListener(listener);
	        twitterStream.sample();

	}

	public void run() {
		// Begin aggregating mentions. Every hour, "store" the relative change
		// (e.g. write it to System.out).
		Instant lastKey = storeValues.lastKey();
		double lastValue = storeValues.get(lastKey);
		System.out.println("Last entry: " + lastValue);
		calculatePercentage(lastValue);
	}

	public Double calculatePercentage(Double lastValue) {
		DecimalFormat df = new DecimalFormat("#.00"); 
		double change;
		if (count/lastValue > 1) {
			System.out.println("Over last hour, increase of " + (df.format((count - lastValue)/lastValue * 100)) + "%");
			change = (count - lastValue)/lastValue * 100;
		}
		else if (count/lastValue == 1) {
			System.out.println("No change over the past hour");
			change = 0;
		}
		else  {
			System.out.println("Over last hour, decrease of " + (df.format((lastValue - count)/lastValue  * 100)) + "%");
			change = (lastValue - count)/lastValue  * 100;
		}
		return change;
	}

	protected void storeValue(TSValue value) {
		if (storeValues.size() == 0) {
			storeValues.put(value.getTimestamp(), value.getVal());
		}
		else {
			System.out.println("TreeMap Size is " + storeValues.size());
			run();
			storeValues.put(value.getTimestamp(), value.getVal());
		}
	}
	
	public static void main(String... args) {
		TwitterScanner scanner = new TwitterScanner("Facebook");
		scanner.startTimer();
	}
	
	public void startTimer() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask () {
		    @Override
		    public void run () {
		    	Instant instant = Instant.now();
		    	TSValue ts = new TSValue(instant, count);
			    storeValue(ts);
		    	System.out.println(count);
		    	count = 0;
		    }
		}, 30000, 1000*30);
		//* 6 each for hour
}
	
}