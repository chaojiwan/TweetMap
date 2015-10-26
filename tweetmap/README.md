# tweetmap
Project url: http://tomcat7-zgrgmhpa2j.elasticbeanstalk.com/

1.jpg: select "sport" category and get data from server and render the google map.
2.jpg: use AlchemyAPI to analyze the newly posted tweets to get lots of keywords shown as a trend.
3.jpg: use websocket to build a real-time service. Use tweetmap stream to get data and a websocket to maintain a long-alive connection and get data from server and show it both in text and map.


1. The project is a java maven web project using tomcat and servlet.

2. The src/main/java is the source code dir.
	db/Rds.java is a database access module
	db/Tweet.java defines a data structure for tweets.
	eb/ElasticBean.java automatically deploy the application, create a elasticbean, an environment for it and uploads a war file to S3 and uses it to update the application.
	server/TweetStream.java is a server using tweet streaming api to get data
	servlet/RdsServlet.java maps to url /getdata to get data from different categories
	servlet/PushServlet.java is a websocket server to push data from TweetStream to the clients.
	servlet/TrendServlet.java maps to url /showtrend to get trend keywords

	webapp/WEB-INF/web.xml defines the configuration
	webapp/index.jsp is the index rendering jsp file.

3. Because the privacy, I erases all the key password data in all source codes.