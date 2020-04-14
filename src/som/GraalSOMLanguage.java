package som;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Option;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.RootNode;

import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.options.OptionKey;
import som.compiler.ProgramDefinitionError;
import som.vm.Universe;
import som.vmobjects.SAbstractObject;

@TruffleLanguage.Registration(id = GraalSOMLanguage.ID,
        name = "GraalSOM",
        defaultMimeType = GraalSOMLanguage.MIME_TYPE,
        characterMimeTypes = GraalSOMLanguage.MIME_TYPE,
        contextPolicy = TruffleLanguage.ContextPolicy.SHARED,
        fileTypeDetectors = GSFileDetector.class)
@ProvidedTags({StandardTags.CallTag.class,
        StandardTags.StatementTag.class,
        StandardTags.RootTag.class,
        StandardTags.RootBodyTag.class,
        StandardTags.ExpressionTag.class,
        DebuggerTags.AlwaysHalt.class})
public final class GraalSOMLanguage extends TruffleLanguage<Universe> {

    public static final String ID = "GS";
    public static final String MIME_TYPE = "application/x-graal-som";
    public static String[] args;

    @Option(help = "For Testing purpose only - Selector of the test ran (see BasicInterpreterTests>>testSomeTest)", category = OptionCategory.USER) public static final OptionKey<String> TestSelector = new OptionKey<>("");
    @Option(help = "For Testing purpose only - Class of the test ran (see BasicInterpreterTests>>testSomeTest)", category = OptionCategory.USER) public static final OptionKey<String> TestClass = new OptionKey<>("");
    @Option(help = "For Testing purpose only - Required classpath to execute a given TestClass>>TestSelector (see BasicInterpreterTests>>testSomeTest)", category = OptionCategory.USER) public static final OptionKey<String> TestClasspath = new OptionKey<>("");

    public GraalSOMLanguage() {
    }

    @Override
    protected Universe createContext(final Env env) {
        return new Universe(env, this);
    }

    @Override
    protected void initializeContext(final Universe context) {
        Env currentEnv = context.getEnv();
        args = currentEnv.getApplicationArguments();
        //TODO - on the long run, it would be better to rely on source file
        // the arguments may not be accessed from here if this approach is used
        // they should be pre processed in the Launcher
//        args = context.handleArguments(args);
//        try {
//            context.initializeObjectSystem();
//        } catch (ProgramDefinitionError e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        // this class is generated by the annotation processor
        return new GraalSOMLanguageOptionDescriptors();
    }

    @Override
    protected boolean isObjectOfLanguage(final Object object) {
        return object instanceof SAbstractObject;
        //TODO - should throw an exception
    }

    public static Universe getCurrentContext() {
        return getCurrentContext(GraalSOMLanguage.class);
    }

    @Override
    protected CallTarget parse(final ParsingRequest request) throws Exception {
        Universe context = getCurrentContext();
        return Truffle.getRuntime().createCallTarget(new RootNode(this) {

            @Override
            public Object execute(final VirtualFrame frame) {
                try {
                    if (context.isForTesting()) {
                        return context.interpret(args, context.testClass(), context.testSelector());
                    }
                    else return context.interpret(args);
                    //TODO - leave the object system initialisation in the initializeContext method, along the lines of...
//                    SInvokable initialize = context.systemClass.lookupInvokable(context.symbolFor("initialize:"));
//                    return context.interpretMethod(context.systemObject, initialize, context.newArray(args) );
                }
                catch (ProgramDefinitionError e) {
                    GraalSOMLanguage.getCurrentContext().errorExit(e.getMessage());
                    return 1;
                }
            }
        });
    }
}
