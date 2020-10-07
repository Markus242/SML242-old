import java.io.BufferedWriter;
import java.time.Instant;


import static java.lang.Thread.sleep;


public class Botthread implements Runnable {
    private BufferedWriter bw;

    private Chat chat;
    private StreamUtils sUtils;

    private boolean live;
    private long Start;
    private int totalfoll;
    private String lastfollname;


    public Botthread(BufferedWriter bw, Chat chat, StreamUtils sUtils){
        this.bw = bw;
        this.chat = chat;
        this.sUtils = sUtils;

        live = sUtils.streamStatus();
        Start = Instant.now().getEpochSecond();

        try {
            totalfoll = sUtils.getFollowerNum();
            lastfollname = sUtils.getFollowerName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                //timed functions must be off when stream's offline
                if (live){
                    if (totalfoll < sUtils.getFollowerNum()) {
                        while (lastfollname.equals(sUtils.getFollowerName())) sleep(4000);
                        chat.greetFollower(bw, sUtils.getFollowerName());
                    }
                    totalfoll = sUtils.getFollowerNum();
                    lastfollname = sUtils.getFollowerName();
                    }
                live = sUtils.streamStatus();
                try {
                    sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("Thread faulty, look above");
        }
    }
}
