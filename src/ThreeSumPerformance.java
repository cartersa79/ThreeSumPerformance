import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;

public class ThreeSumPerformance {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    /* define constants */
    static long MAXVALUE = 2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 20;  // # of trials to run, more trials 'smooths out' quirks caused by random data
    static int MAXINPUTSIZE = (int) Math.pow(2, 14); // CPU limited...as expected, times got long, fast!
    static int MININPUTSIZE = 1;
    // static int SIZEINCREMENT =  10000000; // not using this since we are doubling the size each time

    static String ResultsFolderPath = "/home/steve/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    public static void main(String[] args) {
        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("ThreeSum-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("ThreeSum-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("ThreeSum-Exp3.txt");

        // verify that the algorithm works
        System.out.println("");
        System.out.println("----Verification Test----");
        boolean is_valid = verifyThreeSum();
        System.out.print(is_valid);


    }

    static void runFullExperiment(String resultsFileName) {
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#InputSize    AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for (int inputSize = MININPUTSIZE; inputSize <= MAXINPUTSIZE; inputSize *= 2) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of random integers in random order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            //System.out.print("    Generating test data...");
            //long[] testList = createRandomIntegerList(inputSize);
            //System.out.println("...done.");
            //System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            System.gc();

            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopWatch methods themselves
            BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random list of integers each trial
                long[] testList = createRandomIntegerList(inputSize);

                // generate a random key to search in the range of a the min/max numbers in the list
                // long testSearchKey = (long) (0 + Math.random() * (testList[testList.length - 1]));
                /* force garbage collection before each trial run so it is not included in the time */
                // System.gc();

                TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                long randomArray = threeSum(testList);
                batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            //batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            /* print data for this size of input */
            resultsWriter.printf("%12d  %15.2f \n", inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }


    // implement 'brute force' threeSum algorithm
    public static long threeSum(long[] a) {
        // count triples that sum to 0
        int n = a.length;  // get number of items in array
        int count = 0;  // initialize the counter that keeps track of valid threeSum combinations
        for (int i = 0; i < n; i++)                 // three nested loops to try every combination
            for (int j = i + 1; j < n; j++)         // of array values
                for (int k = j + 1; k < n; k++)
                    if (a[i] + a[j] + a[k] == 0)    // if the values sum to zero, increment count
                        count++;
        return (long)count;  // return number of triplets that sum to zero
    }

    // generate a random list of values to test against
    public static long[] createRandomIntegerList(int size) {
        long[] newList = new long[size];
        // randomly picks a value between -25000 and 25000
        // this range was chosen to help ensure that valid triplets will exist
        for (int j = 0; j < size; j++) {
            newList[j] = (long) ((50000 * Math.random()) - 25000);
        }
        return newList;  // return the list to caller
    }

    // verifies the threeSum function by using a known list with valid combinations
    private static boolean verifyThreeSum() {
        long[] array1 = new long[]{ 1,2,-3,10,11,-21,50,51,-101 };  // 3 valid combinations
        long[] array2 = new long[]{ 1,2,3,10,11,21,50,51,101 };  // no valid combinations
        long combinations1 = threeSum(array1);  // number of valid combinations
        long combinations2 = threeSum(array2);  // number of valid combinations
        if ((combinations1 > 0) && (combinations2 == 0)){
            System.out.println("The algorithm successfully evaluated the test arrays.");
            System.out.println("Array1 = " + combinations1 + ", Array2 = " + combinations2);
            return true;
        }
        else{
            System.out.println("The algorithm failed to correctly process the test arrays.");
            return false;
        }

    }
}
