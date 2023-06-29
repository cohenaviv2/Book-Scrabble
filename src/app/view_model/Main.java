package app.view_model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        // Load the image using ClassLoader
        ClassLoader classLoader = Main.class.getClassLoader();
        URL resourceUrl = classLoader.getResource("resources/boardImg.png");

        if (resourceUrl != null) {
            try {
                BufferedImage image = ImageIO.read(resourceUrl);
                // Do something with the image...
                System.out.println("Resource URL: " + resourceUrl);
                System.out.println("done");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Image file not found!");
        }

        InputStream inputStream = classLoader.getResourceAsStream("resources/boardImg.png");

        if (inputStream != null) {
            // Use the input stream...
        } else {
            System.out.println("Resource not found!");
        }

    }

}
