package test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import bayes.NaiveBayes;
import reader.DataReader;
import svm.SVMTest;
import svm.SVMTest1;

public class Test {
    private static String[] trainPaths = {
            "C:\\Users\\Parag\\Desktop\\Project\\LBJSpamDetector\\data\\spam\\train",
            "C:\\Users\\Parag\\Desktop\\Project\\data\\train"
    };
    private static String[] testPaths = {
            "C:\\Users\\Parag\\Desktop\\Project\\LBJSpamDetector\\data\\spam\\test",
            "C:\\Users\\Parag\\Desktop\\Project\\data\\test"
    };
    private static NaiveBayes nbayes;
    private static List<String> testSet;
    private static List<String> trainSet;
    private static List<String> spamHams;
    private static boolean crossValidation = true;
    static int[] interestingTokens = { 5, 10, 15, 20, 30, 40, 50, 80, 100, Integer.MAX_VALUE};
    static int[] accuracyHam = new int[interestingTokens.length];
    static int[] accuracySpam = new int[interestingTokens.length];
    static int[] totalHam = new int[interestingTokens.length];
    static int[] totalSpam = new int[interestingTokens.length];

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        String dataset = args[1];
        if(args[0].equals("nb")) {
            if (args.length > 2) {
                boolean cf = Boolean.parseBoolean(args[2]);
                int ngram = Integer.parseInt(args[3]);
                if(dataset.equals("small"))
                    naiveBayes(cf, ngram);
                else {
                    indexFileRead_TREC(cf, ngram);
                    //testNewNaiveBayes(cf, ngram);
                    //naiveBayesClassifier(cf, ngram);
                }

            }else {
                if(dataset.equals("small"))
                    naiveBayes(false, 0);
                else {
                    indexFileRead_TREC(false, 0);
                    //testNewNaiveBayes(false, 0);
                    //naiveBayesClassifier(false, 0);
                }
            }
        }else if(args[0].equals("svm")) {
            if (args.length > 2) {
                boolean rp = Boolean.parseBoolean(args[2]);
                int size = Integer.parseInt(args[3]);
                svm_test(rp, size);
            } else
                svm_test(false, 0);
        }
        //indexFileRead_LBJ();
	}
	
	public static void svm_test(boolean rp, int dimSize){
		SVMTest1 svm = new SVMTest1();
        svm.setRP(rp);
        svm.setReducedDimensionSize(dimSize);
		try {
			String trainDirectory = trainPaths[0];
			svm.svmTrain(trainDirectory);
			
			String testDirectory = testPaths[0];
			svm.svmPredict(testDirectory);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static void indexFileRead_TREC(boolean cf, int ngram){
        String path = "C:\\Users\\Parag\\Desktop\\Project\\trec07p";
        String datapath = path + "\\data\\";
        String spamHamFile = path + "\\full\\index";
        spamHams = DataReader.readFile(spamHamFile);
        int spamCount = 5000;
        int hamCount = 3000;
        trainSet = new ArrayList<String>((int)(0.8*(spamCount+hamCount)));
        testSet = new ArrayList<String>((int)(0.2*(spamCount+hamCount)));
        /**
         * 80% for train. 20% for test
         */
        //Collections.shuffle(spamHams);
        if(!crossValidation) {
            int testSpam = 500;
            int testHam = 500;
            spamCount -=testSpam;
            hamCount -= testHam;

            int i = 0;
            while (i < spamHams.size() && (spamCount > 0 || hamCount > 0 || testSpam > 0 || testHam > 0)) {
                String email = spamHams.get(i);
                if (email.startsWith("spam")) {
                    if (spamCount > 0) {
                        trainSet.add(email);
                        spamCount--;
                    } else if (testSpam > 0) {
                        testSet.add(email);
                        testSpam--;
                    }
                } else {
                    if (hamCount > 0) {
                        trainSet.add(email);
                        hamCount--;
                    } else if (testHam > 0) {
                        testSet.add(email);
                        testHam--;
                    }
                }
                i++;
            }
            naiveBayesClassifier(cf, ngram);
        }else {
            ArrayList<String> spamCollection = new ArrayList<String>(spamCount);
            ArrayList<String> hamCollection = new ArrayList<String>(hamCount);
            int i = 0;
            while (i < spamHams.size() && (spamCount > 0 || hamCount > 0)) {
                String email = spamHams.get(i);

                if (email.startsWith("spam") && spamCount > 0) {
                    spamCollection.add(email);
                    spamCount--;
                } else if(email.startsWith("ham") && hamCount > 0) {
                    hamCollection.add(email);
                    hamCount--;
                }
                i++;
            }

            for (i = 1; i <= 16; i*=2) {
                int j=i;
                int partSpam = spamCollection.size()/5;
                int partHam = hamCollection.size()/5;
                int startSpam = 0;
                int startHam = 0;
                for(int k=0; k<5; k++) {
                    if ((j == (1 << k))) {
                        //add in test set
                        testSet.addAll(spamCollection.subList(startSpam, startSpam + partSpam));
                        testSet.addAll(hamCollection.subList(startHam, startHam + partHam));
                    } else{
                        trainSet.addAll(spamCollection.subList(startSpam, startSpam + partSpam));
                        trainSet.addAll(hamCollection.subList(startHam, startHam + partHam));
                    }
                    startSpam += partSpam;
                    startHam += partHam;
                }
                naiveBayesClassifier(cf, ngram);

                testSet.clear();
                trainSet.clear();
            }
        }
        for (int i = 0; i < interestingTokens.length; i++) {
            System.out.println();
            System.out.println("[SPAM]" + interestingTokens[i] + "-"
                    + accuracySpam[i] + "/" + totalSpam[i] + " = "
                    + (double) accuracySpam[i] / (double) totalSpam[i]);
            System.out.println("[HAM]" + interestingTokens[i] + "-"
                    + accuracyHam[i] + "/" + totalHam[i] + " = "
                    + (double) accuracyHam[i] / (double) totalHam[i]);
        }

    }

	// For TREC dataSet
	public static void naiveBayesClassifier(boolean cf, int ngram) {
        String path = "C:\\Users\\Parag\\Desktop\\Project\\trec07p";
        String datapath = path + "\\data\\";
        String emailPath = null;

            int[] count = new int[2];
            nbayes = new NaiveBayes();
            if (cf == true) {
                nbayes.cf = true;
                nbayes.ngram = ngram;
            }
            for (String line : trainSet) {

                // emailPath =
                // "..../Project/Datasets/TREC/trec07p/full/../data/inmail.1";
                emailPath = datapath;
                String subPath = line.substring(line.lastIndexOf("/") + 1);
                emailPath += subPath;
                if (line.startsWith("spam")) {
                    parseEmail(emailPath, true, nbayes);
                    count[0]++;
                } else {
                    parseEmail(emailPath, false, nbayes);
                    count[1]++;
                }
            }
            System.out.println("[TRAIN SPAM COUNT]: " + count[0]);
            System.out.println("[TRAIN HAM COUNT]: " + count[1]);

        /**
         * For dealing with rare words --> Smoothing
         */
		nbayes.calcSpamicityWithSmoothing();

		//nbayes.correctedSpamicity();


        for (String line : testSet) {
            // emailPath =
            // "..../Project/Datasets/TREC/trec07p/full/../data/inmail.68676";
            emailPath = datapath;
            String subPath = line.substring(line.lastIndexOf("/") + 1);
            emailPath += subPath;
            SortedSet<Map.Entry<String, Double>> interestingWords = nbayes.findInterestingWords(DataReader.readFile(emailPath));
            for (int i = 0; i < interestingTokens.length; i++) {
                double p = nbayes.calcSpamProbability(interestingWords, interestingTokens[i]);
                //System.out.println(p);
                if (line.startsWith("spam")) {
                    totalSpam[i]++;
                    if (p >= 0.9)
                        accuracySpam[i]++;
                } else {
                    totalHam[i]++;
                    if (p < 0.9)
                        accuracyHam[i]++;
                    else {
                        System.out.println("Incorrectly classified as Spam " + line + " having p = " + p);

                            //System.out.println(interestingTokens[i]+" Interesting words ");
                            int j=0;
                            for(Iterator<Map.Entry<String, Double>> it = interestingWords.iterator() ; it.hasNext() && j< 10; j++){
                                Map.Entry<String, Double> entry = it.next();
                                System.out.println(entry.getKey()+" "+entry.getValue());

                            }

                    }
                }
            }
            interestingWords.clear();
        }
	}
    public static void naiveBayes(boolean cf, int ngram) {
        File d = new File(trainPaths[0]+"\\spam");
        ArrayList<File> trainSpam = new ArrayList<File>();
        for (File f : d.listFiles()) {
                trainSpam.add(f);
        }
        d = new File(trainPaths[0]+"\\ham");
        ArrayList<File> trainHam = new ArrayList<File>();
        for (File f : d.listFiles()) {
                trainHam.add(f);
        }

        d = new File(testPaths[0]+"\\spam");
        ArrayList<File> testSpam = new ArrayList<File>();
        for (File f : d.listFiles()) {
                testSpam.add(f);
        }
        d = new File(testPaths[0]+"\\ham");
        ArrayList<File> testHam = new ArrayList<File>();
        for (File f : d.listFiles()) {
                testHam.add(f);
        }

        int[] count = new int[2];
        NaiveBayes nbayes = new NaiveBayes();
        if(cf==true) {
            nbayes.cf = true;
            nbayes.ngram = ngram;
        }
        for (File f : trainSpam) {
                parseEmail(f.getAbsolutePath(), true, nbayes);
                count[0]++;
        }
        for (File f : trainHam) {
            parseEmail(f.getAbsolutePath(), false, nbayes);
            count[1]++;
        }
        System.out.println("[TRAIN SPAM COUNT]: " + count[0]);
        System.out.println("[TRAIN HAM COUNT]: " + count[1]);
        /**
         * For dealing with rare words --> Smoothing
         */
        nbayes.calcSpamicityWithSmoothing();

        //nbayes.correctedSpamicity();

        int[] interestingTokens = { 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, Integer.MAX_VALUE};
        int[] accuracyHam = new int[interestingTokens.length];
        int[] accuracySpam = new int[interestingTokens.length];
        int[] totalHam = new int[interestingTokens.length];
        int[] totalSpam = new int[interestingTokens.length];
        for (File f : testSpam) {

            // emailPath =
            // "..../Project/Datasets/TREC/trec07p/full/../data/inmail.68676";
            SortedSet<Map.Entry<String, Double>> interestingWords = nbayes.findInterestingWords(DataReader.readFile(f.getAbsolutePath()));
            for (int i = 0; i < interestingTokens.length; i++) {
                double p = nbayes.calcSpamProbability(interestingWords, interestingTokens[i]);
                /*if(line.contains("inmail.33437")){
                    System.out.println(interestingTokens[i]+" Interesting word for 33437 ");
                    int j=0;
                    for(Iterator<Map.Entry<String, Double>> it = interestingWords.iterator() ; it.hasNext() && j<i; j++){
                        Map.Entry<String, Double> entry = (Map.Entry<String, Double>) it.next();
                        System.out.println(entry.getKey()+" "+entry.getValue());

                    }
                }*/
                    totalSpam[i]++;
                    if (p > 0.9)
                        accuracySpam[i]++;
                }
            }
        for (File f : testHam) {
            SortedSet<Map.Entry<String, Double>> interestingWords = nbayes.findInterestingWords(DataReader.readFile(f.getAbsolutePath()));
            for (int i = 0; i < interestingTokens.length; i++) {
                double p = nbayes.calcSpamProbability(interestingWords, interestingTokens[i]);
                totalHam[i]++;
                if (p < 0.9)
                    accuracyHam[i]++;
            }
        }
        for (int i = 0; i < interestingTokens.length; i++) {
            System.out.println();
            System.out.println("[SPAM]" + interestingTokens[i] + "-"
                    + accuracySpam[i] + "/" + totalSpam[i] + " = "
                    + (double) accuracySpam[i] / (double) totalSpam[i]);
            System.out.println("[HAM]" + interestingTokens[i] + "-"
                    + accuracyHam[i] + "/" + totalHam[i] + " = "
                    + (double) accuracyHam[i] / (double) totalHam[i]);
        }

    }
	/**
	 * Only TEXT. No HTML. Feature = Words (text separated by any non-word
	 * character)
	 * 
	 * @param emailPath
	 *
	 * @param isSpam
	 * @param nbayes
	 */
	public static void parseEmail(String emailPath, boolean isSpam, NaiveBayes nbayes) {
		//List<String> email = DataReader.readFile(emailPath);

		// Only text based
		if (isSpam) {
			nbayes.trainSpam(emailPath);

		} else {
			nbayes.trainHam(emailPath);
		}
	}
    public static void testNewNaiveBayes(boolean cf, int ngram){
        String path = "C:\\Users\\Parag\\Desktop\\Project\\trec07p";
        String datapath = path+"\\data\\";

        String emailPath = null;
        int[] count = new int[2];
        nbayes = new NaiveBayes();

        if(cf==true) {
            nbayes.cf = true;
            nbayes.ngram = ngram;
        }
        for (String line : trainSet) {
            emailPath = datapath;
            String subPath = line.substring(line.lastIndexOf("/")+1);
            emailPath += subPath;
            // emailPath =
            // "..../Project/Datasets/TREC/trec07p/full/../data/inmail.1";

            if (line.startsWith("spam")) {
                parseEmail(emailPath, true, nbayes);
                count[0]++;
            } else {
                parseEmail(emailPath, false, nbayes);
                count[1]++;
            }
        }
        System.out.println("[TRAIN SPAM COUNT]: " + count[0]);
        System.out.println("[TRAIN HAM COUNT]: " + count[1]);
        /**
         * For dealing with rare words --> Smoothing
         */


        //nbayes.correctedSpamicity();

        int[] interestingTokens = { 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, Integer.MAX_VALUE};
        int[] accuracyHam = new int[interestingTokens.length];
        int[] accuracySpam = new int[interestingTokens.length];
        int[] totalHam = new int[interestingTokens.length];
        int[] totalSpam = new int[interestingTokens.length];
        for (String line : testSet) {

            emailPath = datapath;
            String subPath = line.substring(line.lastIndexOf("/")+1);
            emailPath += subPath;
            // emailPath =
            // "..../Project/Datasets/TREC/trec07p/full/../data/inmail.68676";
             double  p = nbayes.nbProb(DataReader.readFile(emailPath));


                if (line.startsWith("spam")) {
                    totalSpam[0]++;
                    if (p > 0)
                        accuracySpam[0]++;
                } else {
                    totalHam[0]++;
                    if (p < 0)
                        accuracyHam[0]++;

                }



        }
        for (int i = 0; i < interestingTokens.length; i++) {
            System.out.println();
            System.out.println("[SPAM]" + interestingTokens[i] + "-"
                    + accuracySpam[i] + "/" + totalSpam[i] + " = "
                    + (double) accuracySpam[i] / (double) totalSpam[i]);
            System.out.println("[HAM]" + interestingTokens[i] + "-"
                    + accuracyHam[i] + "/" + totalHam[i] + " = "
                    + (double) accuracyHam[i] / (double) totalHam[i]);
        }

    }
}
//END OF CODE
 /*
    //For DataSet from LBJ Tutorial
    public static void indexFileRead_LBJ() {
        int[] count = new int[2];
        NaiveBayes nbayes = new NaiveBayes();


        List<String> trainSet = new ArrayList<String>();
        File[] files = new File("/.../LBJSpamDetector/data/spam/train/ham/").listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.
        for (File file : files) {
            if (file.isFile()) {
                trainSet.add(file.getName());
            }
        }
        files = new File("/.../LBJSpamDetector/data/spam/train/spam/").listFiles();
        for (File file : files) {
            if (file.isFile()) {
                trainSet.add(file.getName());
            }
        }
        String emailPath = "";
        for (String line : trainSet) {

            if (line.endsWith("spam.txt")) {
                emailPath = "/..../LBJSpamDetector/data/spam/train/spam/"+line;
                parseEmail(emailPath, true, nbayes);
                count[0]++;
            } else {
                emailPath = "/.../LBJSpamDetector/data/spam/train/ham/"+line;
                parseEmail(emailPath, false, nbayes);
                count[1]++;
            }
        }
        System.out.println("[TRAIN SPAM COUNT]: " + count[0]);
        System.out.println("[TRAIN HAM COUNT]: " + count[1]);
        nbayes.calcSpamicity();

        nbayes.correctedSpamicity();



        List<String> testSet = new ArrayList<String>();

        files = new File("/.../LBJSpamDetector/data/spam/test/ham/").listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.
        for (File file : files) {
            if (file.isFile()) {
                testSet.add(file.getName());
            }
        }
        files = new File("/.../LBJSpamDetector/data/spam/test/spam/").listFiles();
        for (File file : files) {
            if (file.isFile()) {
                testSet.add(file.getName());
            }
        }

        int[] interestingTokens = { 5, 10, 15, 25, 35, 45 };
        for (int i = 0; i < interestingTokens.length; i++) {
            int accuracyHam = 0;
            int accuracySpam = 0;
            int totalHam = 0;
            int totalSpam = 0;
            for (String line : trainSet) {

                if (line.endsWith("spam.txt")) {

                    emailPath = "/.../LBJSpamDetector/data/spam/train/spam/"+line;
                }else{
                    emailPath = "/.../LBJSpamDetector/data/spam/train/ham/"+line;
                }

                double p = nbayes.calcSpamProbability(DataReader.readFile(emailPath), interestingTokens[i]);
                if (line.endsWith("spam.txt")) {
                    totalSpam++;
                    if (p > 0.9)
                        accuracySpam++;
                } else {
                    totalHam++;
                    if (p < 0.9)
                        accuracyHam++;
                }
//				 System.out.println(line +" : "+p);

            }//end for testSet
            System.out.println();
            System.out.println("[SPAM]"
                    + interestingTokens[i] + "-"
                    + accuracySpam + "/" + totalSpam + " = "
                    + (double) accuracySpam / (double) totalSpam);
            System.out.println("[HAM]"
                    + interestingTokens[i] + "-"
                    + accuracyHam + "/" + totalHam + " = "
                    + (double) accuracyHam / (double) totalHam);

        }


    }*/
/*for (int i = 0; i < interestingTokens.length; i++) {
			int accuracyHam = 0;
			int accuracySpam = 0;
			int totalHam = 0;
			int totalSpam = 0;
			for (String line : testSet) {

				emailPath = datapath;
				String subPath = line.substring(line.lastIndexOf("/")+1);
				emailPath += subPath;
				// emailPath =
				// "/.../Project/Datasets/TREC/trec07p/full/../data/inmail.68676";
                SortedSet<Map.Entry<String, Double>> interestingWords = nbayes.findInterestingWords(DataReader.readFile(emailPath));
				double p = nbayes.calcSpamProbability(interestingWords);

				if (line.startsWith("spam")) {
					totalSpam++;
					if (p > 0.9)
						accuracySpam++;
				} else {
					totalHam++;
					if (p < 0.9)
						accuracyHam++;
				}

			}
			System.out.println();
			System.out.println("[SPAM]" + interestingTokens[i] + "-"
					+ accuracySpam + "/" + totalSpam + " = "
					+ (double) accuracySpam / (double) totalSpam);
			System.out.println("[HAM]" + interestingTokens[i] + "-"
					+ accuracyHam + "/" + totalHam + " = "
					+ (double) accuracyHam / (double) totalHam);
		}*/

