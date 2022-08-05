import com.sun.source.tree.Tree;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.*;
import java.util.*;

public class POTDLeaderboard extends ListenerAdapter {
    static TreeMap<Student,String> leaderboard = new TreeMap<>();
    static TreeMap<String,Student> leaderboard2;
    static int alg1 = -1;
    static int alg2 = -1;
    static int geo = -1;
    static int precalc = -1;



    public static TreeMap<String,Student> swap(TreeMap<Student,String> map1){
        TreeMap<String,Student> map2 = new TreeMap<>();
        for(Student s:map1.keySet()){
            map2.put(s.name,s);
        }
        return map2;
    }
    public static TreeMap<Student,String> swap2(TreeMap<String,Student> map1){
        TreeMap<Student,String> map2 = new TreeMap<>();
        for(String name:map1.keySet()){
            map2.put(map1.get(name),name);
        }
        return map2;
    }
    public static void clear(String name){
        System.out.println("I was supposed to do something");
        leaderboard2.get(name).score=0;
        leaderboard = swap2(leaderboard2);
    }

    public static void serialize(TreeMap<Student,String> students) throws IOException, ClassNotFoundException {
        FileOutputStream fos = new FileOutputStream("src/main/java/Leaderboard");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(students);
        oos.flush();
        oos.close();
    }
    public static TreeMap<Student,String> deserialize() throws IOException, ClassNotFoundException, ClassCastException {
        FileInputStream fis = new FileInputStream("src/main/java/Leaderboard");
        ObjectInputStream ois = new ObjectInputStream(fis);
        TreeMap<Student,String> serialized_students = (TreeMap<Student,String>) ois.readObject();
        ois.close();
        return serialized_students;
    }
    public static EmbedBuilder embedBuilder(){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Leaderboard");
        eb.setColor(new Color(220, 20, 60));
        int i = 1;
        for(Student student:leaderboard.descendingKeySet()){
            eb.addField(i+". ```"+student.name+": "+student.score+"```","",false);
            i++;
        }
        return eb;
    }

    public static void setPoints(String name, int points){
        leaderboard2.get(name).score = points;
        leaderboard = swap2(leaderboard2);
    }
    public static void addPoints(String name, int points){
        leaderboard2.get(name).score += points;
        leaderboard = swap2(leaderboard2);
    }

    public static EmbedBuilder helpEmbedBUilder(){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Help");
        eb.setColor(new Color(220,20,60));
        eb.addField("**!help**","```Takes you to the help embed```",false);
        eb.addField("!answer [division] [your answer]", "```Divisions are alg1, alg2, geo, and precalc and answers must be integers. Answers must be DM'd to the bot.```",false);
        eb.addField("**!leaderboard / !lb / !leaderboarf / !leadertbnkoead**","```Sends the leaderboard. Each POTD gives 75,50,25 points with 3,2,1 tries```", false);
        return eb;

    }
    public static void resets(){
        for(Student s: leaderboard.keySet()){
            System.out.println(s.name+" "+s.tries);

        }
    }

    public static void main(String[] args) throws LoginException, IOException, ClassNotFoundException {
        String token = "OTMyMDA4NzE2OTk4NDE0MzY2.YeMuwg.IJgiUxgXeRj6C9ynvoOxP5bbBg4";
        JDA jda = JDABuilder.createDefault(token).build();
        JDABuilder builder = JDABuilder.createDefault(token);
        JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES).addEventListeners(new POTDLeaderboard()).setActivity(Activity.playing("!leaderboard")).build();

