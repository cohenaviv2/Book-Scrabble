package cache;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;

/* 
 * Space efficient probabilistic algorithm based on hashing
 * Maintains BitSet and K different hash functions
 * knows with absolute certainty if a word is not found in the book dictionary
 * and with a high probability as desired whether a word is found.
 * 
 * @author: Aviv Cohen
 * 
 */

public class BloomFilter {
    private BitSet bs;
    private ArrayList<String> bf_algs;
    private int size;

    public BloomFilter(int size, String... algs) {
        this.size = size;
        this.bs = new BitSet(size);
        this.bs.set(0, this.size, false); // set all bits to 0
        this.bf_algs = new ArrayList<>();
        for (int i = 0; i < algs.length; i++) {
            this.bf_algs.add(algs[i]);
        }
    }

    public void add(String word) {
        /*
         * The algorithm activates K hash functions on this word,
         * each one returns a different numerical value.
         * On each such value performs modulo according to the length of the BitSet to
         * get a single index.
         * turns on the bits in the BitSet at the returned indexes.
         */

        for (String s : bf_algs) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance(s);
                byte[] bytes = md.digest(word.getBytes());
                BigInteger index = new BigInteger(bytes);
                int i = (Math.abs(index.intValue())) % this.bs.size();
                this.bs.set(i);
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Exception thrown : " + e);
            }
        }
    }

    public boolean contains(String word) {
        /*
         * performs the same operation as above in order to get the single index
         * returns true if the bloom filter contains this word
         */

        for (String s : bf_algs) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance(s);
                byte[] bytes = md.digest(word.getBytes());
                BigInteger index = new BigInteger(bytes);
                int i = (Math.abs(index.intValue())) % this.bs.size();
                if (!this.bs.get(i)) {
                    return false;
                }
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Exception thrown : " + e);
            }
        }
        return true;
    }

    @Override
    public String toString() {
        /*
         * The method will return a string of {0,1} depending on the on/off bits in the
         * BitSet
         */

        String bitStr = new String();

        int on = this.bs.nextSetBit(0); // first index thats on

        for (int i = 0; i < this.bs.length(); i++) {
            if (i == on) {
                bitStr = bitStr.concat("1");
                on = this.bs.nextSetBit(on + 1); // next index thats on
            } else {
                bitStr = bitStr.concat("0");
            }
        }

        return bitStr;
    }

}
