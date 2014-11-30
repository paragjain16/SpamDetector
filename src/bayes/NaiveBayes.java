package bayes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import test.StopWords;

public class NaiveBayes {
    private HashMap<String, Integer> spamMap;
    private HashMap<String, Integer> hamMap;

    private HashMap<String, Double> spamicityMap;

    private double numberOfSpam;
    private double numberOfHam;
    private StopWords stopWords;
    private final String regex = "\\W+";
	//private final String regex = "[^a-zA-Z0-9$!-%]";
    //private final String regex = "\\s+";

    public NaiveBayes() {
        spamMap = new HashMap<String, Integer>();
        hamMap = new HashMap<String, Integer>();
        spamicityMap = new HashMap<String, Double>();
        numberOfSpam = 0;
        numberOfHam = 0;
        stopWords = new StopWords();
        System.out.println("Regex used for delimiter = "+regex);
    }

    public Map<String, Double> getSpamicityMap(){
        return this.spamicityMap;
    }

    public double getCountSpamMsg() {
        return this.numberOfSpam;
    }

    public double getCountHamMsg() {
        return this.numberOfHam;
    }

    public HashMap<String, Integer> getSpamMap(){
        return this.spamMap;
    }
    public HashMap<String, Integer> getHamMap(){
        return this.hamMap;
    }

    /**
     * TRAINING
     *
     * @param file
     */
    public void trainSpam(String file) {

        numberOfSpam++;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String noHtmlText = line;
                String[] arr = noHtmlText.split(regex);
                String prefix = getPrefix(line);
                for (String word : arr){
                    if(stopWords.isStopWord(word))
                        continue;
                    word = prefix+word;
                    this.countSpam(word);
                }
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
        /*
        for (String line : email) {
            Document doc = Jsoup.parse(line);

            String noHtmlText = line;
//			 noHtmlText = doc.text();

            String[] arr = noHtmlText.split(regex);

//			for(int i=0;i<arr.length-1;i++){
//				this.countSpam(arr[i]+arr[i+1]);
//			}
            String prefix = getPrefix(line);
            for (String word : arr){
                word = prefix+word;
                this.countSpam(word);
            }
        }*/
    }

    public void trainHam(String file) {
        numberOfHam++;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String noHtmlText = line;
                String[] arr = noHtmlText.split(regex);
                String prefix = getPrefix(line);
                for (String word : arr){
                    if(stopWords.isStopWord(word))
                        continue;
                    word = prefix+word;
                    this.countHam(word);
                }
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
        /*
        for (String line : email) {
            Document doc = Jsoup.parse(line);
            String noHtmlText = line;
//			 noHtmlText = doc.text();

            String[] arr = noHtmlText.split(regex);

//			for(int i=0;i<arr.length-1;i++){
//				this.countHam(arr[i]+arr[i+1]);
//			}
            String prefix = getPrefix(line);
            for (String word : arr){
                word = prefix+word;
                this.countHam(word);
            }
        }*/
    }

    public void countSpam(String word) {

        if (spamMap.containsKey(word)) {
            spamMap.put(word, spamMap.get(word) + 1);
        } else {
            spamMap.put(word, 1);
        }
    }

    public void countHam(String word) {
        if (hamMap.containsKey(word)) {
            hamMap.put(word, hamMap.get(word) + 1);
        } else {
            hamMap.put(word, 1);
        }
    }

    public void calcSpamicity() {

        for (Map.Entry<String, Integer> entry : spamMap.entrySet()) {
            String word = entry.getKey();
            double spamCount = entry.getValue();
            double spamicity = 0.99;// default for word only occuring in spam

            double hamCount = 0;
            if (hamMap.containsKey(word)) {
                hamCount = hamMap.get(word);

                double denom = (spamCount / numberOfSpam) + (hamCount / numberOfHam);
                if (denom != 0)
                    spamicity = (spamCount / numberOfSpam) / denom;

            }

            spamicity = Math.max(0.01, Math.min(0.99, spamicity));
            spamicityMap.put(word, spamicity);
        }
        double spamicity = 0.01;// default for HAM msgs
        for (Map.Entry<String, Integer> entry : hamMap.entrySet()) {
            String word = entry.getKey();
            if (!spamicityMap.containsKey(word)) {
                // Word only present in HAM messages for training set
                spamicityMap.put(word, spamicity);
            }
        }

    }

