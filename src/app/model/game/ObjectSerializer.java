package app.model.game;

import java.io.*;
import java.util.Base64;

public class ObjectSerializer {
    /**
     * The ObjectSerializer class provides static methods to serialize objects into
     * a string representation and deserialize string representations back into objects.
     * It uses Java's built-in serialization mechanism to convert objects into byte arrays 
     * and then encodes the byte arrays into Base64 strings.
     * The class supports serializing and deserializing any object that implements
     * the Serializable interface.
     */
    public static String serializeObject(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        byte[] bytes = bos.toByteArray();
        oos.close();
        bos.close();
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static Object deserializeObject(String str) throws IOException, ClassNotFoundException {
        byte[] bytes = Base64.getDecoder().decode(str);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        bis.close();
        return obj;
    }
}
