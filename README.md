# logfile-processing-service

## Description 
Our custom-build server logs different events to a file named logfile.txt. Every event has 2 entries in the file
- one entry when the event was started and another when the event was finished. The entries in the file
have no specific order (a finish event could occur before a start event for a given id)
Every line in the file is a JSON object containing the following event data:
    1. id - the unique event identifier
    2. state - whether the event was started or finished (can have values "STARTED" or "FINISHED"
    3. timestamp - the timestamp of the event in milliseconds
Application Server logs also have the following additional attributes:
    4. type - type of log
    5. host - hostname
 
Take the path to logfile.txt as an input argument
    1. Parse the contents of logfile.txt
    2. Flag any long events that take longer than 4ms
    3. Write the found event details to file-based HSQLDB (http://hsqldb.org/) in the working folder
    4. The application should create a new table if necessary and store the following values:
    5. Event id
    6. Event duration
    7. Type and Host if applicable
    8. Alert (true if the event took longer than 4ms, otherwise false)

##### Technology stack
* Java  8
* Gradle
* Spring boot

## Build and execute project
```console
> gradlew clean build
> gradlew bootrun

Server starts on port `8034`
```

## Testing the application :
1. Start the application as >  gradlew bootrun
2. Hit the endpoint > http://localhost:8084/api/index.html
3. Select the log file placed which user want to process or select the log file preset in resources folder
4. Click submit to see the processing result.
5. Output will show log data with id and alert , alert is true if time taken by log is more then 4ms

### You will see the response: 
```
[{"id":"stcmba","alert":false},{"id":"stcmbb","alert":false},{"id":"stcmbc","alert":false},{"id":"stcmbd","alert":true},{"id":"stcmbe","alert":false},{"id":"stcmbf","alert":false},{"id":"stcmbg","alert":false},{"id":"stcmbh","alert":false},{"id":"stcmbi","alert":false},{"id":"stcmbj","alert":true}]
```
```
