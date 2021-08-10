package src;

import net.bytebuddy.agent.ByteBuddyAgent;
import java.io.File;


public class Launcher {
    public static void main(String[] args) {
        ByteBuddyAgent.attach(new File(args[0]), args[1]);
    }
}
