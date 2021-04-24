package ca.pfv.spmf.algorithms.sequentialpatterns.FHUSPM;

import ca.pfv.spmf.tools.MemoryLogger;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FHUSPM_FNUB_Release {
    /**
     * the time the algorithm started
     */
    public long startTimestamp = 0;
    /**
     * the time the algorithm terminated
     */
    public long endTimestamp = 0;
    /**
     * the number of patterns generated
     */
    public long patternCount = 0;

    /**
     * writer to write the output file
     **/
    BufferedWriter writer = null;

    /**
     * the minUtility threshold
     **/
    BigDecimal minUtility = BigDecimal.ZERO;

    /**
     * the minSupport threshold
     **/
    int minSupport = 0;

    /**
     * num extend
     **/
    long numExtension = 0;

    /**
     * the input file path
     **/
    String input;

    NumberFormat formatter = new DecimalFormat("#0.0");

    /**
     * iCMap & SCMap
     **/
    HashMap<Integer, ArrayList<Integer>> iCMap;
    HashMap<Integer, ArrayList<Integer>> sCMap;

    /**
     * QSDB
     **/
    SULBigNumber qsdb;

    public FHUSPM_FNUB_Release() {
    }

    public void runAlgorithm(String input, String output, double minUtility, int minSupport) throws IOException, Exception {
        // reset maximum
        MemoryLogger.getInstance().reset();

        // input path
        this.input = input;

        // record the start time of the algorithm
        startTimestamp = System.currentTimeMillis();

        // create a writer object to write results to file
        writer = new BufferedWriter(new FileWriter(output));

        // save the minimum support threshold
        this.minSupport = minSupport;

        // check the memory usage again and close the file.
        MemoryLogger.getInstance().checkMemory();

        qsdb = new SULBigNumber();

        // Read DB
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
        String thisLine;

        int inputNo = 0;
        while ((thisLine = inputReader.readLine()) != null) {
            qsdb.addRecord(inputNo, thisLine);
            inputNo++;
        }

        inputReader.close();

        // Set minUtility
        this.minUtility = new BigDecimal("" + qsdb.dbUtility * minUtility);

        // Set minSupport
        this.minSupport = Math.max(minSupport, (int) (minUtility / qsdb.dbMaxSWU));

        System.out.println("Min utility: " + this.minUtility);
        System.out.println("Min support: " + this.minSupport);

        FHUSPM();

        // check the memory usage again and close the file.
        MemoryLogger.getInstance().checkMemory();
        // close output file
        writer.close();
        // record end time
        endTimestamp = System.currentTimeMillis();
    }

    public void FHUSPM() throws IOException {
        // Steps: 1 + 2
        var S = qsdb.db.keySet().stream().toArray(Integer[]::new);
        Arrays.sort(S);

        iCMap = new HashMap<Integer, ArrayList<Integer>>();
        for (var itemA : S) {
            iCMap.put(itemA, new ArrayList<>());
            for (var itemB : S) {
                if (itemA < itemB) {
                    iCMap.get(itemA).add(itemB);
                }
            }
        }


        sCMap = new HashMap<>();
        for (var itemA : S) {
            sCMap.put(itemA, new ArrayList<>());
            for (var itemB : S) {
                sCMap.get(itemA).add(itemB);
            }
        }

        for (var item: S) {
            MineFHUS(qsdb.get(item));
        }

        MemoryLogger.getInstance().checkMemory();
    }

    public void MineFHUS(SULBigNumber alpha) throws IOException {
        numExtension++;

        if (alpha.getSupport() < minSupport) return;

        if (new BigDecimal(alpha.calcFNub() + "").compareTo(minUtility) < 0) return;

        var avgUtility = alpha.calcAvgUtility();

        if (avgUtility.compareTo(minUtility) >= 0) {
            writeOutput(alpha, avgUtility);
        }

        var x = alpha.sequenceLastItem;

        for (var item: iCMap.get(x)) {
            MineFHUS(SULBigNumber.iExtend(alpha, qsdb.get(item)));
        }

        for (var item: sCMap.get(x)) {
            MineFHUS(SULBigNumber.sExtend(alpha, qsdb.get(item)));
        }

        MemoryLogger.getInstance().checkMemory();
    }

    public void writeOutput(SULBigNumber sequence) throws IOException {
        patternCount++;
        writer.write(sequence.sequenceString + " -1 #UTIL: " + formatter.format(sequence.calcAvgUtility()) + "\n");
    }

    public void writeOutput(SULBigNumber sequence, BigDecimal utility) throws IOException {
        patternCount++;
        writer.write(sequence.sequenceString + " -1 #UTIL: " + formatter.format(utility) + "\n");
    }

    public void printStatistics() {
        System.out.println("=============       FHUSPM_FNUB_Release       ===========");
        System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
        System.out.println(" Max Memory ~ " + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println(" Num extensions: " + numExtension);
        System.out.println(" Min Utility: " + minUtility);
        System.out.println(" Min Support: " + minSupport);
        System.out.println(" High-utility sequential pattern count : " + patternCount);
        System.out.println("========================================================");
    }
}