    // Smoothing process
    public void correctedSpamicity() {

        double spamPrior = 0.5;
        double s = 3;

        for (Map.Entry<String, Double> entry : spamicityMap.entrySet()) {
            String word = entry.getKey();
            double n = 0;
            if (spamMap.containsKey(word))
                n += spamMap.get(word);
            if (hamMap.containsKey(word))
                n += hamMap.get(word);

            double correctedSpamicity = (s * spamPrior + n
                    * spamicityMap.get(word))
                    / (s + n);
            spamicityMap.put(word, correctedSpamicity);
        }
    }

    /**
     * END TRAINING
     */

    public double calcSpamProbability(List<String> email, int tokens) {

        SortedSet<Map.Entry<String, Double>> sortedSet = new TreeSet<Map.Entry<String, Double>>(

                new Comparator<Map.Entry<String, Double>>() {
                    @Override
                    public int compare(Map.Entry<String, Double> e1, Map.Entry<String, Double> e2) {
                        return e2.getValue().compareTo(e1.getValue());
                    }
                });
        TreeMap<String, Double> myMap = new TreeMap<String, Double>();

        // Word that doesn't occur in the hash table of word probabilities.
        double pWordSpamDefault = 0.4;
        double eta = 0.0;

        for (String line : email) {
            Document doc = Jsoup.parse(line);
//			String noHtmlText = doc.text();
            String noHtmlText = line;
            String[] arr = noHtmlText.split(regex);

            String prefix = getPrefix(line);

            for (String word : arr) {
                if(stopWords.isStopWord(word))
                    continue;
                double p_word = pWordSpamDefault;
                word = prefix+word;
                if (spamicityMap.containsKey(word))
                    p_word = spamicityMap.get(word);

                myMap.put(word, Math.abs(0.5-p_word));
            }
        }
        //Printing map
//		printSortedMap(myMap);

        sortedSet.addAll(myMap.entrySet());

        //Print sortedSet
//		printSortedSet(sortedSet);

        int noOfInterestingTokens = tokens;
        for(Iterator<Entry<String, Double>> it = sortedSet.iterator() ; it.hasNext() ; ){

            Map.Entry<String, Double> entry = (Entry<String, Double>) it.next();
            double p_word = pWordSpamDefault;

            if(spamicityMap.containsKey(entry.getKey()))
                p_word = spamicityMap.get(entry.getKey());

            double p_log = Math.log(p_word);
            double one_minus_p_log = Math.log(1 - p_word);
            eta = eta + (one_minus_p_log) - p_log;

            if(noOfInterestingTokens-- <0)
                break;
        }
        double combinedProbability = 1 / (1 + Math.pow(Math.E, eta));
        return combinedProbability;
    }

    public String getPrefix(String emailLine){
        String prefix = "";
        if(emailLine.startsWith("From"))
            return "From*";
        else if(emailLine.startsWith("Subject"))
            return "Subject*";
        else if(emailLine.startsWith("Received"))
            return "Rec*";
        else if(emailLine.startsWith("To"))
            return "To*";

        return prefix;
    }

    public void printSortedMap(SortedMap<String, Double> sortedMap){
        for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
            System.out.println(entry.getKey()+" :-: "+entry.getValue()+" :-:"+ spamicityMap.get(entry.getKey()));

        }
    }

    public void printSortedSet(SortedSet<Map.Entry<String, Double>> sortedSet){
        for(Iterator<Entry<String, Double>> it = sortedSet.iterator() ; it.hasNext() ; ){
            Map.Entry<String, Double> entry = (Entry<String, Double>) it.next();
            System.out.println(entry.getKey()+" :-: "+entry.getValue()+" :-:"+ spamicityMap.get(entry.getKey()));

        }
    }

}
