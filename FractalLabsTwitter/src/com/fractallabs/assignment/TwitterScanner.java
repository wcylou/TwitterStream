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

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/*+-----------------------------------------------------------------------------------------------
||
||  Class [TwitterScanner] 
||
||         Author:  [Wilson Lou]
||
||        Purpose:  [Find out how the number of mentions of a company/companies on Twitter 
||			change over time.]
||
||  Inherits From:  [None]
||
||     Interfaces:  [None]
||
|+-------------------------------------------------------------------------------------------------
||
||      Constants:  [TreeMap <Instant, Double> storeValues: holds timestamp and absolute count,
||			TreeMap <Instant, Double> changeValues: holds timestamp and percentage change,
||			String [] searchCompanies: filter tweets by companies,
||			int taskScheduleMilliseconds: how often the count is reset,
||			int startHour: 24 hour time - hour the application will run from,
||			int endHour: 24 hour time - hour the application will stop running,
||			int currentHour: 24 hour time - current system time hour,
||			String oAuthConsumerKey: Twitter OAuth token details,
||			String oAuthConsumerSecret: Twitter OAuth token details,
||			String oAuthAccessToken: Twitter OAuth token details,
||			String oAuthAccessTokenSecret: Twitter OAuth token details]
||
|+-------------------------------------------------------------------------------------------------
||
||   Constructors:  [TwitterScanner(String [] companyNames)]
||
||  Class Methods:  [main(String... args),
||			run(),
|| 			startTimer(),
|| 			storeValue(TSValue value),
|| 			Double calculatePercentage(Instant lastKey, Double lastValue),
|| 			writeMap()]		
||
++-------------------------------------------------------------------------------------------------*/

public class TwitterScanner {
	
	/*+-----------------------------------------------------------------------------
	||
	||  Class [TSValue] 
	||
	||         Author:  [Wilson Lou]
	||
	||        Purpose:  [TSValue Entity]
	||
	||  Inherits From:  [None]
	||
	||     Interfaces:  [None]
	||
	|+------------------------------------------------------------------------------
	||
	||      Constants:  [Instant timestamp: filter tweets by companies,
	||			double val: count of tweets for set time period]
	||
	|+------------------------------------------------------------------------------
	||
	||   Constructors:  [TSValue(Instant timestamp, double val)]
	||
	||  Class Methods:  [Instant getTimestamp(),
	|| 			double getVal()]
	||
	++------------------------------------------------------------------------------*/

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
	protected TreeMap <Instant, Double> storeValues = new TreeMap<Instant, Double>();
	protected TreeMap <Instant, Double> changeValues = new TreeMap<Instant, Double>();
	
	protected static int count = 0;
	protected static String [] searchCompanies = {"Facebook", "Microsoft", "Google"};
	private static int taskScheduleMilliseconds = 1000*60*60;
	private static int startHour = 10;
	private static int endHour = 22;
	private static int currentHour = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).getHour();
	private static String oAuthConsumerKey = "";
	private static String oAuthConsumerSecret = "";
	private static String oAuthAccessToken = "";
	private static String oAuthAccessTokenSecret = "";
	

	public TwitterScanner(String [] companyNames) {
		this.companyNames = companyNames;
	}
	
	/**
	 * Main method instantiates TwitterScanner using the 'searchCompanies' field.
	 * The run() and startTimer() methods are called.
	 * 
	 * @return      void
	 * 
	 */
	
	public static void main(String... args) {
		TwitterScanner scanner = new TwitterScanner(searchCompanies);
		scanner.run();
		scanner.startTimer();
	}
	
	/**
	 * Method uses ConfigurationBuilder to configure Twitter4j OAuth.
	 * A StatusListener is used as a listener for incoming tweets.
	 * The stream uses the "GET statuses/sample" API.
	 * The onStatus() method filters tweets by the static field 'searchCompanies'.
	 *
	 * @return      void
	 * 
	 */

	public void run() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setJSONStoreEnabled(true)
		  .setOAuthConsumerKey(oAuthConsumerKey)
		  .setOAuthConsumerSecret(oAuthConsumerSecret)
		  .setOAuthAccessToken(oAuthAccessToken)
		  .setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
	        
		StatusListener listener = new StatusListener() {
			public void onStatus(Status status) {
				for (String company : companyNames) {
	            	if (status.getText().contains(company)) {
	            		count++;
	            		System.out.println(company + " COUNT: " + count);
	            	}
				}
            }
		          
            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }
            
            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }
            
            @Override
            public void onException(Exception ex) {
            }
            
            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
            }
            
            @Override
            public void onStallWarning(StallWarning warning) {
            }   
		  };
	        
	        TwitterStreamFactory tf = new TwitterStreamFactory(cb.build());
	        TwitterStream twitterStream = tf.getInstance();
	        twitterStream.addListener(listener);
	        twitterStream.sample();
	}
	
	/**
	 * A schedule is implemented via a timer. 
	 * The application runs if the 'if' statement condition checking hours is satisfied.
	 * The number of times the company/list of companies has appeared in tweets is printed
	 * to the console.
	 * The storeValue() and writeMap() methods are called, and count is reset to 0.
	 * 
	 * @return      void
	 * 
	 */

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
		}, taskScheduleMilliseconds, taskScheduleMilliseconds);
	}
	
	/**
	 * If the TreeMap holding the absolute count is empty, the first entry is stored.
	 * Else, the last entry is retrieved and the percentage changed is calculated.
	 * The new absolute value and percentage change value are put into their respective TreeMaps.
	 * 
	 * @param  		value  count
	 * 
	 * @return      void
	 * 
	 */
	
	protected void storeValue(TSValue value) {
		if (storeValues.size() == 0) {
			storeValues.put(value.getTimestamp(), value.getVal());
		} 
		else {
			Instant lastKey = storeValues.lastKey();
			double lastValue = storeValues.get(lastKey);
			double percentageChange = calculatePercentage(lastKey, lastValue);
			storeValues.put(value.getTimestamp(), value.getVal());
			changeValues.put(value.getTimestamp(), percentageChange);
		}
	}
	
	/**
	 * The count is compared to the last entry into the TreeMap and a positive, no change 
	 * or negative percentage, formatted to 2 d.p. change is printed to the console.
	 * 
	 * @param  		lastKey  	last timestamp entry into TreeMap
	 * @param  		lastValue  	count associated witht the last timestamp entry
	 * 
	 * @return      double		percentage change
	 * 
	 */

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
	
	/**
	 * Appends the company search terms, timestamp, and count of tweets to 'changeOnHour.txt'
	 * in the root directory.
	 * 
	 * @return      void	
	 * 
	 */
	
	public void writeMap() {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			String content = "Company: " + Arrays.toString(companyNames) + ", Time: " + storeValues.lastKey().toString() + ", Change: " + changeValues.get(storeValues.lastKey());
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