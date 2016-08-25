package classification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.StatUtils;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;
//import weka.classifiers.functions.*;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.supportVector.RBFKernel;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

public class Driver {

	/**
	 * 
	 *
	 * @param args[0] is the output file name for the performance results(comp1.csv)
	 *        args[1] is the output file name for the algorithm comparison results (A3PerformanceResults.csv)
	 *        args[2] is the input dataset file(trainingset.csv)
	 * @throws Exception I have not handled any exceptions in my code. You may want to for debugging purposes.
	 */
	public static void main(String[] args) throws Exception {


		//the list of classifiers	
		String [] classifiers_names=new String[13];
		classifiers_names[0]="IBk-1";
		classifiers_names[1]="IBk-3";
		classifiers_names[2]="IBk-5";
		classifiers_names[3]="IBk-10";
		classifiers_names[4]="J48-2";
		classifiers_names[5]="J48-5";
		classifiers_names[6]="J48-10";
		classifiers_names[7]="J48-30";
		classifiers_names[8]="NBC";
		classifiers_names[9]="SMO-1-P";
		classifiers_names[10]="SMO-5-P";
		classifiers_names[11]="SMO-1-RBF";
		classifiers_names[12]="SMO-5-RBF";


		//CSV loader    
		CSVLoader loader=new CSVLoader();
		loader.setSource(new File(args[2]));
		Instances _dataset=loader.getDataSet();
		String[] options =new String[4];
		options[0] = "-B";
		options[1] = "10";
		options[2] = "-R";
		options[3] = "last";
		Discretize discretize=new Discretize();
		discretize.setOptions(options);
		discretize.setInputFormat(_dataset);
		Instances dataset=Filter.useFilter(_dataset, discretize);
		// Constructing a weka Instances object representing the data

		dataset.setClassIndex(dataset.numAttributes()-1);


		//Set the number of runs
		int numRuns = 5;

		//Set the random seed for repeatability (PICK AN ARBITRARY SEED THAT IS DIFFERENT THAN THIS ONE!)
		long randSeed = 12L;
		StatUtils stat;

		//Set up your output files:
		PrintWriter perfResults = new PrintWriter(new BufferedWriter(new FileWriter(new File(args[1]))));
		PrintWriter compResults = new PrintWriter(new BufferedWriter(new FileWriter(new File(args[0]))));
		//printing some header information to help track
		//the results in the output files. 
		// Instantiate the classifiers and set the appropriate parameters given in the
		//assignment
		J48 tree = new J48();
		tree.setMinNumObj(2);
		J48 tree2 = new J48();
		tree2.setMinNumObj(5);
		J48 tree3 = new J48();
		tree3.setMinNumObj(10);
		J48 tree4 = new J48();
		tree4.setMinNumObj(30);
		NaiveBayes nb= new NaiveBayes(); 

		SMO svm1 = new SMO();
		svm1.setC(1.0);
		SMO svm2= new SMO();
		svm2.setC(5.0);
		SMO smv3=new SMO();

		RBFKernel rbf=new RBFKernel();

		smv3.setKernel(rbf);
		smv3.setC(1.0);
		SMO smv4=new SMO();
		smv4.setKernel(rbf);
		smv4.setC(5.0);
		IBk ibk1=new IBk(1);
		IBk ibk2=new IBk(3);
		IBk ibk3=new IBk(5);
		IBk ibk4=new IBk(10);

		//Adding them all to a list of classifiers
		List<Classifier> classifiers = new ArrayList<>();
		classifiers.add(ibk1);
		classifiers.add(ibk2);
		classifiers.add(ibk3);
		classifiers.add(ibk4);
		classifiers.add(tree);
		classifiers.add(tree2);
		classifiers.add(tree3);
		classifiers.add(tree4);
		classifiers.add(nb);
		classifiers.add(svm1);
		classifiers.add(svm2);
		classifiers.add(smv3);
		classifiers.add(smv4);
		// generating the results in a variety of ways. 

		List<Generator> tdGenerators = new ArrayList<>();
		tdGenerators.add(new HoldoutGenerator(dataset, randSeed, 1.0/3.0));
		tdGenerators.add(new CrossValidationGenerator(dataset,randSeed));
		tdGenerators.add(new ResampleGenerator(dataset,randSeed));
		// adding the other generators using the parameters 
		Map<String, double[]> performance = new LinkedHashMap<>();
		Map<String,Double> mean_values=new LinkedHashMap<>();
		Map<String,Double> sd_values=new LinkedHashMap<>();
		compResults.append('#');
		compResults.append(',');
		compResults.flush();
		for(int i=0;i<numRuns+2;i++)
		{
			if(i==numRuns)
			{
				compResults.append("mean");
				compResults.append(',');
			}
			else if(i==numRuns+1)
			{
				compResults.append("sd");
			}
			else
			{
				compResults.append("run"+Integer.toString(i));
				compResults.append(',');

			}

		}
		compResults.append('\n');
		compResults.flush();
	//This is the main testing loop.
		//For each classification algorithm and parameter setting...
		int count_c=0;
		for(Classifier alg : classifiers){
			//Breaking up the data according to each of the methods, i.e., holdout, cross validation, etc.
			for(Generator g : tdGenerators){
				// Each iteration of this loop creates data for 2 columns of the performance results file (resub and generalization)
			
				double[] resubErrors = new double[numRuns];
				double[] genErrors = new double[numRuns];
				Evaluation _evalTest;
				for(int run = 0; run < numRuns; ++run){
					g.initializeRun();
					_evalTest=g.getEvalutaion();
					double avgPartResubErr = 0.0;
					double avgPartGenErr = 0.0; 
					for(int part = 0; part < g.getNumPartitions(); ++part){

						Instances train;
						Instances test;
						if(g instanceof CrossValidationGenerator) {
							train = ((CrossValidationGenerator) g).getNextTraingingSet(part);
							test=((CrossValidationGenerator) g).getNextTestingSet(part);
						} else {

							train = g.getNextTrainingSet();
							test = g.getNextTestingSet();
						}
						System.out.println("Building Classifier");
						alg.buildClassifier(train);
						System.out.println("Evaluating the Model");
						_evalTest.evaluateModel(alg,test);
						double genErr = 0.0;
						double resubErr = 0.0;

						//Getting the resubstitution error and estimated generalization error 
						//of the classifier alg.
						System.out.println("Calculating errors");
						double errcount_train=0;
						double errcount_test =0;
						for(int i=0;i<train.numInstances();i++)
						{
							double pred=alg.classifyInstance(train.instance(i));
							String actual=Double.toString(train.instance(i).classValue());
							String predicted=Double.toString(pred);
							if(!actual.equals(predicted))
							{
								errcount_train++;

							}

						}	
						System.out.println("Calculating Resub and Generalization errors");
						resubErr=(errcount_train)/train.numInstances();
						genErr=_evalTest.errorRate();
						avgPartResubErr += resubErr;
						avgPartGenErr += genErr;
					}
					//Storing the error rates in so they can be printed later
					resubErrors[run] = avgPartResubErr/g.getNumPartitions();
					genErrors[run] = avgPartGenErr/g.getNumPartitions();
					g.reset();
				}
				//Using alg.toString() gives unpredictable results since different Weka algorithms
				//implemented toString differently. It is just used as a placeholder!
				System.out.println(g.toString());
				performance.put( classifiers_names[count_c]+g.toString(), resubErrors);
				performance.put(classifiers_names[count_c]+g.toString()+"gen", genErrors);
				Double meanresub=StatUtils.mean(resubErrors);
				Double meangen=StatUtils.mean(genErrors);
				mean_values.put( classifiers_names[count_c]+g.toString(),meanresub );
				mean_values.put(classifiers_names[count_c]+g.toString()+"gen", meangen);
				Double var_resub=StatUtils.variance(resubErrors);
				Double sd_resub=Math.sqrt(var_resub);
				Double var_gen=StatUtils.variance(genErrors);
				Double sd_gen=Math.sqrt(var_gen);
				System.out.println(g.toString());
				sd_values.put( classifiers_names[count_c]+g.toString(),sd_resub );
				sd_values.put(classifiers_names[count_c]+g.toString()+"gen", sd_gen);
				compResults.append(classifiers_names[count_c]+g.toString());
				compResults.append(',');
				for(int i=0;i<numRuns+2;i++)
				{

					if(i==numRuns)
					{
						compResults.append(Double.toString(meanresub));
						compResults.append(',');
					}
					else if(i==numRuns+1)
					{
						compResults.append(Double.toString(sd_resub));
					}
					else
					{
						compResults.append(Double.toString(resubErrors[i]));
						compResults.append(',');
					}

				}
				compResults.append('\n');
				compResults.flush();
				compResults.append(classifiers_names[count_c]+g.toString()+"gen");
				compResults.append(',');
				for(int i=0;i<numRuns+2;i++)
				{
					if(i==numRuns)
					{
						compResults.append(Double.toString(meangen));
						compResults.append(',');
					}
					else if(i==numRuns+1)
					{
						compResults.append(Double.toString(sd_gen));
					}
					else
					{
						compResults.append(Double.toString(genErrors[i]));
						compResults.append(',');

					}


				}
				compResults.append('\n');
				compResults.flush();



			}
			count_c++;
		}

		int pcount=0;
		//Calculating the Student-t-test
		TTest ttest=new TTest();

		perfResults.append('#');

		perfResults.append(',');

		for(String key3 : performance.keySet()) {
			perfResults.append(key3);
			perfResults.append(',');

		}
		perfResults.append('\n');

		for(String key1 : performance.keySet()) {
			perfResults.append(key1);
			perfResults.append(',');
			for(String key2 : performance.keySet()) {


				double p=ttest.pairedTTest(performance.get(key1), performance.get(key2));
				p=Math.round(p*1000)/1000.0;
				System.out.println(key1+ ":" + key2+":"+p);


				perfResults.append(Double.toString(p));
				perfResults.append(',');
				perfResults.flush();
				pcount++;

			}
			perfResults.append('\n');
			perfResults.flush();
		}
		System.out.println("pcount:"+pcount);
		// Performed t-tests to determine which, if any, algorithms have a performance
		//advantage on this data set, and what parameter setting works best.
		//Compared every set of generalization performances against all others
		//using the pairedTTest method from org.apache.commons.math3.stat.inference.TTest
		//to obtain a p-value. Recorded the p-values in the A3compResults file.


		perfResults.flush();
		perfResults.close();
		compResults.close();
	}


