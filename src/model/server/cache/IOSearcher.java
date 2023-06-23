package model.server.cache;

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

    public static boolean search(String word, String... fileNames) {
        for (String book : fileNames) {
            try {
                BufferedReader reader = new BufferedReader(new BufferedReader(new FileReader(book)));
                String line;
                while ((line = reader.readLine()) != null) {
                    Scanner myScanner = new Scanner(line);
                    myScanner.useDelimiter("\\W+");
                    while (myScanner.hasNext()) {
                        if (word.equalsIgnoreCase(myScanner.next())) {
                            myScanner.close();
                            reader.close();
                            return true;
                        }
                    }
                    myScanner.close();
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
