# YelpDatasetChallenge
Z534 Search Project on Yelp Dataset

##Contributors: 
Milind Gokhale | 
Siddharth Jayasankar | 
Sameedha Bairagi | 
Namrata Jagasia | 
Deepak Bharanikana | 

##Task -1: Predicting the category for each business
Yelp has provided around 61K business and each business has one or more categories associated with it. In this task we try to predict the categories of each business from the user reviews and tips provided for these businesses.

####Approach:
We approached this task using Information Retrieval technique. We used the reviews and tips of the training data set for query expansion and used these queries to predict the categories for the businesses in the test dataset. We experimented with a few parameters to compare our results. They are
1)	Considered different ranking algorithms like VSM, BM25, LMD, LMJ
2)	Varied the number of  predicted categories N (3 , 5, 10, 20 categories)
3)	We applied POS tagging to form the query strings.

####Key Classes: 
1.	LoadDataToMongo.java: This code extracts data from the file and inserts into a collection in mongodb.
2.	CategoryFeatures.java: This class primarily extracts the features for every category and ranks them by TFIDF score. Then top 100 words for each category. Then it writes category features map to the list.ser file in a serialized manner.
3.	PrepareTrainingTestData.java: This code extracts data from the individual reviewGroup and tipGroup collections and creates a training and test set containing businessID, yelp assigned categories, all reviews and all tips.
4.	IndexData.java: This class primarily extracts data from the training and test sets and prepares index for each dataset. The index prepared is a lucene index. Lucene index fields used in the final approach - [BUSINESSID, CATEGORIES, REVIEWSTIPS].
5.	AssignCategories.java: This class primarily prepares a mongodb collection containing all the businesses and their assigned categories along with the scores for each category. 
6.	Evaluation.java: This class primarily gets the definite number of top category assignments for each of the businesses and then evaluates the precision and recall for the assignment.
7.	UtilFunctions.java: This code contains utility functions which are used in the project in various other classes.


##Task -2: Predict most discussed attributes in each city
In this task, given a city name we tried to predict the list of attributes that are most talked  about / attributes which people of the city give more weightage to in that particular city. This will help businessmen / entrepreneurs who are planning to establish a restaurant or improve their existing restaurant business by focusing more on the thing which matter most to the local people. We aim to find these most talked about attributes from the reviews and tips provided by yelp.

####Approach:
We considered this task to be an information retrieval task and hence decided to use IR approach. For each attributes we created a bag of words query string and ran a search for these attributes for all the cities considered by us.

####Key Classes:
CityIndexer.java:- Used for indexing City information
Constants.java:- Used for storing constant values in project like directories, feature mapping etc.
DBManager.java:- Used for accessing mongo Database, creating & retrieving data
TopAttributesFinder.java:- Used as main java class that will generate most talked attributes for a business in a given location	

