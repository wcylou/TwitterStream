package com.fractallabs.assignment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fractallabs.assignment.TwitterScanner.TSValue;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

@DisplayName("Twitter Streaming API Tests")
class TwitterScannerTest {

	private TSValue ts;

	private TwitterScanner twitScan;

	private ConfigurationBuilder builder;

	private Instant i1;
	private Instant i2;
	private Instant i3;

	private LocalDateTime dateTime;
	
	protected String [] searchCompanies = {"Facebook", "Microsoft", "Barclays"};

	@BeforeEach
	void setUp() {
		twitScan = new TwitterScanner(searchCompanies);
		twitScan.count = 50;
		twitScan.storeValues = new TreeMap<>();

		dateTime = LocalDateTime.of(2018, Month.AUGUST, 14, 14, 40);
		i1 = dateTime.atZone(ZoneId.of("Europe/Paris")).toInstant();
		i2 = i1.plus(1, ChronoUnit.HOURS);
		i3 = i2.plus(1, ChronoUnit.HOURS);

		ts = new TSValue(i1, 100);

		builder = new ConfigurationBuilder();
		builder.setApplicationOnlyAuthEnabled(true);

	}

	@AfterEach
	void tearDown() {
		ts = null;
		twitScan.storeValues.clear();
	}

	@Test
	@DisplayName("Testing Twitter4j Authentication")
	void testAuthWithBuildingConf1() throws Exception {
		// setup
		Twitter twitter = new TwitterFactory(builder.build()).getInstance();

		// exercise & verify
		twitter.setOAuthConsumer("QQ8vZHPiA5899QYcR1AGT7WR5", "Beh6mEMfRwLrfSdIlpBI1ee12Ia1HuzIMPYVNtFhynPmswFRWJ");
		OAuth2Token token = twitter.getOAuth2Token();
		assertEquals("bearer", token.getTokenType());

		try {
			twitter.getAccountSettings();
			fail("should throw TwitterException");
		} catch (TwitterException e) {
			assertEquals(403, e.getStatusCode());
			assertEquals(220, e.getErrorCode());
			assertEquals("Your credentials do not allow access to this resource", e.getErrorMessage());
		}
	}

	@Test
	@DisplayName("Test Percentage Change between Hours = 0")
	void testNoPercentageChange() {
		twitScan.storeValues.put(i1, 50.00);
		assertEquals(0, twitScan.calculatePercentage(i1, twitScan.storeValues.get(i1)), 0.1);
	}

	@Test
	@DisplayName("Test Percentage Change between Hours is Positive")
	void testPositivePercentageChange() {
		twitScan.storeValues.put(i2, 60.00);
		assertEquals(16.6, twitScan.calculatePercentage(i2, twitScan.storeValues.get(i2)), 0.1);
	}

	@Test
	@DisplayName("Test Percentage Change between Hours is Negative")
	void testNegativePercentageChange() {
		twitScan.storeValues.put(i3, 40.00);
		assertEquals(25, twitScan.calculatePercentage(i3, twitScan.storeValues.get(i3)), 0.1);
	}

	@Test
	@DisplayName("Test First Entry Into TreeMap")
	void testEmptyTreeMapStoreValueMethod() {
		twitScan.storeValue(ts);
		assertEquals(1, twitScan.storeValues.size());
	}

}
