package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bayes.NaiveBayes1;
import reader.DataReader;
import svm.SVMTest;

public class Test1 {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
		indexFileRead_TREC();
//		indexFileRead_LBJ();
        /*if(args.length > 0) {
            boolean rp = Boolean.parseBoolean(args[0]);
            int size = Integer.parseInt(args[1]);
            svm_test(rp, size);
        }else
            svm_test(false, 0);*/
    }

    public static void svm_test(boolean rp, int dimSize){
        SVMTest svm = new SVMTest();
        svm.setRP(rp);
        svm.setReducedDimensionSize(dimSize);
        try {
            String trainDirectory = "C:\\Users\\Parag\\Desktop\\Project\\LBJSpamDetector\\data\\spam\\train";
            svm.svmTrain(trainDirectory);

            String testDirectory = "C:\\Users\\Parag\\Desktop\\Project\\LBJSpamDetector\\data\\spam\\test";
            svm.svmPredict(testDirectory);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    //For DataSet from LBJ Tutorial
    public static void indexFileRead_LBJ() {
        int[] count = new int[2];
        NaiveBayes1 nbayes = new NaiveBayes1();

        /**
         * TRAIN SET
         */
        List<String> trainSet = new ArrayList<String>();
        File[] files = new File("/Users/MayankMahajan/Desktop/EclipseWorkspace/LBJSpamDetector/data/spam/train/ham/").listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.
        for (File file : files) {
            if (file.isFile()) {
                trainSet.add(file.getName());
            }
        }
        files = new File("/Users/MayankMahajan/Desktop/EclipseWorkspace/LBJSpamDetector/data/spam/train/spam/").listFiles();
        for (File file : files) {
            if (file.isFile()) {
                trainSet.add(file.getName());
            }
        }
        String emailPath = "";
        for (String line : trainSet) {

            if (line.endsWith("spam.txt")) {
                emailPath = "/Users/MayankMahajan/Desktop/EclipseWorkspace/LBJSpamDetector/data/spam/train/spam/"+line;
                parseEmail(emailPath, true, nbayes);
                count[0]++;
            } else {
                emailPath = "/Users/MayankMahajan/Desktop/EclipseWorkspace/LBJSpamDetector/data/spam/train/ham/"+line;
                parseEmail(emailPath, false, nbayes);
                count[1]++;
            }
        }
        System.out.println("[TRAIN SPAM COUNT]: " + count[0]);
        System.out.println("[TRAIN HAM COUNT]: " + count[1]);
        nbayes.calcSpamicity();
        /**
         * For dealing with rare words --> Smoothing
         */
        nbayes.correctedSpamicity();


        /**
         * TEST SET
         */
        List<String> testSet = new ArrayList<String>();

        files = new File("/Users/MayankMahajan/Desktop/EclipseWorkspace/LBJSpamDetector/data/spam/test/ham/").listFiles();
        //If this pathname does not denote a directory, then listFiles() returns null.
        for (File file : files) {
            if (file.isFile()) {
                testSet.add(file.getName());
            }
        }
        files = new File("/Users/MayankMahajan/Desktop/EclipseWorkspace/LBJSpamDetector/data/spam/test/spam/").listFiles();
        for (File file : files) {
            if (file.isFile()) {
                testSet.add(file.getName());
            }
        }

        int[] interestingTokens = { 5, 10, 15, 20, 25, 30, 35, 40,45, 50 };
        for (int i = 0; i < interestingTokens.length; i++) {
            int accuracyHam = 0;
            int accuracySpam = 0;
            int totalHam = 0;
            int totalSpam = 0;
            for (String line : trainSet) {

                if (line.endsWith("spam.txt")) {
                    emailPath = "/Users/MayankMahajan/Desktop/EclipseWorkspace/LBJSpamDetector/data/spam/train/spam/"+line;
                }else{
                    emailPath = "/Users/MayankMahajan/Desktop/EclipseWorkspace/LBJSpamDetector/data/spam/train/ham/"+line;
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


    }

    // For TREC dataSet
    public static void indexFileRead_TREC() {
        String path = "C:\\Users\\Parag\\Desktop\\Project\\trec07p";
        String datapath = path + "\\data\\";
        String spamHamFile = path + "\\full\\index";
        List<String> spamHams = DataReader.readFile(spamHamFile);

        /**
         * 80% for train. 20% for test
         */
        Collections.shuffle(spamHams);
        List<String> trainSet = spamHams.subList(0,
                (int) (0.1 * spamHams.size()));
        List<String> testSet = spamHams.subList(
                (int) (0.1 * spamHams.size()) ,
                (int) (0.15 * spamHams.size()));
        String emailPath = null;
        int[] count = new int[2];
        NaiveBayes1 nbayes = new NaiveBayes1();
        for (String line : trainSet) {
            emailPath = datapath;
            String subPath = line.substring(line.lastIndexOf("/") + 1);
            emailPath += subPath;
            // emailPath =
            // "/Users/MayankMahajan/Desktop/UIUC Fall 2014/ML CS466/Project/Datasets/TREC/trec07p/full/../data/inmail.1";

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
        nbayes.calcSpamicity();
        /**
         * For dealing with rare words --> Smoothing
         */
        nbayes.correctedSpamicity();

        int[] interestingTokens = { 5, 10, 15, 20, 25, 30, 35, 40,45, 50 };
        for (int i = 0; i < interestingTokens.length; i++) {
            int accuracyHam = 0;
            int accuracySpam = 0;
            int totalHam = 0;
            int totalSpam = 0;
            for (String line : testSet) {

                emailPath = datapath;
                String subPath = line.substring(line.lastIndexOf("/") + 1);
                emailPath += subPath;

                // emailPath =
                // "/Users/MayankMahajan/Desktop/UIUC Fall 2014/ML CS466/Project/Datasets/TREC/trec07p/full/../data/inmail.68676";
                double p = nbayes.calcSpamProbability(
                        DataReader.readFile(emailPath), interestingTokens[i]);

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
    public static void parseEmail(String emailPath, boolean isSpam, NaiveBayes1 nbayes) {

        List<String> email = new ArrayList<String>();
        email = DataReader.readFile(emailPath);

        // Only text based
        if (isSpam) {
            nbayes.trainSpam(email);

        } else {
            nbayes.trainHam(email);
        }
    }

}
