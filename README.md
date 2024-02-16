> **GETTING STARTED:** You must start from some combination of the CSV Sprint code that you and your partner ended up with. Please move your code directly into this repository so that the `pom.xml`, `/src` folder, etc, are all at this base directory.

> **IMPORTANT NOTE**: In order to run the server, run `mvn package` in your terminal then `./run` (using Git Bash for Windows users). This will be the same as the first Sprint. Take notice when transferring this run sprint to your Sprint 2 implementation that the path of your Server class matches the path specified in the run script. Currently, it is set to execute Server at `edu/brown/cs/student/main/server/Server`. Running through terminal will save a lot of computer resources (IntelliJ is pretty intensive!) in future sprints.

# Project Details
Project name: Server 

Team members and contributions (include cs logins): Mason Lee (jlee704), Simeon Dong (sdong22)

Include the total estimated time it took to complete project: 25 hours

A link to your repo: https://github.com/cs0320-s24/server-sdong-jlee.git

# Design Choices
Explain the relationships between classes/interfaces:

For each api endpoint (load, view, search, broadband) we set up separate handler classes that implements Route. For 
the ACS specific aspect of broadband, i.e making requests from the ACS api and sending that response to the broadband handler, 
we created an interface called ACSDatasource that has two methods with the more important one being getPercentageBBAccess(). This way,
we could create a mocked ACS api class as well as a real ACS api class that implement the ACSDatasource to make the requests to the ACS api to get the response, extract
the information we want, and send it to the user calling our API. By including both a mocked and real data source, we were
able to test our methods without always calling the ACS API. Furthermore, we created a proxy class implementing the ACSDatasource
that takes in a specific source (mocked or real) and utilizes the getPercentageBBAccess from the specific source to implement caching with Guava.
With the proxy class, developers have a choice of using caching as well as how they want the cache configured. The Broadband 
handler utilizes the datasource that is passed in to it to get the percentageBBaccess from whichever datasource we want 
(real, mocked, with/without cache).

Discuss any specific data structures you used, why you created it, and other high level explanations:

For caching, as mentioned in the paragraph above, we utilize Guava to check the cache for our return value for a given state/county and if it is
not in the cache, we can use from whatever datasource we're using the method to get percentageBBaccess. Part of the getting percentageBBaccess in our datasources (real or mocked) is also updating the
date/time that the ACS api is called so that when using caching, we can get the date/time that our datasource used in the cache to ensure the user
knows when the ACS api is called and not everytime that we give the user a response. 

To store the state codes from the ACS api, we took the List<List<String>> that we returned with moshi and stored each state to
state code in a hashmap in order to have constant lookup time after we populate the map with states to state codes. 

Furthermore, to ensure that load/search/view all operate on the same CSV file, we created a CSVState class which acts as
a shared state between the three handler classes. Whenever fields are updated from one handler, it can be seen in the others.

Runtime/ space optimizations you made (if applicable).

# Errors/Bugs

# Tests
Explain the testing suites that you implemented for your program and how each test ensures that a part of the program works:

For each handler, we created individual testing packages for each handler (e.g. loadhandler, broadband handler, etc). Each of these
testing classes test the specific features of each handler and ensures we get proper responses upon success and the right error code for
each error. We also have an additional testing class for the cache that tests the caching capabilities as well as certain criteria such
as hits/misses, consistency, accuracy, and that the cache responds well to different features like max size and eviction policies. Lastly,
we have an integrated test class that tests how the different handlers work together (load, view, search) to ensure that we can use
the endpoints together or return the right error when appropriate. Some examples are loading in different files, viewing the file, then searching the file, or ensuring
that the user can only view/search the most recently loaded csv. 


# How to
Run the tests you wrote/were provided:

To run tests enter "mvn test" in terminal

Build and run your program

To build and run program enter "mvn package" in terminal the "./run" to run the server. For configuring the cache,
the user developer can either use or not use the ACSproxy class depending on whether they want to use the cache or not. If they
are using the cache, the user developer can adjust the parameters passed into the ACSproxy class to ensure the cache is 
working to their desire.