# Last 60 seconds transactions

This is an project to create API
1. To save single transaction
2. To return statistics data for last 60 seconds

## Getting Started
Maven is used as a build tool for project.
I used Intellij IDEA for local development.
You can import sources as a Maven project.
After importing please do "mvn clean package".
This is a simple spring-boot project main class of which is com.api.Application.
Simply create configuration to run Java application with this main class.
in application.yaml class I store database connection data and value of seconds we want to go back to accumulate data.
It is set 60 now as required by assignment, but I externalised it to be more flexible.
Please note that I do not use database when generating statistic data. For it I keep in memory Map.
Database is used to track single transactions. It can be useful for further development. Use cases can be
1. When we shut down application we loose all statistic in memory data. If we have transactions saved it is possible to recover.
2. If one day we decide that we want to have statistics for longer period, it is possible to create old data from database.

### Prerequisites
To build project you need to have maven(preferably recent version to be installed).

### Installing
Application is using embedded Undertow server.
After running "maven clean package", you can find TransactionService-1.0-SNAPSHOT.jar in target folder.
Simply run "java -jar {jarname}" command. It will start server in port 8080.
After it endpoints will be accessible under /statistics and /transactions paths.

### Some notes about design
Application is implemented using spring-boot framework.
It was chosen as autoconfigured easy to use solution for REST APIs implementation.
There are 2 packages with appropriate subpackages com.assignment.transaction and com.assignment.statistics.
Classes of first one are responsible for getting and saving single transaction information and classes of second one are responsible for updating and getting statistical data.
Due to this separation after some changes it will be possible to convert application to 2 microservices.
For it we can have some message queue solution, so after transaction saved in database event can be pushed to queue. Statistics application can subscribe to event and update statistical data.
For now this solution is to big to implement, so TransactionService will do direct call to Statistics Service.

### Some notes about /statistics endpoint
I keep Map where for every second statistical data for transactions happened in that second is accumulated as Map value. Key is a DateTime for that second.
As Implementation for Map I chose ConcurrentHashMap which will provide possibility for concurrent and threadsafe implementation.
Every time new transaction arrived, the datetime for it until seconds is extracted. If it is older than 60 we do nothing. Otherwise
in map appropriate entry with given LocalDateTime is found and value for that entry is updated, by calculated new statistical data based on the old statistics for that second and newly arrived transaction.
Entries in map keys of those are having older time than 60 seconds before current time will be deleted. This way we make sure that we have only 60 entries in Map.

When calculating total statistics for last seconds, we will iterate over Map(which has 60 entries) and accumulate statistical data for every second.
E.g for getting total count we will sum up counts for every second. We will do this way constant amount of steps.

## Running the tests
You can run them separately with "mvn test command".
I did not separate production and test in memory databases(my bad) so please shut down application before running tests.

## Built With

* [Spring-boot](https://projects.spring.io/spring-boot/)
* [Maven](https://maven.apache.org/) - Dependency Management


