package org.acme.agent;

import net.bytebuddy.agent.ByteBuddyAgent;

import java.io.File;

public class Launcher {
  private static final String USAGE = "Launcher (agent attacher) arguments: agentJar transformerJar processId";

  public static void main(String[] args) {
    if (args.length == 0)
      System.out.println(USAGE);
    else if (args.length != 3)
      System.err.println(USAGE);
    else {
      File agentJar = new File(args[0]);
      String transformerJar = args[1];
      String processId = args[2];
      if (!agentJar.isFile())
        System.err.println("agentJar does not exist: " + agentJar);
      else if (!new File(transformerJar).isFile())
        System.err.println("transformerJar does not exist: " + transformerJar);
      else
        ByteBuddyAgent.attach(agentJar, processId, transformerJar);
    }
  }
}
