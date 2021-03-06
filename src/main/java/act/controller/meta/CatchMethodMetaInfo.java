package act.controller.meta;

import org.osgl.util.C;
import org.osgl.util.S;

import java.util.List;

/**
 * Unlike other interceptors (Before/After/Finally), Catch interceptor
 * has a special attribute: value, the exception class. Only when the
 * exception thrown out is instance of the class or subclass of the class,
 * the catch interceptor will be executed
 */
public class CatchMethodMetaInfo extends InterceptorMethodMetaInfo {
    private static final List<String> CATCH_THROWABLE = C.list(Throwable.class.getName());
    private List<String> targetExceptionClassNames = CATCH_THROWABLE;

    public CatchMethodMetaInfo(ControllerClassMetaInfo clsInfo) {
        super(clsInfo);
    }

    public CatchMethodMetaInfo exceptionClasses(List<String> list) {
        targetExceptionClassNames = C.list(list);
        return this;
    }

    public List<String> exceptionClasses() {
        return targetExceptionClassNames;
    }

    @Override
    public String toString() {
        StringBuilder sb = S.builder("catch").append(targetExceptionClassNames).append(" ").append(super.toString());
        return sb.toString();
    }

}
