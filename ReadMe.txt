Read Me:
========

Part A(Collecting data from the Amazon Website using Web Crawler)
=================================================================
Folder: \Sentiment Analysis on Product Reviews\AmazonCrawler
- The code has the comments required to make modifications to crawl an amazon webpage for a given product

  http://www.amazon.com/Mojang-Minecraft-Pocket-Edition/dp/B00992CF6W/ref=sr_1_1?s=mobile-apps&ie=UTF8&qid=1462571451&sr=1-1&keywords=minecraft

  for above url, the product id is B00992CF6W, simlarly extract all the product ids from the url and pass to this executive in code.

- The collected data can be found in "Results.txt" file. We used this as our training data, which we have given as input for preprocessing. 

- This part has been implemented in java on Eclipse IDE.


Part B(Preprosessing of the data) 
=================================
Folder: \Sentiment Analysis on Product Reviews\Project_part_a_pre-processing_ and_naiveBayes
- Files given as input:
    1) Results.txt - Contains all the raw unprocessed data collected from Amazon Website. This is the unstructed training data.
	2) bigtext.txt - Contains all the structured text which has over million words. This is given as an input to the spellchecker. 
	3) YahooMailReviews.txt, ViberReviews.txt and FacebookReviews.txt are the unstructed testing set files which are given to test our classifier.
	4) YahooMailNumbers.txt, ViberNumbers.txt, FaceBookNumbers.txt are the files which are class lables of the testing data. We used this to compare 
	    our results that have been predicted bu our classifer for the testing data provided. 
- Output Files:
   1) trainingset.csv - This is the structured training dataset file that we feed in to our classifer. 
   2) YahooMailReviewstest_structured.csv, FacebookReviewstest_structured.csv, ViberReviewstest_structured.txt are the structered test data which we have 
      given to our classifer to predict the classlabels. 
	  
- We have Copied these Output files are pasted them in the folder of our Evaluator(\Sentiment Analysis on Product Reviews\Project_part_b_classifier_evaluation)

Part C(Evaluation of the Classifer -- Which Classifier to pick?)
================================================================
Folder: C:\Sentiment Analysis on Product Reviews\Project_part_b_classifier_evaluation\classification
- This is Written in Java using Weka API
- InputFiles:
     1) trainingset.csv - This is the structured training set after the preprocessing. 
- Outputfiles:
     1) comp1.csv - This contains the results of average mean and the resubstitution errors for all the classifiers
	 2) A3PerformanceResults.csv - This contains all the Paired Student t-test results of all the classifiers. 
- Arguments to give to this Program: comp1.csv A3PerformanceResults.csv trainingset.csv
- The Main fucntion is in Driver.java file. 
- O/p: After the Evaluation, NaiveBayes and (Support Vector Machine)SMO(RBF Kernel - 1) has been selected as our classifiers. 

Part D (Testing the selected Classifier with test data(Structured))
=================================================================
We have done testing for NaiveBayes classifer in Python nltk and for SMO RBF-1 classifier in Weka(Java). 
Testing for SMO:
---------------
Folder: \Sentiment Analysis on Product Reviews\Project_part_c_Classifier_and_test\Choosen_Classifier
- TestFiles as Input: test_set_processed_facebook.csv, test_set_processed_viber.csv, test_set_procesed_yahoo.csv. Note: These files have been copied and 
                      pasted after the Part B(Preprocessing) is done in to this folder. 
- Other input Files : FaceBookNumbers.txt,ViberNumbers.txt, YahooMailNumbers.txt - these have the correct classlabels for their respective testing data. We 
                      used to them to test our accuracy of our classifier.
- The output for the classifiers along with the predicted class labels for each document is in testingset_output_facebookSMORBF-1.txt, 
   testingset_output_viberSMORBF-1.txt, testingset_output_yahooSMORBF-1.txt.
 
 Testing for NaiveBayes:
 -----------------------
 This is written in Python. We extented the code for Part B to train the Naivebayes classifier and have tested on facebook, viber, and yahoo data, 
 just like we have done for SMO using weka. 


 
    
