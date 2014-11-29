package svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import randomprojection.RandomProjection;

public class SVMTest {

	private List<File> files;
	private String regex = "\\s+";
	private Map<String, Integer> wordMap;
    private boolean rp = false;
    private int reducedDimensionSize=10;
    private RandomProjection randomProjection;
    private boolean cf = true;
    private int ngram = 4;
     
	public SVMTest(){
		//wordMap = new HashMap<String, Integer>();
		files = new ArrayList<File>();
	}

	public void setRP(boolean rp){
        this.rp = rp;
    }
    public void setReducedDimensionSize(int size){
        this.reducedDimensionSize = size;
    }
	public void svmTrain(String directory){
		svm_problem prob = new svm_problem();
		try{
            if(cf)
                genCharMap(directory);
            else
			    genWordMap(directory);
			prob = genProblem(directory, cf);
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
		
		
		/**
		 * SET PARAMETERS
		 */
		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.NU_SVC;
		param.kernel_type = svm_parameter.LINEAR;

        param.degree = 1;
		param.nu = 0.1;
		param.gamma = 0.0001;
		param.eps = 0.001;
		param.C = 3.0;
		
		svm.svm_check_parameter(prob, param);
		svm_model model =  svm.svm_train(prob, param);
		try {
			svm.svm_save_model("spam_svm.model",model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected void genWordMap(String directory) throws IOException{
		 File d = new File(directory);
		 files = new ArrayList<File>();
        for (File f : d.listFiles()) {
            if (f.isDirectory()) {
                files.addAll(Arrays.asList(f.listFiles()));
            }
        }

        wordMap = new HashMap<String, Integer>(files.size());

        int index = 0;
        
        for(int fileId = 0;fileId<files.size();fileId++){
        	File file = files.get(fileId);
	        BufferedReader reader = new BufferedReader(new FileReader(file));
		 	String line = null;
		 	
		 	while ((line = reader.readLine()) != null) {
		 		
		 		for (String word : line.split(this.regex)){
                    String s = word.trim().toLowerCase();
		 			if(!wordMap.containsKey(s)){
		 				wordMap.put(s,index);
		 				index++;
		 			}
		 		}
		 	}

		 	reader.close();
        }
	}

    protected void genCharMap(String directory) throws IOException{
        File d = new File(directory);
        files = new ArrayList<File>();
        for (File f : d.listFiles()) {
            if (f.isDirectory()) {
                files.addAll(Arrays.asList(f.listFiles()));
            }
        }

        wordMap = new HashMap<String, Integer>(files.size());

        int index = 0;

        for(int fileId = 0;fileId < files.size(); fileId++){
            File file = files.get(fileId);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if(line.length() < ngram){
                    String s = line.toLowerCase();
                    if(!wordMap.containsKey(s)){
                        wordMap.put(s,index);
                        index++;
                    }
                }else {
                    for (int i = 0; i <= line.length() - ngram; i++) {
                        String s = line.substring(i, i + ngram).toLowerCase();
                        if (!wordMap.containsKey(s)) {
                            wordMap.put(s, index);
                            index++;
                        }
                    }
                }
            }
            reader.close();
        }
    }
	protected svm_problem genProblem(String directory, boolean cf) throws IOException{
		/**
		 * TRAINING
		 */
		System.out.println("[SVM TRAIN START]");
		
		svm_problem prob = new svm_problem();
		prob.x = new svm_node[files.size()][];
		
        prob.y = new double[files.size()];
          
        for(int fileId = 0;fileId<files.size();fileId++){
        	
        	TreeSet<Integer> indices = new TreeSet<Integer>();

        	File file = files.get(fileId);

	        String[] split = file.getPath().split("\\\\");
	        String label = split[split.length - 2];
	        if(label.equals("spam")){
	        	prob.y[fileId] = -1.0;
	        }else{
	        	prob.y[fileId] = +1.0;
	        }
	        BufferedReader reader = new BufferedReader(new FileReader(file));
		 	String line = null;
		 	if(cf) {
                while ((line = reader.readLine()) != null) {
                    if(line.length() < ngram){
                        String s = line.toLowerCase();
                        int idx = wordMap.get(s);
                        indices.add(idx);
                    }else {
                        for (int i = 0; i <= line.length() - ngram; i++) {
                            String s = line.substring(i, i+ngram).toLowerCase();
                            int idx = wordMap.get(s);
                            indices.add(idx);
                        }
                    }
                }//File read and stored
            }else{
                while ((line = reader.readLine()) != null) {
                    for (String word : line.split(this.regex)) {
                        int idx = wordMap.get(word.trim().toLowerCase());
                        indices.add(idx);
                    }
                }//File read and stored
            }
		 	
		 	int i=0;
		 	svm_node[] x = new svm_node[indices.size()+1];
            Iterator<Integer> it = indices.iterator();
			while (it.hasNext()) {
				x[i] = new svm_node();
				x[i].index = it.next();
				x[i].value = 1.0;
                i++;
			}
			x[i] = new svm_node();
			x[i].index = -1;
			x[i].value = 1.0;
			prob.x[fileId] = x;

		 	reader.close();
    	}
        if(rp){
            randomProjection = new RandomProjection(reducedDimensionSize, wordMap.size());
            randomProjection.convertToRandomProjection(prob.x);
        }
        prob.l = files.size();
        System.out.println("[SVM TRAIN END]");
		return prob;
	}
	
	public void svmPredict(String directory) throws IOException{
		

		svm_model model = svm.svm_load_model("spam_svm.model");
		File d = new File(directory);
		ArrayList<File> testFiles = new ArrayList<File>();
        
		for (File f : d.listFiles()) {
            if (f.isDirectory()) {
            	testFiles.addAll(Arrays.asList(f.listFiles()));
            }
        }   
		double []actualLabels = new double[testFiles.size()];
		double []predictedLables = new double[testFiles.size()];
		
        for(int fileId = 0;fileId<testFiles.size();fileId++){
			TreeSet<Integer> sortedIndices = new TreeSet<Integer>();
			File file = testFiles.get(fileId);
			String[] split = file.getPath().split("\\\\");
	        String label = split[split.length - 2];
	        if(label.equals("spam")){
	        	actualLabels[fileId] = -1.0;
	        }else{
	        	actualLabels[fileId] = +1.0;
	        }
			BufferedReader reader = new BufferedReader(new FileReader(file));
		 	
			String line = null;
            if(cf) {
                while ((line = reader.readLine()) != null) {
                    if(line.length() < ngram){
                        String s = line.toLowerCase();
                        if (wordMap.containsKey(s))
                            sortedIndices.add(wordMap.get(s));
                    }
                    for (int i = 0; i <= line.length() - ngram; i++) {
                        String s = line.substring(i, i+ngram).toLowerCase();
                        if (wordMap.containsKey(s))
                            sortedIndices.add(wordMap.get(s));
                    }
                }//File read and stored
            }else {
                while ((line = reader.readLine()) != null) {
                    for (String word : line.split(this.regex)) {
                        String s = word.trim().toLowerCase();
                        if (wordMap.containsKey(s))
                            sortedIndices.add(wordMap.get(s));
                    }
                }//File read and stored
            }
		 	svm_node[] x = new svm_node[sortedIndices.size()+1];
		 	int i = 0;
            Iterator<Integer> it = sortedIndices.iterator();
		 	while(it.hasNext()) {
				x[i] = new svm_node();
				x[i].index = it.next();
				x[i].value = 1.0;
				i++;
			}
            x[i] = new svm_node();
            x[i].index = -1;
            x[i].value = 1.0;

            if(rp) {
                svm_node[] rp_x = randomProjection.calculateRandomProjectionNode(x);
                predictedLables[fileId] = svm.svm_predict(model,rp_x);
            }else{
                predictedLables[fileId] = svm.svm_predict(model,x);
            }
		 	reader.close();
        }
        
        /**
         * Calculate accuracy
         */
		int correct = 0;
		int total = actualLabels.length;
		double error = 0;
		
        for(int i=0;i<predictedLables.length;i++){
        	if(actualLabels[i] ==  predictedLables[i]){
        		correct++;
        	}
        }

        System.out.println("Accuracy = "+(double)correct/total*100+
				 "% ("+correct+"/"+total+") (classification)\n");
	}
}

/*
* while ((line = reader.readLine()) != null) {

		 		for (String word : line.split(this.regex)){
		 			sortedMap.put(wordMap.get(word.trim().toLowerCase()), word.trim().toLowerCase());
		 		}
		 	}//File read and stored

		 	int i=0;
		 	svm_node[] x = new svm_node[sortedMap.size()];
			for (Map.Entry<Integer, String> entry : sortedMap.entrySet()) {
				x[i] = new svm_node();
				x[i].index = entry.getKey();
				x[i].value = 1.0;
				i++;
			}*/