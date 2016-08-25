import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.NumericToNominal;

public class SentimentClassifier {
	

	static ArrayList<String> answers= new ArrayList<String>();
	static ArrayList<String> results= new ArrayList<String>();
	static ArrayList<Classifier>classifier_list = new ArrayList<Classifier>();
	static ArrayList<String>classifier_names = new ArrayList<String>();
	//holds the classifer's accuracies
	static TreeMap<String,Double>classifiers_accuracy = new TreeMap<String,Double>();
//Intializing classsifers that we have picked after doing evaluation
	public static void intilialize_classifiers(){
		//SMO with RBF Kernel with C value 1.0
		RBFKernel rbf=new RBFKernel();
		SMO smo=new SMO();
		smo.setKernel(rbf);
		smo.setC(1.0);
		classifier_list.add(smo);
		classifier_names.add("SMO-RBF-1");	
	
	}
	
	
	
	// comparing the Predictions made by the classifier with the actual classlables(handwritten) for calculating the error rate and accuracy
	
	public static void compareArrays(String outputfile_name)
	{
		int i = 0;
		int count = 0;
		while(i < answers.size())
		{
			if(Integer.parseInt(answers.get(i)) == Integer.parseInt( results.get(i)))
			{
				count++;
				System.out.println(count);
			}
			i++;
		}
		Double accuracy = (double) ((count*100)/answers.size());
		classifiers_accuracy.put(outputfile_name, accuracy);
		
	}
	static void write_results_to_file(String fileName)
	{
		 try {
	            
	            FileWriter fileWriter =
	                new FileWriter(fileName);

	           
	            BufferedWriter bufferedWriter =
	                new BufferedWriter(fileWriter);

	           
	           for(int i=0;i<answers.size();i++)
	           { 
	        	   bufferedWriter.write(i+" "+results.get(i)+ "===="+answers.get(i));
	        	   bufferedWriter.write("\n");
	           }
	           bufferedWriter.write("The number of correctly classifed reviews :");
	           compareArrays(fileName.substring(0, fileName.length() - 4));
	            bufferedWriter.close();
	        }
	        catch(IOException ex) {
	            System.out.println(
	                "Error writing to file '"
	                + fileName + "'");
	             }
	    }
		
	static void Parse_Answers(String fileName)
	{
		// This will reference one line at a time
        String line = null;

        try {
             FileReader fileReader = 
                new FileReader(fileName);

            BufferedReader bufferedReader = 
                new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                answers.add(line);
            }   
             bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
                   }
    }
	public static void Runner(String[] args)throws Exception
	{
//		
		CSVLoader loader=new CSVLoader();
		loader.setSource(new File(args[0]));
		Instances dataset=loader.getDataSet();
		String[] options =new String[4];
		options[0] = "-B";
		options[1] = "10";
		options[2] = "-R";
		options[3] = "last";
		Discretize discretize=new Discretize();
		discretize.setOptions(options);
		discretize.setInputFormat(dataset);
		
		NumericToNominal convert= new NumericToNominal();
        String[] options_= new String[2];
        options_[0]="-R";
        options_[1]="1-2";  //range of variables to make numeric
        convert.setOptions(options_);
        convert.setInputFormat(dataset);
        Instances _dataset=Filter.useFilter(dataset,discretize);
         _dataset.setClassIndex(dataset.numAttributes()-1);
		Instances dataset_train= _dataset;
		CSVLoader loader1=new CSVLoader();
		loader.setSource(new File(args[1]));
		loader1.setSource(new File(args[1]));
	    Instances _dataset_test=loader1.getDataSet();
	    Instances dataset_test=Filter.useFilter(_dataset_test, discretize);
	    //setting the last attribute of the data set as the class label for train and test set
		dataset_train.setClassIndex(dataset_train.numAttributes()-1);
		dataset_test.setClassIndex(dataset_test.numAttributes()-1);
		//Initializing classifiers
		intilialize_classifiers();
		//Getting the answers from the answers file
		Parse_Answers(args[2]);
		//loops through all the classifiers and predicts the classlabel for the testing data set
		for(int j=0;j<classifier_list.size();j++ )
		{
			Classifier classifier = classifier_list.get(j);
			classifier.buildClassifier(dataset_train);		
			 for (int i = 0; i < dataset_test.numInstances(); i++) {
				   int pred = (int)classifier.classifyInstance(dataset_test.instance(i));
				   if (pred == 9)
				   {
					   pred = 1;
				   }
				   System.out.println(" predicted: " + pred);
				   results.add(Integer.toString(pred));
				   }
			 // writing the results to the to the file 
			 write_results_to_file(args[3]+classifier_names.get(j)+".txt");
			 for(String s: results)
				{
					System.out.println(s);
				}
			 results.clear();
			 
		}
		
		answers.clear();
		results.clear();
		classifier_list.clear();
		classifier_names.clear();
	}
	
	
	public static void main(String[] args) throws Exception {
		
		
		// testing files related to facebook, Yahoo , Viber
		String[] arguments_for_facebook = new String[] {"trainingset.csv", "test_set_processed_facebook.csv", "FaceBookNumbers.txt", "testingset_output_facebook"};
		SentimentClassifier.Runner(arguments_for_facebook);
		
		String[] arguments_for_yahoo_mail = new String[] {"trainingset.csv", "test_set_processed_yahoo.csv", "YahooMailNumbers.txt", "testingset_output_yahoo"};
		SentimentClassifier.Runner(arguments_for_yahoo_mail);
		
		
		
		String[] arguments_for_viber = new String[] {"trainingset.csv", "test_set_processed_viber.csv", "ViberNumbers.txt", "testingset_output_viber"};
		SentimentClassifier.Runner(arguments_for_viber);
		
		Iterator entries = classifiers_accuracy.entrySet().iterator();
		
		//printing the accuracy
		while (entries.hasNext()) {
		    Map.Entry entry = (Map.Entry) entries.next();
		    String key = (String) entry.getKey();
		    Double value = (Double) entry.getValue();
		    System.out.println( key + "Accuracy Value = " + value);
		}
	    
	      
			}	

}
