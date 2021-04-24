package ca.pfv.spmf.algorithms.sequentialpatterns.FHUSPM;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;

public class SULBigNumber {
    boolean isDB = true;

    String sequenceString = "";
    int sequenceSize = 0;
    Integer sequenceLastItem = 0;
    int sequenceMaxRemainSize = 0;

    long dbUtility = 0;
    long dbMaxSWU = 0;
    public HashMap<Integer, HashMap<Integer, InputSequence>> db = new HashMap<>(); // item - input - info

    public SULBigNumber() {
    }

    public SULBigNumber(SULBigNumber other) {
        this.isDB = other.isDB;
        this.sequenceString = new String(other.sequenceString);
        this.sequenceSize = other.sequenceSize;
        this.sequenceLastItem = other.sequenceLastItem;
        this.sequenceMaxRemainSize = other.sequenceMaxRemainSize;

        this.dbUtility = other.dbUtility;
        this.dbMaxSWU = other.dbMaxSWU;
        this.db = new HashMap<>();
        for (var keyVal : other.db.entrySet()) {
            db.put(keyVal.getKey(), new HashMap<>(keyVal.getValue()));
        }
    }

    public void addRecord(int inputIndex, String record) throws Exception {
        if (!isDB) {
            throw new Exception("Can't add record to Sequence");
        }

        if (record.length() == 0 || record.charAt(0) == '#') return;

        String[] tokens = record.split(" ");

        var utilityString = tokens[tokens.length - 1];
        int swu = Integer.parseInt(utilityString.substring(utilityString.indexOf(':') + 1, utilityString.length()));
        if (dbMaxSWU < swu) {
            dbMaxSWU = swu;
        }

        Integer rem = swu;

        int pos = 0;
        int itemPos = 0;

        for (int i = 0; i < tokens.length - 3; i++) {
            String token = tokens[i];

            if (token.length() == 0) continue;

            if (token.charAt(0) == '-') {
                pos++;
                continue;
            }


            int lefBracket = token.indexOf('[');
            int rightBracket = token.indexOf(']');

            Integer item = Integer.parseInt(token.substring(0, lefBracket));
            Integer utility = Integer.parseInt(token.substring(lefBracket + 1, rightBracket));
            dbUtility += utility;

            if (db.get(item) == null) {
                db.put(item, new HashMap<Integer, InputSequence>());
            }

            if (db.get(item).get(inputIndex) == null) {
                db.get(item).put(inputIndex, new InputSequence());
            }

            InputSequence inputSequence = db.get(item).get(inputIndex);

            rem = rem - utility;
            inputSequence.utility = swu;
            inputSequence.mapInfo.put(pos, new Info(pos, itemPos, new BigInteger(utility.toString()), new BigInteger(rem.toString()), new BigInteger("1")));
            itemPos++;
        }

        for (var itemMap : db.values()) {
            InputSequence inputSequence = itemMap.get(inputIndex);
            if (inputSequence == null) continue;

            itemMap.get(inputIndex).length = itemPos;
            itemMap.get(inputIndex).size = pos;
        }
    }

    public SULBigNumber get(int item) {
        var data = db.get(item);

        if (data == null) return null;

        SULBigNumber sul = new SULBigNumber();
        sul.sequenceString = "" + item;
        sul.db.put(item, data);
        sul.sequenceLastItem = item;
        sul.isDB = false;
        sul.sequenceSize = 1;
        sul.sequenceMaxRemainSize = 10000;

        return sul;
    }

    public static SULBigNumber sExtend(SULBigNumber alpha, SULBigNumber beta) {
        var alphaMap = alpha.db.get(alpha.sequenceLastItem);
        var betaMap = beta.db.get(beta.sequenceLastItem);

        HashMap<Integer, InputSequence> result = new HashMap<>();
        int maxRemainSize = 0;
        for (var input : alphaMap.keySet()) {
            var betaInputSequence = betaMap.get(input);
            if (betaInputSequence == null) continue;

            var alphaInputSequence = alphaMap.get(input);

            for (var alphaItem : alphaInputSequence.mapInfo.values()) {
                for (var betaItem : betaInputSequence.mapInfo.values()) {
                    if (alphaItem.pos < betaItem.pos) {
                        Info newInfo = new Info(
                                betaItem.pos,
                                betaItem.itemPos,
                                alphaItem.sumU.add(betaItem.sumU.multiply(new BigInteger(alphaItem.no.toString()))),
                                betaItem.remU,
                                alphaItem.no
                        );

                        if (result.get(input) == null) {
                            InputSequence newInputSequence = new InputSequence();
                            newInputSequence.utility = alphaInputSequence.utility;
                            newInputSequence.length = alphaInputSequence.length;
                            newInputSequence.size = alphaInputSequence.size;
                            result.put(input, newInputSequence);
                        }

                        if (result.get(input).mapInfo.get(newInfo.pos) == null) {
                            result.get(input).mapInfo.put(newInfo.pos, newInfo);
                        } else {
                            var curInfo = result.get(input).mapInfo.get(newInfo.pos);
                            curInfo.sumU = curInfo.sumU.add(newInfo.sumU);
                            curInfo.no = curInfo.no.add(newInfo.no);
                        }

                        if (alphaInputSequence.size - newInfo.pos > maxRemainSize) {
                            maxRemainSize = alphaInputSequence.size - newInfo.pos;
                        }
                    }
                }
            }
        }

        SULBigNumber sul = new SULBigNumber();
        sul.sequenceLastItem = beta.sequenceLastItem;
        sul.sequenceString = alpha.sequenceString + " -1 " + beta.sequenceString;
        sul.db.put(sul.sequenceLastItem, result);
        sul.isDB = false;
        sul.sequenceSize = alpha.sequenceSize + 1;
        sul.sequenceMaxRemainSize = maxRemainSize;

        return sul;
    }

