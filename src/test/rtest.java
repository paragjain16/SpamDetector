package test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Parag on 30-11-2014.
 */
public class rtest {
    public static void main(String[] args) {

        BufferedReader br;
        /*try {
            br = new BufferedReader(new FileReader("C:/Users/Parag/Desktop/Project/trec07p/data/inmail.37704"));
            String line;
            while ((line = br.readLine()) != null) {

                Document doc = Jsoup.parse(line);
                line = doc.text();
                System.out.println(line);
            }
        }catch(IOException e){
            e.printStackTrace();
        }*/
        String line = "23 2";
        if(line.matches("[0-9 ]*"))
            System.out.println("All numeric");
        if(line.isEmpty())
            System.out.println("First Check"+line.length());
        line = line.trim();
        if(line.isEmpty())
            System.out.println("Second Check");
    }
}
