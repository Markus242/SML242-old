public class Config {
    private static final String HOST = "irc.chat.twitch.tv";
    private static final int PORT = 6667;
    private static final String PASSWORD = "oauth:xxx";
    private static final String USERNAME = "sml242";
    private static final String CLIENT = "zzz";
    private static final String CHANNEL = "markus242";


    public static void main(String[] args)
    {
        TwitchBot twitchBot = new TwitchBot(USERNAME, HOST, PASSWORD, CHANNEL, PORT, CLIENT);
        twitchBot.start();
    }
}

