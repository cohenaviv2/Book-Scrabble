package Model.cache;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

/* Performs an I/O based search of the word in the txt files of each book.
 * The static search method searches all files for the word
 * Once the word is found, will return true.
 * if all the files were scanned and the word was not found then will return false.
 * 
 * @author: Aviv Cohen
 * 
 */

public class IOSearcher {

    public static boolean search(String word,String...fileNames){
        for (String book : fileNames) {
            try {
                Scanner myScanner = new Scanner(new BufferedReader(new FileReader(book)));
                while(myScanner.hasNext()){
                    if (word.equals(myScanner.next())){
                        myScanner.close();
                        return true;
                    }
                }
                myScanner.close();
            } catch (Exception e) {
                System.out.println("Exception thrown : " + e);
                return false;
            }
        }
        return false;
    }

}
