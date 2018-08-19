package com.fractallabs.assignment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import twitter4j.*;
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
	
	private String [] companyNames;
	
	protected TreeMap <Instant, Double> storeValues = new TreeMap<>();
	protected TreeMap <Instant, Double> changeValues = new TreeMap<>();
	
	protected static int count = 0;
	protected static String [] searchCompanies = {"Facebook", "Microsoft", "Barclays"};
	protected static int taskScheduleMilliseconds = 1000*60*60;
	protected static int startHour = 10;
	protected static int endHour = 17;
	protected static int currentHour = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).getHour();

	public TwitterScanner(String [] companyNames) {
		this.companyNames = companyNames;
	}
	
	public static void main(String... args) {
		TwitterScanner scanner = new TwitterScanner(searchCompanies);
		scanner.run();
		scanner.startTimer();
	}

	public void run() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setJSONStoreEnabled(true)
		  .setOAuthConsumerKey("QQ8vZHPiA5899QYcR1AGT7WR5")
		  .setOAuthConsumerSecret("Beh6mEMfRwLrfSdIlpBI1ee12Ia1HuzIMPYVNtFhynPmswFRWJ")
		  .setOAuthAccessToken("780035227-pr2FGpvNTAGEvRGlECosW1UK97DgVb403rd42RgH")
		  .setOAuthAccessTokenSecret("8Sogh8eCywCpUuFU0ke7dZMGiC7IT3FUm2sp7pJ3wOYC9");
	        
		StatusListener listener = new StatusListener() {
			public void onStatus(Status status) {
				for (String company : companyNames) {
	            	if (status.getText().contains(company)) {
	            		count++;
	            		System.out.println(company + " COUNT: " + count);
	            	}
				}
            }
		            
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
//		    	System.out.println("Status deletion notice id:" + statusDeletionNotice.getStatusId());
            }
            
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            	System.out.println("Track limitation notice:" + numberOfLimitedStatuses);
            }
            
            public void onException(Exception ex) {
//		    	ex.printStackTrace();
            }
            
            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Scrub_geo. User id:" + userId + " up to status id:" + upToStatusId);
            }
            
            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Stall warning:" + warning);
            }   
		  };
	        
	        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
	        TwitterStream twitterStream = tf.getInstance();
	        twitterStream.addListener(listener);
	        twitterStream.sample();
	}
	
	//Timer to store values to be able to calculate change on past hour. 60 minute delay and occurs every hour
	public void startTimer() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask () {
		    @Override
		    public void run () {
		    	Instant instant = Instant.now();
		    	if (currentHour > startHour && currentHour < endHour) {
			    	TSValue ts = new TSValue(instant, count);
			    	System.out.println(Arrays.toString(companyNames) + " appeared in " + count + " tweets over the past hour");
				    storeValue(ts);
			    	count = 0;
					writeMap();
		    	}
		    }
//		}, 1000*60*60, 1000*60*60);
	}, taskScheduleMilliseconds, taskScheduleMilliseconds);
	}
	
	protected void storeValue(TSValue value) {
		if (storeValues.size() == 0) {
			storeValues.put(value.getTimestamp(), value.getVal());
		} else {
			Instant lastKey = storeValues.lastKey();
			double lastValue = storeValues.get(lastKey);
			System.out.println("Last entry: " + lastValue);
			double percentageChange = calculatePercentage(lastKey, lastValue);
			storeValues.put(value.getTimestamp(), value.getVal());
			changeValues.put(value.getTimestamp(), percentageChange);
		}
	}

	public Double calculatePercentage(Instant lastKey, Double lastValue) {
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
	
	public void writeMap() {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			String content = "Company: " + companyNames + ", Time: " + storeValues.lastKey().toString() + ", Change: " + changeValues.get(storeValues.lastKey());
			fw = new FileWriter("changeOnHour.txt", true);
			bw = new BufferedWriter(fw);
			bw.write(content);
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}