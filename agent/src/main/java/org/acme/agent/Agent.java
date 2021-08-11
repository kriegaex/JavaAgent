package org.acme.agent;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

public class Agent {

  public static void premain(String agentArgs, Instrumentation inst) throws Exception {
    generalMain(agentArgs, inst);
  }

  public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
    generalMain(agentArgs, inst);
  }

  public static void generalMain(String agentArgs, Instrumentation inst) throws Exception {
    try (InputStream agentDepsStream = Agent.class.getClassLoader().getResourceAsStream("agent-deps.jar")) {
      Path agentDepsJar = Files.createTempFile(null, ".jar");
      Files.copy(agentDepsStream, agentDepsJar, StandardCopyOption.REPLACE_EXISTING);
      agentDepsJar.toFile().deleteOnExit();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> agentDepsJar.toFile().delete()));
      inst.appendToBootstrapClassLoaderSearch(new JarFile(agentDepsJar.toFile()));
    }
    Class.forName("org.acme.agent.Transformer")
      .getMethod("init", Instrumentation.class)
      .invoke(null, inst);
  }

}
