package team30.personalbest.messeging;

import java.io.Serializable;
import java.util.HashMap;

public class MyUser implements Serializable {

    private String user_id;
    private String user_name;
    private String user_email;
    private HashMap<String, Boolean> chatRooms;

    MyUser(String user_id, String user_name, String email, HashMap<String, Boolean> chatRooms) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_email = email;
        this.chatRooms = chatRooms;
    }

    MyUser() {
        this.user_email = "none";
        this.user_name = "none";
        this.user_id = "none";
        this.chatRooms = new HashMap<>();
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public HashMap<String, Boolean> getChatRooms() {
        return chatRooms;
    }
}
