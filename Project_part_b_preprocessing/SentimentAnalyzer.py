# -*- coding: utf-8 -*-
from nltk import *
import sys
from nltk.corpus import stopwords
from nltk.stem import WordNetLemmatizer
from nltk.probability import FreqDist
import random, string
from SpellCheck import correct
import csv

reload(sys)
sys.setdefaultencoding("utf-8")
stop = stopwords.words('english')


all_words_list_of_list=[]
train_sent = []
review_training_set = []
count_total_positive = 0
count_total_negative = 0
documentlist=[]
lemmatizer = WordNetLemmatizer()


#removes puntuations from the review_training_set
def remove_puncutation(sent):
    no_punct = ""
    for char in sent:
       if char not in string.punctuation:
        no_punct = no_punct + char
    sent = no_punct
    return sent

#gets  the words present from the tweet
def get_words_in_review_training_set(review_training_set):
    global all_words_list_of_list
    all_words = []
    for (words, sentiment) in review_training_set:
        if words != "":
          all_words += words
          all_words_list_of_list.append(words)
    print all_words
    return all_words

#get the features from the wordlist using frequncy distribution

def get_word_features(wordlist):
    wordlist = FreqDist(wordlist)
    word_features = [w for (w, c) in wordlist.most_common(1000)]
    return word_features

#extracting features from the document using bag of method
def extract_features(document):
    document_words = set(document)
    features = {}
    for word in word_features_set:
        features['contains(%s)' % word] = (word in document_words)
    return features

#stemming words
def stem_words(word):
   return lemmatizer.lemmatize(word)

#writing headers(features) and the samples to the csv file(Creating a structured format of data) which is in turn given as a
#input to the classifiers to train them and to evaluate their performance

def write_to_test(testing_set,filename):
    writer=csv.writer(open(filename,'wb'))
    headers = word_features
    headers.append("ClassLabel")
    writer.writerow(headers)
    for test_row in testing_set:
        list = []
        dict = test_row[0]
        classlable = "?"
        for key in dict:
          list.append(dict[key])
        list.append(classlable)
        writer.writerow(list)



def write_to_train(training_set):
    writer=csv.writer(open("trainingset.csv",'wb'))
    headers = word_features
    headers.append("ClassLabel")
    writer.writerow(headers)
    for train_row in training_set:
        list = []
        dict = train_row[0]
        classlable = train_row[1]
        for key in dict:
          list.append(dict[key])
        list.append(classlable)
        writer.writerow(list)

#2.splitting the sentences in to sentiment and twxt
def split_train_sents_to_tokens(sents):
 for i in range(len(sents)):
     sentiment = sents[i].split(",")[0]
     sentence = " ".join(sents[i].split(",")[1:])
     train_sent.append((sentence,sentiment))
 for (words, sentiment) in train_sent:
    tt = TweetTokenizer(words.decode("utf8","ignore"))
    tokens = tt.tokenize(words.decode('utf8','ignore'))
    words_filtered = [remove_puncutation(e.lower()) for e in tokens if remove_puncutation(e.lower().decode('utf-8','ignore')) not in stop  ]
    while ' ' in words_filtered:
     words_filtered.remove(' ')
    correct_words=[]
    for word in words_filtered:
        correct_words.append(stem_words(correct(word)))
    review_training_set.append((correct_words, sentiment))


def split_test_sents_to_tokens(sents):
 test_sent=[]
 test_set= []
 for i in range(len(sents)):
     sentiment = sents[i].split(",")[0]
     sentence = " ".join(sents[i].split(","))[1:]
     test_sent.append((sentence,sentiment))
 for (words, sentiment) in test_sent:
    tt = TweetTokenizer(words.decode("utf8","ignore"))
    tokens = tt.tokenize(words.decode('utf8','ignore'))
    words_filtered = [remove_puncutation(e.lower()) for e in tokens if remove_puncutation(e.lower().decode('utf-8','ignore')) not in stop  ]
    while ' ' in words_filtered:
     words_filtered.remove(' ')
    correct_words=[]
    for word in words_filtered:
        correct_words.append(stem_words(correct(word)))
    test_set.append((correct_words, sentiment))
 return test_set

def calculate_accuracy_of_test(answers_file,result_file):
    file = open(result_file, 'r')
    answers =  file.readlines()
    file1 = open(answers_file,'r')
    results = file1.readlines()
    count = 0
    list_common = []
    for a, b in zip(answers, results):
     if a == b:
        list_common.append(a)
        count+=1
    print((count)*100)/len(answers)


####################Start of the Program##########################
#reading the data, this is the training data
file = open('Results.txt', 'r')
sents =  file.readlines()

#splitting the sentences in to sentiment and preprocessing the text for training data(flag is 0 for training)
split_train_sents_to_tokens(sents)

word_features =get_word_features(get_words_in_review_training_set(review_training_set))
word_features_set = set(word_features)
#creating a training set.
training_set = [(extract_features(d), c) for (d,c) in review_training_set]
write_to_train(training_set)


#listing the testfiles.
test_files_unstructed_list = ["FacebookReview.txt","YahooMailReviews.txt","ViberReviews.txt"]
test_answers_file_list = ["FacebookNumbers.txt","YahooMailNumbers.txt","ViberNumbers.txt"]

#reading the testing data
for test_file,test_file_answer in zip(test_files_unstructed_list,test_answers_file_list):
    results_file = open("output"+test_file_answer, "wb")
    test_sents=[]
    test_set=[]
    with open(test_file, 'r') as f:
       test_sents = f.readlines()
    #preprocessing the text for the testing data(flag is 1 for testing)
    test_set=split_test_sents_to_tokens(test_sents)
    testing_set_=  [(extract_features(d), c) for (d,c) in test_set]
    #writing the data to the csv file
    write_to_test(testing_set_,test_file+"test_structured.csv")
    classifier = DecisionTreeClassifier.train(training_set)
    for t in testing_set_:
       ans = classifier.classify(t[0])
       results_file.write(ans+"\n")
    testing_set_=[]
    results_file.close()

#Printing the accuracy of given table
for test_answer_file in test_answers_file_list:
    print("Calculating Accuracy of " + test_answer_file)
    print(" For NaiveBayes Classifier:")
    calculate_accuracy_of_test(test_answer_file,"output"+test_answer_file)
    print ("=========================================")












