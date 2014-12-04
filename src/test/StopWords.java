package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by Parag on 30-11-2014.
 */
public class StopWords {
    private HashSet<String> stopwords;
    private boolean used = true;
    public StopWords(){
        readFile();
    }
    public void readFile(){
        stopwords = new HashSet<String>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("C:\\Users\\Parag\\Desktop\\Project\\stopwords.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                stopwords.add(line);
            }
            br.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
    }
    public boolean isStopWord(String word){
        if(used)
            return stopwords.contains(word);
        else
            return false;
    }
}
