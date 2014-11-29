package reader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataReader {

	public static List<String> readFile(String file) {
		List<String> lines = new ArrayList<String>();

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));

			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}

		return lines;
	}


	public static List<String> readFileWithEncoding(String filePath,
			String encoding) {
		List<String> lines = new ArrayList<String>();
		try {
			lines = Files.readAllLines(Paths.get(filePath),
					Charset.forName(encoding));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return lines;
	}
	
	public static void writeToFile(PrintWriter writer, String str){
		writer.println(str);
	}
}