    public static SULBigNumber iExtend(SULBigNumber alpha, SULBigNumber beta) {
        var alphaMap = alpha.db.get(alpha.sequenceLastItem);
        var betaMap = beta.db.get(beta.sequenceLastItem);

        HashMap<Integer, InputSequence> result = new HashMap<>();
        int maxRemainSize = 0;

        for (var input : alphaMap.keySet()) {
            var betaInputSequence = betaMap.get(input);
            if (betaInputSequence == null) continue;

            var alphaInputSequence = alphaMap.get(input);

            for (var pos : alphaInputSequence.mapInfo.keySet()) {
                var betaItem = betaInputSequence.mapInfo.get(pos);
                if (betaItem == null) continue;

                var alphaItem = alphaInputSequence.mapInfo.get(pos);
                Info newInfo = new Info(betaItem.pos,
                        betaItem.itemPos,
                        alphaItem.sumU.add(betaItem.sumU.multiply(new BigInteger(alphaItem.no.toString()))),
                        betaItem.remU,
                        alphaItem.no);

                if (result.get(input) == null) {
                    InputSequence newInputSequence = new InputSequence();
                    newInputSequence.utility = alphaInputSequence.utility;
                    newInputSequence.length = alphaInputSequence.length;
                    newInputSequence.size = alphaInputSequence.size;
                    result.put(input, newInputSequence);
                }

                if (result.get(input).mapInfo.get(newInfo.pos) == null) {
                    result.get(input).mapInfo.put(newInfo.pos, newInfo);
                } else {
                    var curInfo = result.get(input).mapInfo.get(newInfo.pos);
                    curInfo.sumU = curInfo.sumU.add(newInfo.sumU);
                    curInfo.no = curInfo.no.add(newInfo.no);
                }

                if (alphaInputSequence.size - newInfo.pos > maxRemainSize) {
                    maxRemainSize = alphaInputSequence.size - newInfo.pos;
                }
            }
        }

        SULBigNumber sul = new SULBigNumber();
        sul.sequenceLastItem = beta.sequenceLastItem;
        sul.sequenceString = alpha.sequenceString + " " + beta.sequenceString;
        sul.db.put(sul.sequenceLastItem, result);
        sul.isDB = false;
        sul.sequenceMaxRemainSize = maxRemainSize;
        sul.sequenceSize = alpha.sequenceSize;

        return sul;
    }

    public BigDecimal calcFNub() {
        var data = db.get(sequenceLastItem);
        BigDecimal sumNub = BigDecimal.ZERO;

        for (var inputSequence : data.values()) {
            BigDecimal maxNub = BigDecimal.ZERO;
            ;
            for (var info : inputSequence.mapInfo.values()) {
                BigDecimal nub = new BigDecimal(info.sumU).divide(new BigDecimal(info.no), 2, RoundingMode.HALF_UP).add(new BigDecimal(info.remU));
                if (maxNub.compareTo(nub) < 0) {
                    maxNub = nub;
                }
            }

            sumNub = sumNub.add(maxNub);
        }

        return sumNub;
    }

    public BigDecimal calcNub() {
        var data = db.get(sequenceLastItem);
        BigDecimal sumNub = BigDecimal.ZERO;

        for (var inputSequence : data.values()) {
            BigDecimal maxNub = BigDecimal.ZERO;
            ;
            for (var info : inputSequence.mapInfo.values()) {
                BigDecimal nub = new BigDecimal(info.sumU).divide(new BigDecimal(info.no), 2, RoundingMode.HALF_UP).add(new BigDecimal(info.remU));
                if (maxNub.compareTo(nub) < 0) {
                    maxNub = nub;
                }
            }

            sumNub = sumNub.add(maxNub);
        }

        return sumNub;
    }

