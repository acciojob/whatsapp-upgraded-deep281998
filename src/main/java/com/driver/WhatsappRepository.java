package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {

    Map<String , User> userMap = new HashMap<>();

    Map<Group , List<User>> groupListMap = new HashMap<>();

    Map<Integer , Message> messageMap = new HashMap<>();

    Map<User , List<Message>> usermessagedb = new HashMap<>();

    Map<Group , List<Message>> groupmessagedb = new HashMap<>();

    Map<User , Group> admindb = new HashMap<>();

    int count = 0;
    int messagecount = 1;

    public String createUser(String name, String mobile) {
        if(userMap.containsKey(mobile) == true){
            throw new RuntimeException("User already exists");
        }
        User user = new User(name , mobile);
        userMap.put(mobile,user);
        usermessagedb.put(user,new ArrayList<>());
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) {
        for (User user : users){
            if(!userMap.containsKey(user.getMobile())){
                userMap.put(user.getMobile() , user);
            }
        }
        if(users.size() == 2){
            Group group = new Group();
            group.setName(users.get(1).getName());
            group.setNumberOfParticipants(2);
            groupListMap.put(group , users);
            groupmessagedb.put(group,new ArrayList<>());
            admindb.put(users.get(1) , group);
            return group;
        }
        else {
            Group group = new Group();
            count++;
            group.setName("Group "+count);
            group.setNumberOfParticipants(users.size());
            groupListMap.put(group,users);
            groupmessagedb.put(group,new ArrayList<>());
            admindb.put(users.get(0),group);
            return group;
        }
    }

    public int createMessage(String content) {
        int id = messagecount;
        messagecount++;
        Date date = new Date();
        Message message = new Message();
        message.setId(id);
        message.setContent(content);
        message.setTimestamp(date);
        messageMap.put(id,message);
        return id;
    }

    public int sendMessage(Message message, User sender, Group group) {
            if(groupListMap.containsKey(group)){
                for(User user : groupListMap.get(group)){
                    if(user.getMobile().equals(sender.getMobile())){
                        List<Message> messages = usermessagedb.get(user);
                        messages.add(message);
                        usermessagedb.put(user,messages);
                        List<Message> messageList = groupmessagedb.get(group);
                        messageList.add(message);
                        groupmessagedb.put(group,messageList);
                        return groupmessagedb.get(group).size();
                    }
                }
                throw new RuntimeException("You are not allowed to send message");
            }
        throw new RuntimeException("Group does not exist");
    }

    public String changeAdmin(User approver, User user, Group group) {
        if(groupListMap.containsKey(group)){
            if(admindb.containsKey(approver) && admindb.get(approver) == group){
                for (User user1 : groupListMap.get(group)){
                    if(user1 == user){
                        admindb.remove(approver);
                        admindb.put(user,group);
                        return "SUCCESS";
                    }
                }
                throw new RuntimeException("User is not a participant");
            }
            throw new RuntimeException("Approver does not have rights");
        }
        throw new RuntimeException("Group does not exist");
    }

    public int removeUser(User user) {
        if(admindb.containsKey(user)){
            throw new RuntimeException("Cannot remove admin");
        }
        for (Group group : groupListMap.keySet()){
            for (User user1 : groupListMap.get(group)){
                if(user1 == user){
                    List<User> users = groupListMap.get(group);
                    users.remove(user1);
                    groupListMap.put(group,users);
                    List<Message> messageList = usermessagedb.get(user);
                    usermessagedb.remove(user);
                    List<Message> messages = groupmessagedb.get(group);
                    for(Message message : messageList){
                        messages.remove(message);
                    }
                    groupmessagedb.put(group,messages);
                    return groupmessagedb.get(group).size()+messageMap.size()+groupListMap.get(group).size();
                }
            }
        }
        throw new RuntimeException("User not found");
    }

    public String findMessage(Date start, Date end, int k) {
        List<Message> messageList = new ArrayList<>();
        for(Message message : messageMap.values()){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                messageList.add(message);
            }
        }
        if(messageList.size() < k){
            throw new RuntimeException("K is greater than the number of messages");
        }
        Map<Long, Message> map = new TreeMap<>();
        String response = "";
        for(Message message : messageList){
            long val = message.getTimestamp().getTime();
            map.put(val, message);
        }
        for(Message message : map.values()){
            k--;
            if(k == 0){
                response = message.getContent();
                break;
            }
        }
        return response;
    }
}