	public static abstract class Generator {
		/** A copy of the source data used to generate training and testing samples */
		protected Instances dataCopy;
		/** A source of randomness */
		protected Random srcRand;
		/** Seed to control the random behavior for experimental repeatability */
		protected long randSeed;

		private Evaluation eval;

		/**
		 * Creating a new training data Generator from the specified source data and
		 * with the given seed for experimental repeatability.
		 * 
		 * @param srcData must not be null
		 * @param randSeed 
		 */
		public Generator(Instances srcData, long randSeed){
			if(srcData.classIndex() <  0)
				srcData.setClassIndex(srcData.numAttributes()-1);
			dataCopy = new Instances(srcData);
			this.randSeed = randSeed;
			srcRand = new Random(randSeed);
		}


		public void initializeRun(){
			dataCopy.randomize(srcRand);
			try {
				eval = new Evaluation(dataCopy);
			} catch (Exception e) {

			}

		}

		public Evaluation getEvalutaion() {
			return eval;
		}

		/**
		 * Get the next training set generated according to a specific
		 * training data generation method.
		 * 
		 * @return non-null set of data instances drawn from the source data
		 */
		public abstract Instances getNextTrainingSet();

		/**
		 * Get the next testing set generated according to a specific
		 * testing generation method.
		 * @return non-null set of data instances drawn from the source data
		 */
		public abstract Instances getNextTestingSet();

