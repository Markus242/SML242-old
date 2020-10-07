import com.google.gson.*;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class StreamUtils {
    private String CHANNEL;
    private String CLIENT;
    private String BEARER;
    private String ID;

    public StreamUtils(String CHANNEL, String CLIENT, String OAUTH)
    {
        this.CHANNEL = CHANNEL.toLowerCase();
        this.CLIENT = CLIENT.toLowerCase();
        this.BEARER = OAUTH.substring(6);
        try {
            this.ID = getChannelId(CHANNEL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUrl(String urlString) throws Exception {
        String inputLine = "";
        StringBuilder end = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
            conn.setRequestProperty("Authorization", "Bearer "+ BEARER);
            conn.setRequestProperty("Client-ID",CLIENT);
            conn.setRequestMethod("GET");
            InputStream inputStream = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            while ((inputLine = in.readLine()) != null){
                end.append(inputLine);
            }
            in.close();
            inputStream.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return end.toString();
    }

    public int SetupStrawpoll(ArrayList<String> options){
        try {
            URL url = new URL("https://www.strawpoll.me/api/v2/polls");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");
            Poll poll = new Poll(CHANNEL,options);
            Gson gson = new Gson();
            OutputStream os = conn.getOutputStream();
            os.write(gson.toJson(poll, Poll.class).getBytes());
            InputStream stream = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(stream);
            PollResponce r = gson.fromJson(isr,PollResponce.class);
            os.close();
            conn.disconnect();
            return r.id;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String GetVotesStrawpoll(int id){
        try {
            URL url = new URL("https://www.strawpoll.me/api/v2/polls/"+id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type","application/json");
            InputStream stream = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(stream);
            VotesResponce r = new Gson().fromJson(isr,VotesResponce.class);
            StringBuilder sb = new StringBuilder();
            Iterator<String> iter1 = r.options.iterator();
            Iterator<Integer> iter2 = r.votes.iterator();
            while (iter1.hasNext() && iter2.hasNext()){
                sb.append(iter1.next());
                sb.append(" - ");
                sb.append(iter2.next());
                sb.append(", ");
            }
            conn.disconnect();
            return sb.toString().substring(0,sb.length()-2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "None were found";
    }

    public String getChannelId(String name) throws Exception {
        String json = getUrl("https://api.twitch.tv/helix/users?login="+name);
        return new JsonParser().parse(json).getAsJsonObject().get("data").getAsJsonArray()
                .get(0).getAsJsonObject().get("id").getAsString();
    }

    public int getFollowerNum(){
        int numfoll=0;
        try {
            String json = getUrl("https://api.twitch.tv/helix/users/follows?to_id="+ID);
            numfoll = new JsonParser().parse(json).getAsJsonObject().get("total").getAsInt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return numfoll;
    }

    public String getFollowerName(){
        String json = null;
        String from_name = null;
        try {
            json = getUrl("https://api.twitch.tv/helix/users/follows?to_id="+ID);
            from_name = new JsonParser().parse(json).getAsJsonObject().get("data").getAsJsonArray()
                .get(0).getAsJsonObject().get("from_name").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return from_name;
    }

    public Boolean streamStatus(){
        try {
            String streamInfo =  getUrl("https://api.twitch.tv/kraken/streams/"+ID);
            JsonElement status = new JsonParser().parse(streamInfo).getAsJsonObject().get("stream");
            if (!status.isJsonNull()){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUptime(){
        if (streamStatus()) {
            String streamInfo = "";
            try {
                streamInfo = getUrl("https://api.twitch.tv/kraken/streams/" + ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            JsonParser parser = new JsonParser();
            JsonObject stream = parser.parse(streamInfo).getAsJsonObject().getAsJsonObject("stream");
            ZonedDateTime startdt = ZonedDateTime.parse(stream.get("created_at").getAsString());
            Duration duration = Duration.between(startdt, ZonedDateTime.now(ZoneId.of("UTC")));
            return DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true);
        }
        else return " stream is offline";
    }

    public String getGame(){
        String game=null;
        try {
            game = new JsonParser().parse(getUrl("https://api.twitch.tv/kraken/channels/"+ID))
                    .getAsJsonObject().get("game").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return game;
    }

    public String getTitle(){
        String game=null;
        try {
            game = new JsonParser().parse(getUrl("https://api.twitch.tv/kraken/channels/"+ID))
                    .getAsJsonObject().get("status").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return game;
    }

    public String[] getChatters(){
        List<String> list = new ArrayList<String>();
        try {
            JsonArray arr = new JsonParser().parse(getUrl("https://tmi.twitch.tv/group/user/"+CHANNEL+"/chatters"))
                    .getAsJsonObject().get("chatters").getAsJsonObject().get("viewers").getAsJsonArray();
            for(int i = 0; i < arr.size(); i++){
                list.add(arr.get(i).getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.toArray(String[]::new);
    }

    public String[] getMods(){
        List<String> list = new ArrayList<String>();
        try {
            JsonArray arr = new JsonParser().parse(getUrl("https://tmi.twitch.tv/group/user/"+CHANNEL+"/chatters"))
                    .getAsJsonObject().get("chatters").getAsJsonObject().get("moderators").getAsJsonArray();
            for(int i = 0; i < arr.size(); i++){
                list.add(arr.get(i).getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.toArray(String[]::new);
    }
}

class Poll{
    Poll(String channel, ArrayList<String> options){
        this.title="New poll ordered by "+channel;
        this.options=options;
    }
    String title;
    ArrayList<String> options;
}

class PollResponce{
    int id;
    String title;
    ArrayList<String> options;
}

class VotesResponce{
    ArrayList<String> options;
    ArrayList<Integer> votes;
}

