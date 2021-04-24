package ca.pfv.spmf.algorithms.sequentialpatterns.UMax_FHUSPM;


import java.util.HashMap;

public class SUL {
    boolean isDB = true;

    String sequenceString = "";
    int sequenceSize = 0;
    Integer sequenceLastItem = 0;

    public long dbUtility = 0;
    public long dbMaxSWU = 0;
    public HashMap<Integer, HashMap<Integer, InputSequence>> db = new HashMap<>(); // item - inputSequence - info

    public SUL() {
    }

    public SUL(SUL other) {
        this.isDB = other.isDB;
        this.sequenceString = new String(other.sequenceString);
        this.sequenceSize = other.sequenceSize;
        this.sequenceLastItem = other.sequenceLastItem;

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

        Integer uRem = swu;
        int length = 0;
        int end = 0;

        for (int i = 0; i < tokens.length - 2; i++) {
            String token = tokens[i];
            if (token.length() == 0) continue;

            if (token.charAt(0) == '-') {
                end++;
                continue;
            }

            length++;

            int lefBracket = token.indexOf('[');
            int rightBracket = token.indexOf(']');

            Integer item = Integer.parseInt(token.substring(0, lefBracket));
            Integer utility = Integer.parseInt(token.substring(lefBracket + 1, rightBracket));
            dbUtility += utility;

            if (db.get(item) == null) {
                db.put(item, new HashMap<>());
            }

            if (db.get(item).get(inputIndex) == null) {
                db.get(item).put(inputIndex, new InputSequence());
            }

            var inputSequence = db.get(item).get(inputIndex);

            uRem = uRem - utility;
            inputSequence.utility = swu;
            inputSequence.mapInfo.put(end, new Info(end, utility, uRem));
        }

        for (var itemMap : db.values()) {
            var inputSequence = itemMap.get(inputIndex);
            if (inputSequence == null) continue;

            inputSequence.length = length;
            inputSequence.size = end;
        }
    }

