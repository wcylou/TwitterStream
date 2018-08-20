# Twitter Streaming API
A program to find out how the number of mentions of a company/companies on Twitter change over time.

### Technologies Used
- Java
- [Twitter4j library](http://twitter4j.org/en/)
- Gradle
- JUnit
- Mockito

### How to Run
Enter Twitter OAuth access details and run as Java Application.

### Overview
- The application uses the Twitter4j Java library for the Twitter API.
- When the program runs, a StatusListener is used as a listener for incoming tweets.
- The stream uses the ["GET statuses/sample" API](https://developer.twitter.com/en/docs/tweets/sample-realtime/overview/GET_statuse_sample.html) which returns a small random sample of all public statuses.
- The 'onStatus' method filters tweets by the static field *'searchCompanies'*.
- If a tweet matches, the count is incremented by one.
- A schedule is implemented via a timer. Static fields; *'taskScheduleMilliseconds'*, *'startHour'* and *'endHour'* can be set to determine how often the count occurs and during which hours of the day the application runs.
- The relative change is appended to the *'changeOnHour.txt'* file and printed to the console every hour.

***

### License
This program is licensed under the MIT license.
Copyright (c) 2018 Wilson Lou
