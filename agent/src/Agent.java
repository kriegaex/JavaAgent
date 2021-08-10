package src;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.nameMatches;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.isSynthetic;
import static net.bytebuddy.matcher.ElementMatchers.none;
import static net.bytebuddy.matcher.ElementMatchers.any;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.InitializationStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.RedefinitionStrategy;
import net.bytebuddy.agent.builder.AgentBuilder.TypeStrategy;
import net.bytebuddy.asm.Advice;

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
        .with(AgentBuilder.Listener.StreamWriting.toSystemError().withTransformationsOnly())
        .with(AgentBuilder.InstallationListener.StreamWriting.toSystemError())
        .ignore(none())
        .ignore(
            nameStartsWith("net.bytebuddy.")
            .or(nameStartsWith("jdk.internal"))
            .or(nameStartsWith("java.lang.reflect"))
            .or(nameStartsWith("java.instrument"))
            .or(nameStartsWith("java.lang.WeakPairMap"))
            .or(nameStartsWith("java.nio").and(nameContains("Buffer")))
            .or(nameStartsWith("java.nio.charset"))
            .or(nameStartsWith("sun.reflect"))
            .or(nameStartsWith("java.lang.invoke"))
            .or(nameStartsWith("com.sun.proxy"))
            .or(nameStartsWith("java.util."))
            .or(nameStartsWith("java.lang.Object"))
            .or(nameContains(".instrument"))
            .or(nameContains("java.lang.Thread"))
            .or(nameContains("java.lang.Class"))
            .or(nameContains("Exception"))
            .or(nameContains("Throwable"))
            .or(nameStartsWith("sun."))
            .or(nameStartsWith("java.io"))
            .or(nameStartsWith("java.lang"))
            )
        .disableClassFormatChanges()
        .with(RedefinitionStrategy.RETRANSFORMATION)
        .with(InitializationStrategy.NoOp.INSTANCE)
        .with(TypeStrategy.Default.REDEFINE);
        mybuilder.type(any().and(not( nameMatches("^src.Agent").or(nameMatches("^src.CompleteSTE")) )))
        .transform((builder, type, classLoader, module) -> {
            try {
                return builder
                .visit(Advice.to(TraceAdvice.class).on(isMethod()));
                } catch (SecurityException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        ).installOn(inst);
        System.out.println("Done");
    }

    public static String callStackToJSON() {
        String ret = "{";
        for ( Map.Entry<String, Stack<CompleteSTE>> entry : s.entrySet()) {
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
                    if (j != stk.get(i).args_types.length - 1) ret += ", ";
                }
                ret += "], ";

                ret += "\"args\" : [";
                for (int j = 0; j < stk.get(i).args.length; j++) {
                    ret += "\"" + stk.get(i).args[j] + "\"";
                    if (j != stk.get(i).args.length - 1) ret += ", ";
                }
                ret += "]";
                ret += "}";
                if (i != stk.size() - 1) ret += ", ";
            }
            ret += "], ";
        }
        ret = ret.substring(0, ret.length() - 2); 
        ret += "}";
        return ret;
    }
}
    
