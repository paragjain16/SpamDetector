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
    private boolean cf = false;
    private int ngram = 4;
    private ArrayList<Integer> randomNodes;
    private int numDataPoints = 30;

	public SVMTest(){
		//wordMap = new HashMap<String, Integer>();
		files = new ArrayList<File>();
        randomNodes = new ArrayList<Integer>(numDataPoints);
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
		param.kernel_type = svm_parameter.RBF;

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
            int totalWords=0;
	        BufferedReader reader = new BufferedReader(new FileReader(file));
		 	String line = null;
		 	
		 	while ((line = reader.readLine()) != null){
		 		for (String word : line.split(this.regex)){
                    String s = word.trim();//.toLowerCase();
		 			if(!wordMap.containsKey(s)){
		 				wordMap.put(s,index);
		 				index++;
		 			}
		 		}
		 	}

		 	reader.close();
        }
        System.out.println("Number of features "+index);
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
                    String s = line;//line.toLowerCase();
                    if(!wordMap.containsKey(s)){
                        wordMap.put(s,index);
                        index++;
                    }
                }else {
                    for (int i = 0; i <= line.length() - ngram; i++) {
                        String s = line.substring(i, i + ngram);//.toLowerCase();
                        if (!wordMap.containsKey(s)) {
                            wordMap.put(s, index);
                            index++;
                        }
                    }
                }
            }
            System.out.println("Number of features "+index);
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
                        String s = line;//line.toLowerCase();
                        int idx = wordMap.get(s);
                        indices.add(idx);
                    }else {
                        for (int i = 0; i <= line.length() - ngram; i++) {
                            String s = line.substring(i, i+ngram);//.toLowerCase();
                            int idx = wordMap.get(s);
                            indices.add(idx);
                        }
                    }
                }//File read and stored
            }else{
                while ((line = reader.readLine()) != null) {
                    for (String word : line.split(this.regex)) {
                        int idx = wordMap.get(word.trim());//.toLowerCase());
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
            chooseDataPoints(prob.x);
            System.out.println("Pairwise distances before random projection ");
            calculateDistance(randomNodes, prob.x);
            randomProjection = new RandomProjection(reducedDimensionSize, wordMap.size());
            randomProjection.convertToRandomProjection(prob.x);
            System.out.println("Pairwise distances after random projection ");
            calculateDistance(randomNodes, prob.x);
        }
        prob.l = files.size();
        System.out.println("[SVM TRAIN END]");
		return prob;
	}

    public void chooseDataPoints(svm_node[][] dataNodes){
        Random r = new Random();
        int size = dataNodes.length;
        int i = numDataPoints;
        while( i > 0 ){
            int index = r.nextInt(size);
            randomNodes.add(index);
            i--;
        }
    }
    public void calculateDistance(ArrayList<Integer> dataNodes, svm_node[][] nodes){
        int size = dataNodes.size();
        for(int i = 0; i < size/2; i++){
            System.out.println(/*"Distance between Data Node "+(i+1)+" with dimensionality "+nodes[dataNodes.get(i)].length
                    +" and data Data Node "+(size-i)+" with dimensionality "+nodes[dataNodes.get(size-i-1)].length+" is "*/
                    distanceBetweenVector(nodes[dataNodes.get(i)], nodes[dataNodes.get(size-i-1)]));
        }
    }

    public double distanceBetweenVector(svm_node[] v1, svm_node[] v2){
        double distance = 0.0;
        int i=0;
        int j=0;
        int index1 = 0;
        int index2 = 0;
        double value1= 0.0;
        double value2 = 0.0;
        while(i < v1.length-1 && j < v2.length-1){
            index1 = v1[i].index;
            value1 = v1[i].value;
            index2 = v2[j].index;
            value2 = v2[j].value;
            if(index1 == index2){
                distance += ((value1- value2)*(value1- value2));
                i++;
                j++;
            }else if(index1 > index2){
                distance += ((0 - value2)*(0 - value2));
                j++;
            }else{
                distance += ((value1 - 0)*(value1 - 0));
                i++;
            }
        }
        if( i < v1.length - 1){
            while(i < v1.length -1){
                distance += (v1[i].value * v1[i].value);
                i++;
            }
        }
        if( j < v2.length - 1){
            while(j < v2.length -1){
                distance += (v2[j].value * v2[j].value);
                j++;
            }
        }
        return Math.sqrt(distance);
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
                        String s = line;//.toLowerCase();
                        if (wordMap.containsKey(s))
                            sortedIndices.add(wordMap.get(s));
                    }
                    for (int i = 0; i <= line.length() - ngram; i++) {
                        String s = line.substring(i, i+ngram);//.toLowerCase();
                        if (wordMap.containsKey(s))
                            sortedIndices.add(wordMap.get(s));
                    }
                }//File read and stored
            }else {
                while ((line = reader.readLine()) != null) {
                    for (String word : line.split(this.regex)) {
                        String s = word.trim();//.toLowerCase();
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
        int totalspam = 0;
        int totalham = 0;
		int spam =0;
        int ham =0;

        for(int i=0;i<predictedLables.length;i++){
            if(actualLabels[i] == -1.0){
                totalspam++;
                if(predictedLables[i] == -1.0)
                    spam++;
            }else{
                totalham++;
                if(predictedLables[i] == 1.0)
                    ham++;
        	}
        }

        System.out.println("Spam Accuracy = "+(double)spam/totalspam*100+
				 "% ("+spam+"/"+totalspam+") (classification)\n");
            System.out.println("Ham Accuracy = "+(double)ham/totalham*100+
                    "% ("+ham+"/"+totalham+") (classification)\n");
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