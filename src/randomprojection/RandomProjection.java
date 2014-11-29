package randomprojection;

import libsvm.svm_node;

import java.util.Map;
import java.util.Random;

/**
 * Created by Parag on 27-11-2014.
 */
public class RandomProjection {
    // Dimensionality to reduce to
    private int k;
    //Random projection matrix
    private double[][] rp_matrix;
    // Original dimensionality size
    private int n;
    //Seed value for random number generator - default 42
    private long seed = 42;
    //Random number generator
    private Random random;
    //Feature array
    //private svm_node[] svm_nodes;

    public RandomProjection(int k, int n){
        this.k = k;
        this.n = n;
        random = new Random();
        random.setSeed(seed);
        rp_matrix = new double[k][n];
        populateRandomMatrix();
    }
    public RandomProjection(int k, int n, int seed){
        this.k = k;
        this.n = n;
        this.seed = seed;
        random = new Random();
        random.setSeed(seed);
        rp_matrix = new double[k][n];
        populateRandomMatrix();
    }

    private void populateRandomMatrix(){
        for(int i=0; i<k; i++){
            for(int j=0; j<n; j++){
                rp_matrix[i][j] = generateValuesForRPMatrix();
            }
        }
    }
    // Get -1, 1 and 0 with 1/6, 1/6 and 2/3 probability respectively
    private double generateValuesForRPMatrix(){
        int val = (int) Math.floor(random.nextDouble() * 6);
        int[] nums = {1,1,4};
        int i=0;
        for (i = 0; i < nums.length; i++) {
            val -= nums[i];
            if (val < 0) {
                break;
            }
        }
        if(i==0)
            return -1;
        else if(i==1)
            return 1;
        else
            return 0;
    }

    public void convertToRandomProjection(svm_node[][] x){
        //Iterate through all files/documents/instances
        for(int i=0; i < x.length; i++) {
            //map feature vector of each file to lower dimension
            x[i] = calculateRandomProjectionNode(x[i]);
        }
    }

    public svm_node[] calculateRandomProjectionNode(svm_node[] svm_nodes){
        svm_node[] new_node = new svm_node[k+1];
        // loop through new feature set and fill in the appropriate values
        for(int i=0; i<new_node.length-1; i++){
            new_node[i] = new svm_node();
            new_node[i].index = i;
            // calculate value for this feature using random projection matrix
            new_node[i].value = computeRPValue(i ,svm_nodes);
        }
        new_node[k] = new svm_node();
        new_node[k].index = -1;
        new_node[k].value = 1.0;
        return new_node;
    }

    private double computeRPValue(int index, svm_node[] svm_nodes){
        double result =0.0;
        for(int i=0; i < svm_nodes.length-1; i++){
            int attrIndex = svm_nodes[i].index;
            double attrValue = svm_nodes[i].value;
            result += rp_matrix[index][attrIndex] * attrValue;
        }
        return result;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }
}