    public BigDecimal calcBiNub(SULBigNumber beta) { // beta = alpha + y
        var alphaMap = db.get(sequenceLastItem);
        var betaMap = beta.db.get(beta.sequenceLastItem);
        BigDecimal sumNub = BigDecimal.ZERO;

        for (var inputKey : betaMap.keySet()) {
            var inputSequence = alphaMap.get(inputKey);

            BigDecimal maxNub = BigDecimal.ZERO;
            for (var info : inputSequence.mapInfo.values()) {
                BigDecimal nub = new BigDecimal(info.sumU).divide(new BigDecimal(info.no), 2, RoundingMode.HALF_UP).add(new BigDecimal(info.remU));
                if (maxNub.compareTo(nub) < 0) {
                    maxNub = nub;
                }
            }

            sumNub = sumNub.add(maxNub);
        }

        return sumNub;
    }

    public BigDecimal calcWNub() {
        var data = db.get(sequenceLastItem);
        BigDecimal wNub = BigDecimal.ZERO;

        for (var inputSequence : data.values()) {
            BigDecimal maxNub = BigDecimal.ZERO;
            int minItemPos = 0;

            for (var itemInfo : inputSequence.mapInfo.values()) {
                BigDecimal nub = new BigDecimal(itemInfo.sumU).divide(new BigDecimal(itemInfo.no), 2, RoundingMode.HALF_UP).add(new BigDecimal(itemInfo.remU));

                if (maxNub.compareTo(nub) < 0) {
                    maxNub = nub;
                }

                if (minItemPos > itemInfo.itemPos) {
                    minItemPos = itemInfo.itemPos;
                }
            }

            if (inputSequence.length - minItemPos - 1 > 0) {
                wNub = wNub.add(maxNub);
            }
        }

        return wNub;
    }

    public BigDecimal[] calcFnubWnub() {
        var data = db.get(sequenceLastItem);
        BigDecimal fNub = BigDecimal.ZERO;
        BigDecimal wNub = BigDecimal.ZERO;

        for (var inputSequence : data.values()) {
            BigDecimal maxNub = BigDecimal.ZERO;
            int minItemPos = 0;

            for (var info : inputSequence.mapInfo.values()) {
                BigDecimal nub = new BigDecimal(info.sumU).divide(new BigDecimal(info.no), 2, RoundingMode.HALF_UP).add(new BigDecimal(info.remU));

                if (maxNub.compareTo(nub) < 0) {
                    maxNub = nub;
                }
                if (minItemPos > info.itemPos) {
                    minItemPos = info.itemPos;
                }
            }

            fNub = fNub.add(maxNub);

            if (inputSequence.length - minItemPos - 1 > 0) {
                wNub = wNub.add(maxNub);
            }
        }

        return new BigDecimal[]{fNub, wNub};
    }

    public BigDecimal[] calcFNubAvgUtility() {
        var data = db.get(sequenceLastItem);
        BigDecimal fNub = BigDecimal.ZERO;
        BigDecimal sumAvgUtility = BigDecimal.ZERO;

        for (var inputSequence : data.values()) {
            if (inputSequence.mapInfo.size() == 0) continue;

            BigDecimal maxNub = BigDecimal.ZERO;
            BigDecimal avgUtility = BigDecimal.ZERO;
            BigInteger count = BigInteger.ZERO;

            for (var info : inputSequence.mapInfo.values()) {
                BigDecimal nub = new BigDecimal(info.sumU).divide(new BigDecimal(info.no), 2, RoundingMode.HALF_UP).add(new BigDecimal(info.remU));

                if (maxNub.compareTo(nub) < 0) {
                    maxNub = nub;
                }

                avgUtility = avgUtility.add(new BigDecimal(info.sumU));
                count = count.add(info.no);
            }

            fNub = fNub.add(maxNub);
            sumAvgUtility = sumAvgUtility.add(avgUtility.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP));
        }

