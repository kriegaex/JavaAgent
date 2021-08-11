package org.acme.agent;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class Agent {

  public static void premain(String agentArgs, Instrumentation inst) throws Exception {
    generalMain(agentArgs, inst);
  }

  public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
    generalMain(agentArgs, inst);
  }

  public static void generalMain(String agentArgs, Instrumentation inst) throws Exception {
    // TODO: embed transformer and extract on the fly
    File transformerJar = new File(agentArgs);
    inst.appendToBootstrapClassLoaderSearch(new JarFile(transformerJar));
    Class.forName("org.acme.agent.Transformer"/*, true, null*/)
      .getMethod("init", Instrumentation.class, File.class)
      .invoke(null, inst, transformerJar);
}

}
