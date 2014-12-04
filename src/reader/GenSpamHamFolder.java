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
		String spamHamFile = "C:\\Users\\Parag\\Desktop\\Project\\trec07p\\full\\index";
		List<String> spamHams = DataReader.readFile(spamHamFile);
	
		
		
		
		List<String> trainSet = spamHams.subList(0,
				(int) (0.8 * spamHams.size()));
		
		List<String> testSet = spamHams.subList(
				(int) (0.8 * spamHams.size())+1, spamHams.size());
		
		copyFn(trainSet,"train");
		copyFn(testSet,"test");

	
	}
	
	public static void copyFn(List<String> set, String type){
		String path = "C:\\Users\\Parag\\Desktop\\Project\\trec07p\\full\\";
		String destPath = "C:\\Users\\Parag\\Desktop\\Project\\dataset\\"+type+"\\";
		for(String line: set){
			
			String subPath = line.substring(line.lastIndexOf(("/")));
			Path source = Paths.get(path+"data\\"+subPath);
			Path dest = null;
			if(line.startsWith("spam")){
				dest = 	Paths.get(destPath+"spam\\"+source.getFileName()+".spam.txt");
			}else{
				dest = 	Paths.get(destPath+"ham\\"+source.getFileName()+".ham.txt");
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
