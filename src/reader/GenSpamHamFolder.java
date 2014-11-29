package reader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GenSpamHamFolder {
	
	public static void main(String []args){
			indexFileReader();
		
	}
	
	public static void indexFileReader() {
		String spamHamFile = "/Users/MayankMahajan/Desktop/UIUC Fall 2014/ML CS466/Project/Datasets/TREC/trec07p/full/index";
		List<String> spamHams = DataReader.readFile(spamHamFile);
	
		
		
		
		List<String> trainSet = spamHams.subList(0,
				(int) (0.8 * spamHams.size()));
		
		List<String> testSet = spamHams.subList(
				(int) (0.8 * spamHams.size())+1, spamHams.size());
		
		copyFn(trainSet,"train");
		copyFn(testSet,"test");

	
	}
	
	public static void copyFn(List<String> set, String type){
		String path = "/Users/MayankMahajan/Desktop/UIUC Fall 2014/ML CS466/Project/Datasets/TREC/trec07p/full/";
		String destPath = "/Users/MayankMahajan/Desktop/EclipseWorkspace/SpamDetector/dataset/"+type+"/";
		for(String line: set){
			
			String subPath = line.substring(line.indexOf(".."));
			Path source = Paths.get(path+subPath);
			Path dest = null;
			if(line.startsWith("spam")){
				dest = 	Paths.get(destPath+"spam/"+source.getFileName()+".spam.txt");
			}else{
				dest = 	Paths.get(destPath+"ham/"+source.getFileName()+".ham.txt");	
			}
			try {
				Files.copy(source, dest);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
			}
		}
	}
	
	
}
