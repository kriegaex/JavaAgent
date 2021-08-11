package src;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.TypeStrategy;
import net.bytebuddy.asm.Advice;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;
import static net.bytebuddy.matcher.ElementMatchers.any;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.nameMatches;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class Agent {

  public final static HashMap<String, Stack<CompleteSTE>> s = new HashMap<String, Stack<CompleteSTE>>();

  public static void premain(String agentArgs, Instrumentation inst) {
    generalMain(agentArgs, inst);
  }

  public static void agentmain(String agentArgs, Instrumentation inst) {
    generalMain(agentArgs, inst);
  }

  public static void generalMain(String agentArgs, Instrumentation inst) {
    AgentBuilder mybuilder = new AgentBuilder.Default()
      .disableClassFormatChanges()
      .with(RETRANSFORMATION)
      // Make sure we see helpful logs
      .with(AgentBuilder.RedefinitionStrategy.Listener.StreamWriting.toSystemError())
      .with(AgentBuilder.Listener.StreamWriting.toSystemError()/*.withTransformationsOnly()*/)
      .with(AgentBuilder.InstallationListener.StreamWriting.toSystemError())
//      .with(InitializationStrategy.NoOp.INSTANCE)
//      .with(TypeStrategy.Default.DECORATE)
      .with(
        new AgentBuilder.InjectionStrategy.UsingInstrumentation(
          inst, new File("c:/Users/Alexa/Documents/java-src/SO_JavaAgent_ByteBuddy_68715127/agent/target/agent-1.0-SNAPSHOT.jar")
        )
      )
      .ignore(none())
      .ignore(
        nameStartsWith("net.bytebuddy.")
          .or(nameStartsWith("jdk.internal"))
          .or(nameStartsWith("java.lang.reflect"))
          .or(nameStartsWith("java.instrument"))
          .or(nameStartsWith("java.lang.WeakPairMap"))
          .or(nameStartsWith("java.security."))
          .or(nameStartsWith("sun.security."))
          .or(nameStartsWith("java.nio").and(nameContains("Buffer")))
          .or(nameStartsWith("java.nio.charset"))
          .or(nameStartsWith("sun.reflect"))
          .or(nameStartsWith("java.lang.invoke"))
          .or(nameStartsWith("com.sun.proxy"))
          .or(nameStartsWith("java.util.").and(not(nameStartsWith("java.util.UUID"))))
          .or(nameStartsWith("java.lang.Object"))
          .or(nameContains(".instrument"))
//          .or(nameContains("java.lang.Thread"))
          .or(nameContains("java.lang.Class"))
          .or(nameContains("java.lang.String"))
          .or(nameContains("java.lang.Character"))
          .or(nameContains("java.lang.PublicMethods"))
          .or(nameContains("java.lang.Long"))
          .or(nameContains("java.lang.Integer"))
          .or(nameContains("java.lang.Math"))
          .or(nameContains("java.util.Stack"))
          .or(nameContains("java.util.HashMap"))
          .or(nameContains("java.util.String"))
          .or(nameContains("java.util.Arrays"))
          .or(nameContains("StringBuilder"))
          .or(nameContains("Exception"))
          .or(nameContains("Throwable"))
          .or(nameStartsWith("sun."))
          .or(nameStartsWith("java.io.PrintStream"))
          .or(nameMatches("java[.]io[.].*Writer"))
          .or(nameStartsWith("java.lang.SecurityManager"))
          .or(nameStartsWith("java.lang.ref.SoftReference"))
          .or(nameStartsWith("java.io"))
          .or(nameStartsWith("java.lang"))
      );
    mybuilder.type(any().and(not(nameMatches("^src.Agent").or(nameMatches("^src.CompleteSTE")))))
      .transform((builder, type, classLoader, module) -> {
          try {
            return builder
              .visit(Advice.to(TraceAdvice.class).on(
                isMethod()
                  .and(not(nameContains("getMethod")))
                  .and(not(named("getSecurityManager")))
                  .and(not(named("valueOf")))
                  .and(not(named("toString")))
                  .and(not(named("stringSize")))
                  .and(not(named("getChars")))
                  .and(not(named("requireNonNull")))
                  .and(not(named("equals")))
                  .and(not(named("hashCode")))
                ));
          }
          catch (SecurityException e) {
            e.printStackTrace();
            return null;
          }
        }
      ).installOn(inst);
    System.out.println("Done");
  }

  public static String callStackToJSON() {
    String ret = "{";
    for (Map.Entry<String, Stack<CompleteSTE>> entry : s.entrySet()) {
      String key = entry.getKey();
      Stack<CompleteSTE> stk = entry.getValue();

      ret += "\"" + key + "\" : [";
      for (int i = 0; i < stk.size(); i++) {
        ret += "{";
        ret += "\"class\" : \"" + stk.get(i).clazz + "\", ";
        ret += "\"method\" : \"" + stk.get(i).method + "\", ";

        ret += "\"args_types\" : [";
        for (int j = 0; j < stk.get(i).args_types.length; j++) {
          ret += "\"" + stk.get(i).args_types[j] + "\"";
          if (j != stk.get(i).args_types.length - 1)
            ret += ", ";
        }
        ret += "], ";

        ret += "\"args\" : [";
        for (int j = 0; j < stk.get(i).args.length; j++) {
          ret += "\"" + stk.get(i).args[j] + "\"";
          if (j != stk.get(i).args.length - 1)
            ret += ", ";
        }
        ret += "]";
        ret += "}";
        if (i != stk.size() - 1)
          ret += ", ";
      }
      ret += "], ";
    }
    ret = ret.substring(0, ret.length() - 2);
    ret += "}";
    return ret;
  }
}