		/**
		 * Return the number of individual partitions that must be calculated for this
		 * training method. 
		 * @return
		 */
		public int getNumPartitions(){
			return 1;
		}

		/**
		 * Reset the random state of this training data Generator.
		 */
		public void reset(){
			srcRand = new Random(randSeed);
		}
	}

	/**
	 * This class implements a random hold-out procedure.
	 * 
	 *
	 */
	public static class HoldoutGenerator extends Generator{
		/** The fraction of the data to randomly hold out for testing. */
		private int trainingSize;

		/**
		 * Create a new training set generator following the Holdout process
		 * @param srcData must not be null
		 * @param randSeed must not be null
		 * @param holdoutFraction must be in the range (0,1)
		 */
		public HoldoutGenerator(Instances srcData, long randSeed, double holdoutFraction) {
			super(srcData, randSeed);
			trainingSize = (int)(holdoutFraction*dataCopy.numInstances());
		}

		@Override
		public Instances getNextTrainingSet() {
			//The randomization is done automatically by initializeRun at the start of each run.
			return new Instances(dataCopy, 0, trainingSize);
		}

		@Override
		public Instances getNextTestingSet() {
			//the data has been shuffled around in getNextTrainingSet so we just need
			//to copy over the remaining data
			return new Instances(dataCopy, trainingSize, dataCopy.numInstances() - trainingSize);
		}

