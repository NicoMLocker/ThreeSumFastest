import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;

public class ThreeSumFastest {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    /* define constants */
    static long MAXVALUE =  1000;
    static long MINVALUE = -1000;
    static int numberOfTrials = 10;  // can change 1-1000 for testing , set at 1000 for actual test
    static int MAXINPUTSIZE  = (int) Math.pow(2,12);  // can change from 29 to 10-20 for testing
    static int MININPUTSIZE  =  1;

    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time
    static String ResultsFolderPath = "/home/nicolocker/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;
    static double[] avgTimeList = new double[2];
    static double ratio = 0;

    public static void main(String[] args) {

        verifyThreeSum();

        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("ThreeSumFastest-Exp1.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("ThreeSumFastest-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("ThreeSumFastest-Exp3.txt");
    }

    public static long[] createRandomIntegerList(int size){
        long[] newList = new long[size];
        for(int j = 0; j < size; j++){
            newList[j] = (long)(MAXVALUE + Math.random() *  (MAXVALUE - MINVALUE));
        }
        return newList;
    }

    public static void verifyThreeSum(){
        long[] testList1 = {1,2,3,4,5,6};
        System.out.println("\nTest List: " + Arrays.toString(testList1));
        threeSum(testList1);
        System.out.println("Should equal 0.");
        System.out.println("Three Sum Count: " +  threeSum(testList1) + "\n");

        long[] testList2 = {324110,-442472,626686,-157678,508681,123414,-77867,155091,129801,287381};
        System.out.println("Test List: " + Arrays.toString(testList2));
        threeSum(testList2);
        System.out.println("Should equal 1.");
        System.out.println("Three Sum Count: " +  threeSum(testList2) + "\n");

        long[] testList3 = {1,2,6, -3, 12, -18};
        System.out.println("Test List: " + Arrays.toString(testList3));
        threeSum(testList3);
        System.out.println("Should equal 2.");
        System.out.println("Three Sum Count: " +  threeSum(testList3) + "\n");

    }

    public static void runFullExperiment(String resultsFileName){

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch(Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file "+ResultsFolderPath+resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize       AverageTime              Ratio"); // # marks a comment in gnuplot data
        resultsWriter.flush();
        /* for each size of input we want to test: in this case starting small and doubling the size each time */

        for(int inputSize=MININPUTSIZE;inputSize<=MAXINPUTSIZE; inputSize*=2) {
            // progress message...
            System.out.println("Running test for input size "+inputSize+" ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of randomly spaced integers in ascending sorted order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            // but we will randomly generate the search key for each trial
            System.out.print("    Generating test data...");
            long[] testList = createRandomIntegerList(inputSize);
            System.out.println("...done.");
            System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();


            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopwatch methods themselves
            //BatchStopwatch.start(); // comment this line if timing trials individually

            // run the tirals
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random key to search in the range of a the min/max numbers in the list
                //    long testSearchKey = (long) (0 + Math.random() * (testList[testList.length-1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                //    long foundIndex = binarySearch(testSearchKey, testList);

                ThreeSumFastest.threeSum(testList);

                batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }

            //batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually

            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double)numberOfTrials; // calculate the average time per trial in this batch

            /* put the averageTimePerTrialBatch into an array for the ratio*/
            if(avgTimeList[0] == 0){
                avgTimeList[0] = averageTimePerTrialInBatch;  // set first averageTime to first spot in array
                //System.out.println("\nINDEX 0\n");  // used for testing
                //System.out.println(avgTimeList[0]); // used for testing
            } else{
                avgTimeList[1] = averageTimePerTrialInBatch; // set next averageTime to second spot in array
                //System.out.println("\nINDEX 0\n");  //used for testing
                //System.out.println(avgTimeList[0]); //used for testing
                //System.out.println("\nINDEX 1\n");  //used for testing
                //System.out.println(avgTimeList[1]);  //used for testing
                double ratio = avgTimeList[1]/avgTimeList[0];  // divide the larger time by the smaller time to get the ratio
                avgTimeList[0] = avgTimeList[1];              // set the current biggest averageTime to first array spot for next test
                //System.out.println(ratio);  //used for testing

                /* print data for this size of input */
                resultsWriter.printf("%12d  %15.2f %15.2f\n",inputSize, averageTimePerTrialInBatch, ratio); // might as well make the columns look nice
                resultsWriter.flush();
                System.out.println(" ....done.");
            }
        }
    }

    public static ArrayList<ArrayList<Long>> threeSum (long[] list){
        int n = list.length;
        ArrayList<ArrayList<Long>> result = new ArrayList<ArrayList<Long>>();  //array to store results in
        Arrays.sort(list);      //sort list first

        for(int i = 0; i < n - 2; i++){
            if(list[i] > 0) break;
            if(i == 0 || list[i] > list[i - 1]){
                long target = 0 - list[i];
                int start = i + 1;
                int end = n - 1;
                while(start < end) {
                    if(list[start] + list[end] == target) {
                        ArrayList<Long> elem = new ArrayList<Long>();
                        elem.add(list[i]);
                        elem.add(list[start]);
                        elem.add(list[end]);

                        result.add(elem);
                        start++;
                        end--;

                        while (start < end && list[end + 1] == list[end]){
                            end--;
                        }
                        while (start < end && list[start - 1] == list[start]){
                            start++;
                        }
                    } else {
                        if(list[start] + list[end] > target){
                            end--;
                        } else {
                            start++;
                        }
                    }
                }
            }
        }
        return result;
    }
}