        //Serialization!!!!
        leaderboard = deserialize();
        leaderboard2 = swap(leaderboard);


    }
    public static void resetTries(){
        for(Student s :leaderboard.keySet()){
            s.tries=3;
            s.solved = false;
        }
        leaderboard2=swap(leaderboard);
    }
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(!event.getAuthor().getName().equals("POTDLeaderboard")){
            String str = event.getMessage().getContentRaw();
            MessageChannel channel = event.getChannel();
            String name = event.getAuthor().getName();
            //Embeds
            if(str.equals("!leaderboard")||str.equals("!lb")||str.equals("!leaderboarf")||str.equals("!leadertbnkoead")) channel.sendMessageEmbeds(embedBuilder().build()).queue();
            if(str.equals("!help")) channel.sendMessageEmbeds(helpEmbedBUilder().build()).queue();

            //Setting answers
            if(!str.contains("!setPoints")&&str.contains("!set")&&event.getAuthor().getIdLong()==487797361335074827L){
                resetTries();
                channel.sendMessage("Tries have been reset").queue();
                if(!str.equals("!set")) {
                    StringTokenizer st = new StringTokenizer(str);
                    st.nextToken();
                    String setAnswerType = st.nextToken();
                    System.out.println("answer type: " + setAnswerType);
                    int setAnswer = Integer.parseInt(st.nextToken());
                    channel.sendMessage("Answer has been set to: " + setAnswer).queue();
                    switch (setAnswerType) {
                        case "alg1" -> alg1 = setAnswer;
                        case "alg2" -> alg2 = setAnswer;
                        case "geo" -> geo = setAnswer;
                        case "precalc" -> precalc = setAnswer;
                    }
                    channel.sendMessage("Answer has been set!").queue();
                    if (leaderboard2.isEmpty()) {
                        leaderboard2.put(name, new Student(name, -1, "calc", 3));
                        leaderboard = swap2(leaderboard2);
                        System.out.println(leaderboard2);

                    }
                }
            }

            //Answering
            if(event.isFromType(ChannelType.PRIVATE)&&str.contains("!answer")){
                int answer;
                if(str.contains("alg1")){
                    if(!leaderboard2.containsKey(name)){
                        leaderboard.put(new Student(name,0,"alg1",3),name);
                        leaderboard2 = swap(leaderboard);
                    }
                    answer = Integer.parseInt(str.substring(13));
                    if(!leaderboard2.isEmpty()&&leaderboard2.containsKey(name)&&leaderboard2.get(name).solved){
                        channel.sendMessage("You've already solved it!").queue();
                    }
                    else if(alg1==answer){
                        leaderboard2.get(name).score+=(int)(75*leaderboard2.get(name).tries*1.0/3);
                        leaderboard2.get(name).solved=true;
                        leaderboard = swap2(leaderboard2);
                        channel.sendMessage("Nice job, the answer is "+alg1).queue();
                    }
                    else if(alg1!=answer){
                        System.out.println(answer);
                        System.out.println("answer"+alg1);
                        leaderboard2.get(name).tries--;
                        leaderboard = swap2(leaderboard2);
                        channel.sendMessage("Not quite, try again").queue();
                    }
                }
                else if(str.contains("alg2")){
                    if(!leaderboard2.containsKey(name)){
                        leaderboard.put(new Student(name,0,"alg2",3),name);
                        leaderboard2 = swap(leaderboard);
                    }
                    answer = Integer.parseInt(str.substring(13));
                    if(!leaderboard2.isEmpty()&&leaderboard2.containsKey(name)&&leaderboard2.get(name).solved){
                        channel.sendMessage("You've already solved it!").queue();
                    }
                    else if(alg2==answer){
                        leaderboard2.get(name).score+=(int)(75*leaderboard2.get(name).tries*1.0/3);
                        leaderboard2.get(name).solved=true;
                        leaderboard = swap2(leaderboard2);
                        channel.sendMessage("Nice job, the answer is "+alg2).queue();
                    }
                    else if(alg2!=answer){
                        leaderboard2.get(name).tries--;
                        leaderboard = swap2(leaderboard2);
                        channel.sendMessage("Not quite, try again").queue();
                    }
                }
                else if(str.contains("precalc")){
                    if(!leaderboard2.containsKey(name)){
                        leaderboard.put(new Student(name,0,"precalc",3),name);
                        leaderboard2 = swap(leaderboard);
                    }
                    answer = Integer.parseInt(str.substring(16));
                    if(!leaderboard2.isEmpty()&&leaderboard2.containsKey(name)&&leaderboard2.get(name).solved){
                        channel.sendMessage("You've already solved it!").queue();
                    }
                    else if(precalc==answer){
                        leaderboard2.get(name).score+=(int)(75*leaderboard2.get(name).tries*1.0/3);
                        leaderboard2.get(name).solved=true;
                        leaderboard = swap2(leaderboard2);
                        channel.sendMessage("Nice job, the answer is "+precalc).queue();
                    }
                    else if(precalc!=answer){
                        leaderboard2.get(name).tries--;
                        leaderboard = swap2(leaderboard2);
                        channel.sendMessage("Not quite, try again").queue();
                    }
                }
                else if(str.contains("geo")){
                    if(!leaderboard2.containsKey(name)){
                        leaderboard.put(new Student(name,0,"geo",3),name);
                        leaderboard2 = swap(leaderboard);
                    }
                    answer = Integer.parseInt(str.substring(12));
                    if(!leaderboard2.isEmpty()&&leaderboard2.containsKey(name)&&leaderboard2.get(name).solved){
                        channel.sendMessage("You've already solved it!").queue();
                    }
                    else if(geo==answer){
                        leaderboard2.get(name).score+=(int)(75*leaderboard2.get(name).tries*1.0/3);
                        leaderboard2.get(name).solved=true;
                        leaderboard = swap2(leaderboard2);
                        channel.sendMessage("Nice job, the answer is "+geo).queue();
                    }
                    else if(geo!=answer){
                        leaderboard2.get(name).tries--;
                        leaderboard = swap2(leaderboard2);
                        channel.sendMessage("Not quite, try again").queue();
                    }
                }


            }

            //Misc
            if(event.getAuthor().getIdLong()==487797361335074827L) {
                if (str.contains("!clear")) clear(str.substring(6).trim());
                if (str.contains("!setPoints")) {
                    StringTokenizer st = new StringTokenizer(str);
                    st.nextToken();
                    String namePoints = st.nextToken();
                    int points = Integer.parseInt(st.nextToken());
                    System.out.println(namePoints);
                    System.out.println(points);
                    setPoints(namePoints, points);
                    channel.sendMessage("Score has been set to "+points).queue();
                }
                if (str.contains("!addPoints")) {
                    StringTokenizer st = new StringTokenizer(str);
                    st.nextToken();
                    String namePoints = st.nextToken();
                    int points = Integer.parseInt(st.nextToken());
                    addPoints(namePoints, points);
                    if(points>0) channel.sendMessage(points+" points have been added to their score").queue();
                    else channel.sendMessage(points+" points have been subtracted from their score").queue();
                }
                if(str.equals("!resets")) resets();
            }


            //Josh
            if(event.getAuthor().getName().equals("spetznazspy24")&&(int)(Math.random() * 5) ==4) channel.sendMessage("Didn't ask").queue();

            try {serialize(leaderboard);}
            catch (Exception e) {e.printStackTrace();}

        }


    }


}