    public SUL get(Integer item) {
        var data = db.get(item);
        if (data == null) return null;

        SUL sul = new SUL();
        sul.sequenceString = "" + item;
        sul.db.put(item, data);
        sul.sequenceLastItem = item;
        sul.isDB = false;
        sul.sequenceSize = 1;

        return sul;
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
                result += inputSequence.getKey() + " | ";
                for (var info : inputSequence.getValue().mapInfo.values()) {
                    result += "( " + info.end + ", " + info.u + ", " + info.uRem + " ) ";
                }
                result += "\n";
            }
        }

        return result;
    }

    public static SUL iExtend(SUL alpha, SUL y) {
        var alphaMap = alpha.db.get(alpha.sequenceLastItem);
        var yMap = y.db.get(y.sequenceLastItem);

        HashMap<Integer, InputSequence> result = new HashMap<>();

        for (var input: alphaMap.keySet()) {
            var yInputSequence = yMap.get(input);
            if (yInputSequence == null) continue;

            var alphaInputSequence = alphaMap.get(input);

            for (var pos : alphaInputSequence.mapInfo.keySet()) {
                var yItem = yInputSequence.mapInfo.get(pos);
                if (yItem == null) continue;

                var alphaItem = alphaInputSequence.mapInfo.get(pos);

                var newInfo = new Info(yItem.end, alphaItem.u + yItem.u, yItem.uRem);

                if (result.get(input) == null) {
                    var newInputSequence = new InputSequence();
                    newInputSequence.utility = alphaInputSequence.utility;
                    newInputSequence.size = alphaInputSequence.size;
                    newInputSequence.length = alphaInputSequence.length;
                    result.put(input, newInputSequence);
                }

                if (result.get(input).mapInfo.get(newInfo.end) == null) {
                    result.get(input).mapInfo.put(newInfo.end, newInfo);
                } else {
                    var curInfo = result.get(input).mapInfo.get(newInfo.end);
                    if (curInfo.u < newInfo.u) {
                        curInfo.u = newInfo.u;
                    }
                }
            }
        }

        SUL sul = new SUL();
        sul.sequenceLastItem = y.sequenceLastItem;
        sul.sequenceString = alpha.sequenceString + " " + y.sequenceString;
        sul.db.put(sul.sequenceLastItem, result);
        sul.isDB = false;
        sul.sequenceSize = alpha.sequenceSize;

        return sul;
    }

    public static SUL sExtend(SUL alpha, SUL y) {
        var alphaMap = alpha.db.get(alpha.sequenceLastItem);
        var yMap = y.db.get(y.sequenceLastItem);

        HashMap<Integer, InputSequence> result = new HashMap<>();

        for (var input: alphaMap.keySet()) {
            var yInputSequence = yMap.get(input);
            if (yInputSequence == null) continue;

            var alphaInputSequence = alphaMap.get(input);

            for (var alphaItem: alphaInputSequence.mapInfo.values()) {
                for (var yItem: yInputSequence.mapInfo.values()) {
                    if (alphaItem.end < yItem.end) {
                        var newInfo = new Info(
                                yItem.end,
                                alphaItem.u + yItem.u,
                                yItem.uRem
                        );

                        if (result.get(input) == null) {
                            var newInputSequence = new InputSequence();
                            newInputSequence.utility = alphaInputSequence.utility;
                            newInputSequence.size = alphaInputSequence.size;
                            newInputSequence.length = alphaInputSequence.length;
                            result.put(input, newInputSequence);
                        }

                        if (result.get(input).mapInfo.get(newInfo.end) == null) {
                            result.get(input).mapInfo.put(newInfo.end, newInfo);
                        } else {
                            var curInfo = result.get(input).mapInfo.get(newInfo.end);
                            if (curInfo.u < newInfo.u) {
                                curInfo.u = newInfo.u;
                            }
                        }
                    }
                }
            }
        }

        SUL sul = new SUL();
        sul.sequenceLastItem = y.sequenceLastItem;
        sul.sequenceString = alpha.sequenceString + " -1 " + y.sequenceString;
        sul.db.put(sul.sequenceLastItem, result);
        sul.isDB = false;
        sul.sequenceSize = alpha.sequenceSize;

        return sul;
    }

    public long calcSWU() {
        long swu = 0;
        var itemMap = db.get(sequenceLastItem);

        for (var val: itemMap.values()) {
            swu += val.utility;
        }

        return swu;
    }

    public long calcMEU() {
        long meu = 0;
        var itemMap = db.get(sequenceLastItem);

        for (var val: itemMap.values()) {
            long maxUbRem = 0;

            for (var pos : val.mapInfo.values()) {
                long ubRem = pos.u + pos.uRem;
                if (maxUbRem < ubRem) {
                    maxUbRem = ubRem;
                }
            }

            meu += maxUbRem;
        }

        return meu;
    }

    public long calcMaxUtility() {
        long utility = 0;
        var itemMap = db.get(sequenceLastItem);

        for (var sequence: itemMap.values()) {
            long maxUtility = 0;

            for (var endPos : sequence.mapInfo.values()) {
                if (maxUtility < endPos.u) {
                    maxUtility = endPos.u;
                }
            }

            utility += maxUtility;
        }

        return utility;
    }

    public int getSupport() {
        return db.get(sequenceLastItem).size();
    }

    public void deleteItem(Integer item) {
        var itemMap = db.get(item);
        if (itemMap == null) return;

        db.remove(item);

        for (var input: itemMap.keySet()) {
            int reduce = 0;

            for (var sequenceItem : itemMap.get(input).mapInfo.values()) {
                reduce += sequenceItem.u;
            }

            for (var otherItem: db.values()) {
                if (otherItem.get(input) != null) {
                    otherItem.get(input).utility -= reduce;
                }
            }
        }
    }

    public static class InputSequence {
        public long utility = 0;
        public int size = 0;
        public int length = 0;
        public int maxSizeRem = 0;
        HashMap<Integer, Info> mapInfo = new HashMap<>();

        public InputSequence() {
        }

        public InputSequence(long utility, int size, int length, int maxSizeRem) {
            this.utility = utility;
            this.size = size;
            this.length = length;
            this.maxSizeRem = maxSizeRem;
        }
    }

    public static class Info {
        public int end = 0;
        public long u = 0;
        public long uRem = 0;

        public Info(int end, long u, long uRem) {
            this.end = end;
            this.u = u;
            this.uRem = uRem;
        }
    }
}