        return new BigDecimal[]{fNub, sumAvgUtility};
    }

    public BigDecimal[] calcFNubWNubAvgUtility() {
        var data = db.get(sequenceLastItem);
        BigDecimal fNub = BigDecimal.ZERO;
        BigDecimal wNub = BigDecimal.ZERO;
        BigDecimal sumAvgUtility = BigDecimal.ZERO;

        for (var inputSequence : data.values()) {
            if (inputSequence.mapInfo.size() == 0) continue;

            BigDecimal maxNub = BigDecimal.ZERO;
            int minItemPos = 0;
            BigDecimal avgUtility = BigDecimal.ZERO;
            BigInteger count = BigInteger.ZERO;

            for (var info : inputSequence.mapInfo.values()) {
                BigDecimal nub = new BigDecimal(info.sumU).divide(new BigDecimal(info.no), 2, RoundingMode.HALF_UP).add(new BigDecimal(info.remU));
                if (maxNub.compareTo(nub) < 0) {
                    maxNub = nub;
                }

                if (minItemPos > info.itemPos) {
                    minItemPos = info.itemPos;
                }

                avgUtility = avgUtility.add(new BigDecimal(info.sumU));
                count = count.add(info.no);
            }

            fNub = fNub.add(maxNub);
            sumAvgUtility = sumAvgUtility.add(avgUtility.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP));

            if (inputSequence.length - minItemPos - 1 > 0) {
                wNub = wNub.add(maxNub);
            }
        }

        return new BigDecimal[]{fNub, wNub, sumAvgUtility};
    }

    public BigDecimal calcAvgUtility() {
        var data = db.get(sequenceLastItem);

        BigDecimal sumAvgUtility = BigDecimal.ZERO;

        for (var inputSequence : data.values()) {
            if (inputSequence.mapInfo.size() == 0) continue;
            BigDecimal avgUtility = BigDecimal.ZERO;
            BigInteger count = BigInteger.ZERO;

            for (var val : inputSequence.mapInfo.values()) {
                avgUtility = avgUtility.add(new BigDecimal(val.sumU));
                count = count.add(val.no);
            }

            sumAvgUtility = sumAvgUtility.add(avgUtility.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP));
        }

        return sumAvgUtility;
    }

    public long calcSWU(int item) {
        long swu = 0;

        var data = db.get(item);
        for (var val : data.values()) {
            swu += val.utility;
        }

        return swu;
    }

    public long calcSWU() {
        var itemMap = db.get(sequenceLastItem);
        long swu = 0;

        for (var val : itemMap.values()) {
            swu += val.utility;
        }

        return swu;
    }

    public int getSupport() {
        return db.get(sequenceLastItem).size();
    }

    public void deleteItem(int item) {
        var itemMap = db.get(item);
        if (itemMap == null) return;

        db.remove(item);

        for (var input : itemMap.keySet()) {
            int reduce = 0;
            for (var inputSequence : itemMap.get(input).mapInfo.values()) {
                reduce += Integer.parseInt(inputSequence.sumU.toString());
            }

            for (var otherItem : db.values()) {
                if (otherItem.get(input) != null) {
                    otherItem.get(input).utility -= reduce;
                }
            }
        }
    }

    public String toString() {
        String result = "";

        if (isDB) {
            result += "QSDB\n";
        } else {
            result += "Sequence: " + sequenceString + " \n";
        }

        for (var keyVal : db.entrySet()) {
            result += "Item: " + keyVal.getKey() + "\n";
            for (var inputSequence : keyVal.getValue().entrySet()) {
                result += inputSequence.getKey() + " - " + inputSequence.getValue().length + " | ";
                for (var info : inputSequence.getValue().mapInfo.values()) {
                    result += "( " + info.pos + ", " + info.itemPos + ", " + info.sumU + ", " + info.remU + ", " + info.no + " ) ";
                }
                result += "\n";
            }
        }

        return result;
    }

    public String toString(String debugTab) {
        String result = "";

        if (isDB) {
            result += debugTab + "QSDB\n";
        } else {
            result += debugTab + "Sequence: " + sequenceString + " \n";
        }

        for (var keyVal : db.entrySet()) {
            result += debugTab + "Item: " + keyVal.getKey() + "\n";
            for (var inputSequence : keyVal.getValue().entrySet()) {
                result += debugTab + inputSequence.getKey() + " - " + inputSequence.getValue().length + " | ";
                for (var info : inputSequence.getValue().mapInfo.values()) {
                    result += "( " + info.pos + ", " + info.itemPos + ", " + info.sumU + ", " + info.remU + ", " + info.no + " ) ";
                }
                result += "\n";
            }
        }

        return result;
    }

    public static class InputSequence {
        public int utility = 0;
        public int length = 0;
        public int size = 0;
        HashMap<Integer, Info> mapInfo = new HashMap<>(); // pos - info
    }

    public static class Info {
        int pos = 0;
        int itemPos = 0;
        BigInteger sumU = new BigInteger("0");
        BigInteger remU = new BigInteger("0");
        BigInteger no = new BigInteger("0");

        public Info(int pos, int itemPos, BigInteger sumU, BigInteger remU, BigInteger no) {
            this.pos = pos;
            this.itemPos = itemPos;
            this.sumU = sumU;
            this.remU = remU;
            this.no = no;
        }
    }
}
