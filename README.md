# Twitter Stream
A program to find out how the number of mentions of a company/companies on Twitter change over time.

### Technologies Used
- Java
- Twitter4j
- JUnit
- Gradle

### Overview
- The application uses the Twitter4j Java library for the Twitter API.
- When the program runs, a StatusListener is used as a listener for incoming tweets.
- The stream uses the ["GET statuses/sample" API](https://developer.twitter.com/en/docs/tweets/sample-realtime/overview/GET_statuse_sample.html) which returns a small random sample of all public statuses.
- The 'onStatus' method filters tweets based on an array of 'searchCompanies'
- If a tweet matches, the count is incremented by one.
- A schedule is implemented via a timer which can be set to only run during an interval of the day.
- The relative change is placed in a map and printed to the screen every hour.
- The data is appended to the 'changeOnHour.txt' file every hour.

### License
This program is licensed under the MIT license.

#### Copyright (c) 2018 Wilson Lou
