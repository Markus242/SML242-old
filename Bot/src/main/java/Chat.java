import java.io.BufferedWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


public class Chat {
    private String USERNAME;
    private String CHANNEL;
    private long lastF = 0;
    private long lastPog = 0;


    private ArrayList<String> options;
    private int pollId;


    Chat(String USERNAME, String CHANNEL) {
        this.USERNAME = USERNAME;
        this.CHANNEL = CHANNEL;
        this.options = new ArrayList<>();
        this.pollId = 0;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public void setOptions(ArrayList<String> options) {
        this.options = options;
    }

    public int getPollId() {
        return pollId;
    }

    public void setPollId(int pollId) {
        this.pollId = pollId;
    }

    int processCommands(BufferedWriter bw, String user, String message) {
        String[] command = message.split(" ");

        for (int i = 0; i < command.length; i++) {
            if (command[0].equals("!shutdown")) {
                if (user.equals(CHANNEL) || user.equals("markus242")) {
                    sendToChat(bw, "Слушаюсь и повинуюсь BibleThump 7");
                    return -1;
                } else sendToChat(bw, "Эта хипстерская команда только* для стримера FeelsBadMan");
                return 0;
            } else if (command[i].equals("!ping")) {
                sendToChat(bw, "Бот жив :)");
                return 0;
            } else if (command[0].equals("!help")) {
                sendToChat(bw, "@" + user + " available commands: !ping, !uptime, !roll, !title, !game");
                return 0;
            } else if (command[i].equals("!uptime")) {
                return 1;
            } else if (command[i].equals("!title")) {
                return 2;
            } else if (command[i].equals("!game")) {
                return 3;
            } else if (message.toLowerCase().contains("sml242")) {
                if (message.contains("GivePLZ"))
                    sendToChat(bw, "@" + user + " TakeNRG");
                else if (message.contains("TakeNRG"))
                    sendToChat(bw, "@" + user + " GivePLZ");
                return 0;
            } else if (message.equals("F") || message.contains("Kappa 7") || message.equals("RIP")) {
                if (Instant.now().getEpochSecond() - lastF >= 60) {
                    sendToChat(bw, "Kappa 7");
                    lastF = Instant.now().getEpochSecond();
                }
                return 0;
            } else if (message.contains("PogChamp")) {
                if (Instant.now().getEpochSecond() - lastPog >= 60) {
                    sendToChat(bw, "PogChamp");
                    lastPog = Instant.now().getEpochSecond();
                }
                return 0;
            } else if (command[0].equals("!roll")) {
                if (user.equals("antr1x")) {
                    sendToChat(bw,"@"+user+", 20");
                    return 0;
                }
                try {
                    int result = 0;
                    if (message.equals("!roll")) result = rolldice("d100");
                    else if (!command[1].equals("")) {
                        String[] dices = message.split(" ");
                        for (int j = 1; j < command.length; j++) {
                            result += rolldice(dices[j]);
                        }
                    } else result = rolldice("d100");
                    sendToChat(bw, "@" + user + ", " + result);
                } catch (Exception e) {
                    sendToChat(bw, "Слишком сложно, бот запутался (кубики в формате XdY+N, каждый через пробел) SMOrc");
                }
                return 0;
            } else if (command[0].equals("!poll") && (user.equals("markus242") || user.equals(CHANNEL))) {
                try {
                    String msg = message.substring(6);
                    String[] arr = msg.split(",");
                    options.addAll(Arrays.asList(arr));
                    return 4;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            } else if (command[0].equals("!votes") && (user.equals("markus242")||(user.equals(CHANNEL)))) {
                try {
                    if (pollId!=0){
                        return 5;
                    }
                    else sendToChat(bw,"Голосования на стриме ещё не было :)");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        }
        return 0;
    }

    private int rolldice(String d) {
        int nod = 1;
        int nSides = 100;
        int rest = 0;
        if (!d.equals("")) {
            String[] dice = d.split("d");

            if (dice[0].equals("")) {
                dice[0] = "1";
            }
            if (dice[1].contains("+")) {
                rest = Integer.parseInt(dice[1].split("\\+")[1]);
                dice[1] = dice[1].split("\\+")[0];
            } else if (dice[1].contains("-")) {
                rest = Integer.parseInt(dice[1].split("-")[1]) * (-1);
                dice[1] = dice[1].split("-")[0];
            }
            nod = Integer.parseInt(dice[0]);
            nSides = Integer.parseInt(dice[1]);
        }
        int roll;
        int num = 0;
        Random r = new Random();
        for (int j = 0; j < nod; j++) {
            roll = r.nextInt(nSides) + 1;
            num = num + roll;
        }
        return num + rest;
    }


    public void greetFollower(BufferedWriter bw, String name) {
        if (name != null) sendToChat(bw, "Спасибо за фоллоу, " + name + " Kappa /");
    }

    public void sendToServer(BufferedWriter bw, String message) {
        try {
            bw.write(message + "\r\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToChat(BufferedWriter bw, String message) {
        sendToServer(bw, "PRIVMSG #" + CHANNEL + " :" + message);
        System.out.println(USERNAME + " >> " + message);
    }

    public void sendToUser(BufferedWriter bw, String user, String message) {
        sendToChat(bw, "/w " + user + " " + message);
        System.out.println(USERNAME + " ~" + user + "~ " + message);
    }
}