package org.acme.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;

import static net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy.RETRANSFORMATION;
import static net.bytebuddy.matcher.ElementMatchers.*;

public class Transformer {

  private static final String AGENT_PACKAGE = "org.acme.agent.";
  private static final String BYTEBUDDY_PACKAGE = "net.bytebuddy.";

  private static final List<String> WARM_UP_CLASSES = Arrays.asList(
    "java.util.Stack",
    AGENT_PACKAGE + "TraceStackInfo",
    AGENT_PACKAGE + "CompleteSTE"
  );

  private static final ElementMatcher.Junction<TypeDescription> IGNORED_TYPES =
    nameStartsWith(AGENT_PACKAGE)
      .or(nameStartsWith(BYTEBUDDY_PACKAGE))
      .or(nameStartsWith("com.sun.proxy"))
      .or(nameStartsWith("java.instrument."))
      .or(nameStartsWith("java.io"))
      .or(nameStartsWith("java.lang"))
      .or(nameStartsWith("java.lang.invoke"))
      .or(nameStartsWith("java.lang.reflect."))
      .or(nameStartsWith("java.nio").and(nameContains("Buffer")))
      .or(nameStartsWith("java.nio.charset"))
      .or(nameStartsWith("java.security."))
      .or(nameStartsWith("java.util.").and(not(nameStartsWith("java.util.UUID"))))
      .or(nameStartsWith("jdk.internal."))
      .or(nameStartsWith("sun."))
      .or(nameStartsWith("sun.reflect"))
      .or(nameStartsWith("sun.security."))
      .or(named("java.io.PrintStream"))
      .or(named("java.lang.Character"))
      .or(named("java.lang.Class"))
      .or(named("java.lang.Integer"))
      .or(named("java.lang.Long"))
      .or(named("java.lang.Math"))
      .or(named("java.lang.Object"))
      .or(named("java.lang.PublicMethods"))
      .or(named("java.lang.SecurityManager"))
      .or(named("java.lang.String"))
      .or(named("java.lang.StringBuilder"))
      .or(named("java.lang.Throwable"))
      .or(named("java.lang.WeakPairMap"))
      .or(named("java.lang.ref.SoftReference"))
      .or(named("java.util.Arrays"))
      .or(named("java.util.HashMap"))
      .or(named("java.util.Stack"))
      .or(named("java.util.String"))
      .or(nameEndsWith("Exception"))
      .or(nameMatches(".*[.]instrument[.].*"))
      .or(nameMatches("java[.]io[.].*Writer"))
      .or(nameMatches("java[.]net[.]URL.*"));

  private static final ElementMatcher.Junction<MethodDescription> TARGET_METHODS =
    isMethod()
      .and(not(nameContains("getMethod")))
      .and(not(named("equals")))
      .and(not(named("getChars")))
      .and(not(named("getSecurityManager")))
      .and(not(named("hashCode")))
      .and(not(named("requireNonNull")))
      .and(not(named("stringSize")))
      .and(not(named("toString")))
      .and(not(named("valueOf")));

  public static void init(Instrumentation inst) throws ClassNotFoundException {
    warmUpPlatformClassLoader();
    installTransformer(inst);
    System.out.println("Transformer installed");
  }

  /**
   * Avoid "ClassCircularityError: .../TraceStackInfo" by pre-loading some classes before installing advice
   *
   * @throws ClassNotFoundException
   */
  private static void warmUpPlatformClassLoader() throws ClassNotFoundException {
    // Java 9+: ClassLoader.getPlatformClassLoader()
    ClassLoader platformClassLoader = ClassLoader.getSystemClassLoader().getParent();
    for (String warmUpClass : WARM_UP_CLASSES) {
      platformClassLoader.loadClass(warmUpClass);
    }
  }

  private static void installTransformer(Instrumentation inst) {
    new AgentBuilder.Default()
      .disableClassFormatChanges()
      .with(RETRANSFORMATION)
      .with(RedefinitionStrategy.Listener.StreamWriting.toSystemError())
      .with(AgentBuilder.Listener.StreamWriting.toSystemError()/*.withTransformationsOnly()*/)
      .with(AgentBuilder.InstallationListener.StreamWriting.toSystemError())
      .ignore(none())
      .ignore(IGNORED_TYPES)
      .type(any())
      .transform((builder, type, classLoader, module) ->
        builder.visit(
          Advice.to(TraceAdvice.class).on(TARGET_METHODS)
        )
      )
      .installOn(inst);
  }

}
