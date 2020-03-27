package som;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.debug.DebuggerTags;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.RootNode;

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
        //TODO - leave the object system initialisation in the initializeContext method, along the lines of...
//        args = context.handleArguments(args);
//        try {
//            context.initializeObjectSystem();
//        } catch (ProgramDefinitionError e) {
//            e.printStackTrace();
//        }
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
                    return context.interpret(args);
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
