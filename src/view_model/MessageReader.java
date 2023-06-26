package view_model;

public class MessageReader {
    private static String message;
    
    public static void setMsg(String msg){
        message = msg;
    }

    public static String getMsg(){
        return message;
    }
}
