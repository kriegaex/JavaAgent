package org.acme.agent;

import net.bytebuddy.agent.ByteBuddyAgent;

import java.io.File;

public class Launcher {
  private static final String USAGE = "Launcher (agent attacher) arguments: agentJar processId";

  public static void main(String[] args) {
    if (args.length == 0)
      System.out.println(USAGE);
    else if (args.length != 2)
      System.err.println(USAGE);
    else {
      File agentJar = new File(args[0]);
      String processId = args[1];
      if (!agentJar.isFile())
        System.err.println("agentJar does not exist: " + agentJar);
      else
        ByteBuddyAgent.attach(agentJar, processId);
    }
  }
}