		@Override
		public String toString(){
			return "Holdout";
		}
	}

	public static class CrossValidationGenerator extends Generator{

		private static final int cvPartitions = 10;

		public CrossValidationGenerator(Instances srcData, long randSeed) {
			super(srcData, randSeed);

		}

		@Override
		public  void initializeRun() {
			super.initializeRun();
			//done stratified sampling
			if(dataCopy.classAttribute().isNominal())
			{
				dataCopy.stratify(cvPartitions);
			}
			
		}

		@Override
		public Instances getNextTrainingSet() {
			
			return null;
		}

		public Instances getNextTraingingSet(int number) {
			return dataCopy.trainCV(cvPartitions , number);
		}
		public Instances getNextTestingSet(int number) {
			return dataCopy.testCV(cvPartitions , number);
		}


		@Override
		public Instances getNextTestingSet() {
			
			return null;
		}

		@Override
		public int getNumPartitions() {
			// TODO Return the number of partitions done by CV
			return this.cvPartitions;
		}

		@Override
		public void reset() {
			super.reset();
			//TODO reset any state variables used to keep track of folds
		}

		@Override
		public String toString(){
			return "CV";
		}
	}

	

	public static class ResampleGenerator extends Generator{
		public Instances random_selected_data;
		private Evaluation resample_eval;
		public Instances trainingdata;
		public Instances testingdata;

		public ResampleGenerator(Instances srcData, long randSeed) {
			super(srcData, randSeed);

			System.out.println("Resample");



			
		}
		// this function does resampling with replacement. It generates testing and training data for every run 
		//returns evaluation object
		public void get_train_test_data() {
			
			double pickedNumber;
			//randomly creates a fraction (0 to 1)
			Random rand = new Random(); 
			int RandomdataSize = (int)(0.632*dataCopy.numInstances());
			System.out.println(RandomdataSize);
			Instances newdata=new Instances(dataCopy,0,dataCopy.numInstances());
			System.out.println("The num,ber of newdata");
			System.out.println(newdata.numInstances());
			trainingdata=new Instances(newdata,RandomdataSize);
			testingdata=new Instances(newdata,(newdata.numInstances()-RandomdataSize)+1);
			ArrayList<Integer> array_ = new ArrayList<Integer>();
			//randomly picking the testing set
			for(int i=0;i<RandomdataSize;i++)
			{
				int randomi=rand.nextInt(RandomdataSize);
				Instance inst = newdata.instance(randomi);
				array_.add(randomi);
				inst.setDataset(newdata);
				testingdata.add(inst);
			}
		//	collecting the training setis not there in the testing set
			for(int i=0;i<newdata.numInstances();i++){

				if(array_.contains(i))	
				{continue;}
				Instance inst1 = newdata.instance(i);
				inst1.setDataset(newdata);
				trainingdata.add(inst1);
			}
			try
			{
				
			}

			catch(Exception e)
			{
				System.out.println(e.getMessage());			 
			}

		}

		@Override
		public Instances getNextTrainingSet() {
			
			System.out.println("Nxt training set");
			this.get_train_test_data();
		
			System.out.println(trainingdata.numInstances());
			return new Instances(trainingdata,0,trainingdata.numInstances());
		}

		@Override
		public Instances getNextTestingSet() {
			
			System.out.println("Next testing set");
			System.out.println(testingdata.numInstances());
			return new Instances(testingdata,0,testingdata.numInstances());
		}

		@Override
		public String toString(){
			return "Resample";
		}
	}
}
