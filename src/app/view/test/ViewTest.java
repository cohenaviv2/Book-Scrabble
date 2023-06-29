package app.view.test;

import java.io.File;
import java.io.InputStream;

import javafx.scene.image.Image;

public class ViewTest {
    public static void main(String[] args) {
        String imagePath = "BB.png";
        File imageFile = new File(imagePath);

        if (imageFile.exists() && imageFile.canRead()) {
            System.out.println("Image file exists and is readable.");
        } else {
            System.out.println("Image file does not exist or is not readable.");
        }

        InputStream inputStream = ViewTest.class.getResourceAsStream(imagePath);

        //String backgroundImageUrl = ViewTest.class.getClassLoader().getResource(imagePath).toExternalForm();
        //System.out.println(backgroundImageUrl);

        Image image = new Image(inputStream);

        if (image.isError()) {
            System.out.println("Failed to load the image.");
        } else {
            System.out.println("Image loaded successfully.");
        }
    }
}
