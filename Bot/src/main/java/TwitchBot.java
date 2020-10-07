import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.sql.SQLException;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class TwitchBot extends Thread
{
    private String USERNAME;
    private String HOST;
    private String PASS;
    private String CHANNEL;
    private int PORT;
    private String CLIENT;

    private Socket s;

    private BufferedWriter bw;
    private BufferedReader br;
    private Chat chat;
    private StreamUtils sUtils;

    private Runnable runnable;

    private boolean live;


    private boolean isRunning = false;

    public TwitchBot(String USERNAME, String HOST, String PASS, String CHANNEL, int PORT, String CLIENT)
    {
        this.USERNAME = USERNAME.toLowerCase();
        this.HOST = HOST.toLowerCase();
        this.PASS = PASS.toLowerCase();
        this.CHANNEL = CHANNEL.toLowerCase();
        this.PORT = PORT;
        this.CLIENT = CLIENT;
    }

    private void init()
    {
        System.out.println("Starting...");
        try
        {
            s = new Socket(HOST, PORT);

            bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            sUtils = new StreamUtils(CHANNEL, CLIENT, PASS);
            chat = new Chat(USERNAME, CHANNEL);

            chat.sendToServer(bw, "PASS " + PASS);
            chat.sendToServer(bw, "NICK " + USERNAME);
            chat.sendToServer(bw, "USER " + USERNAME);
            chat.sendToServer(bw, "CAP REQ :twitch.tv/membership");
            chat.sendToServer(bw, "CAP REQ :twitch.tv/commands");
            chat.sendToServer(bw, "JOIN #" + CHANNEL);

            live = sUtils.streamStatus();

            runnable = new Botthread(bw,chat,sUtils);
            Thread thread = new Thread(runnable);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loop()
    {
        isRunning = true;
        String line = "";
        try
        {
            long lastActivity = Instant.now().getEpochSecond();

            while((line = br.readLine()) != null && isRunning)
            {
                if(line.contains("PRIVMSG"))
                {
                    long currentActivity = Instant.now().getEpochSecond();
                    if (currentActivity-lastActivity < 2){
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    String user = line.substring(1, line.indexOf("!"));
                    String message = line.substring(line.indexOf(" :") + 2);

                    System.out.println(user + " >> " + message);

                    int command = chat.processCommands(bw, user, message);
                    executeCommand(command, user);
                    lastActivity = Instant.now().getEpochSecond();
                }
                else if(line.contains("PING"))
                {
                    chat.sendToServer(bw, "PONG :tmi.twitch.tv");
                }
                else if(line.contains("JOIN"))
                {
                    String user = line.substring(1, line.indexOf("!"));

                    System.out.println(user + " has joined " + CHANNEL + "'s Channel");
                }
                else if(line.contains("PART"))
                {
                    String user = line.substring(1, line.indexOf("!"));

                    System.out.println(user + " has left " + CHANNEL + "'s Channel");
                }
                else if(line.contains("WHISPER"))
                {
                    String user = line.substring(1, line.indexOf("!"));

                    String message = line.substring(line.indexOf(" :") + 2);

                    System.out.println(user + " ~~ " + message);
                }
                else if(line.contains("NOTICE") || line.contains("CLEARCHAT"))
                {
                    String message = line.substring(line.indexOf(" :") + 2);
                    System.out.println("!!! " + message);
                }
                if(!isRunning)
                {
                    chat.sendToServer(bw, "PING :tmi.twitch.tv");
                }
            }
            chat.sendToServer(bw, "PART #" + CHANNEL);
            s.close();
            bw.close();
            br.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    private void executeCommand(int command, String user)
    {
        if(command == -1) {
            System.out.println("shutting down");
            isRunning = false;
            return;
        }
        if(command == 1){
            live = sUtils.streamStatus();
            if (!live)
            chat.sendToChat(bw, "@" + user + sUtils.getUptime());
            else {
                chat.sendToChat(bw, "@" + user +" stream has been live for "+ sUtils.getUptime());
            }
            return;
        }
        if(command == 2){
            chat.sendToChat(bw, "@" + user +" "+ sUtils.getTitle());
            return;
        }
        if(command == 3){
            chat.sendToChat(bw, "@" + user +" "+ sUtils.getGame());
            return;
        }
        if(command == 4){
            chat.setPollId(sUtils.SetupStrawpoll(chat.getOptions()));
            chat.sendToChat(bw, "Новое голосование! https://strawpoll.me/"+chat.getPollId());
            chat.setOptions(new ArrayList<>());
        }
        if (command == 5){
            chat.sendToChat(bw, sUtils.GetVotesStrawpoll(chat.getPollId()));
        }
    }

    public void start()
    {
        if(isRunning)
        {
            return;
        }

        init();
        loop();
    }

}