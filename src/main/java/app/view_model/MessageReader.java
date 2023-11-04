package app.view_model;

public class MessageReader {
    private static String message;
    
    public static void setMsg(String msg){
        message = msg;
    }

    public static String getMsg(){
        return message;
    }

    public static void main(String[] args) {
        String message = "This is a message, with, commas, to replace.";
        String sanitizedMessage = message.replace(",", "#");
        System.out.println(sanitizedMessage);
    }
}
