package som;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

public class Launcher  {

    private static final String GraalSOM = "GS";

    public static void main(final String[] arguments) throws IOException {
      String file = null;
      Source source;

      //TODO - this isn't right - ignored at the moment
      file = "core-lib/Examples/Echo.som";
      source = Source.newBuilder(GraalSOM, new File(file)).internal(true).buildLiteral();

      System.exit(executeSource(source, arguments));
    }

    private static int executeSource(final Source source, final String[] arguments) throws IOException {
        Context context;
        PrintStream err = System.err;

        Context.Builder builder = Context.newBuilder();
        builder.arguments(GraalSOM, arguments);

        // Try to build the context in which we will execute the source code
        try {
            context = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        // Try to evaluate the source code
        try {
            Value result = context.eval(source);
            return 0;
        } catch (PolyglotException ex) {
            if (ex.isInternalError()) {
                // for internal errors we print the full stack trace
                ex.printStackTrace();
            } else {
                err.println(ex.getMessage());
            }
            return 1;
        } finally {
            context.close();
        }
    }

    //TODO - adapt if from original som-java project
    private static boolean parseOption(final Map<String, String> options, final String arg) {
        if (arg.length() <= 2 || !arg.startsWith("--")) {
            return false;
        }
        int eqIdx = arg.indexOf('=');
        String key;
        String value;
        if (eqIdx < 0) {
            key = arg.substring(2);
            value = null;
        } else {
            key = arg.substring(2, eqIdx);
            value = arg.substring(eqIdx + 1);
        }

        if (value == null) {
            value = "true";
        }
        int index = key.indexOf('.');
        String group = key;
        if (index >= 0) {
            group = group.substring(0, index);
        }
        options.put(key, value);
        return true;
    }
}