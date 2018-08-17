package com.fractallabs.assignment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.TreeMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fractallabs.assignment.TwitterScanner.TSValue;

class TwitterScannerTest {
	
	  private TSValue ts;

	  @BeforeEach
	  void setUp() {
		  TreeMap <Instant, Double> testMap = new TreeMap<>();
		  LocalDateTime dateTime = LocalDateTime.of(2018, Month.AUGUST, 14, 14, 40);
		  Instant instant = dateTime.atZone(ZoneId.of("Europe/Paris")).toInstant();
		  ts = new TSValue(instant, 100);
	  }

	  @AfterEach
	  void tearDown() {
	    ts = null;
	  }

	  @Test
	  @DisplayName("Test deposit adds to balance.")
	  void test() {
	    account.deposit(50);
	    assertEquals(150, account.getBalance());
	  }

}