package act.controller.meta;

import act.asm.Label;
import act.asm.Type;
import act.handler.builtin.controller.ControllerAction;
import act.handler.builtin.controller.Handler;
import act.util.AsmTypes;
import act.util.Prioritised;
import org.osgl._;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.Map;

/**
 * Common meta data storage for both {@link ControllerAction}
 * and {@link Handler}
 */
public abstract class HandlerMethodMetaInfo<T extends HandlerMethodMetaInfo> implements Prioritised {
    private String name;
    private InvokeType invokeType;
    private AppContextInjection appContextInjection;
    private ControllerClassMetaInfo clsInfo;
    private C.List<ParamMetaInfo> params = C.newList();
    private ReturnTypeInfo returnType;
    private Map<Label, Map<Integer, LocalVariableMetaInfo>> locals = C.newMap();
    private int appCtxLVT_id = -1;

    public HandlerMethodMetaInfo(ControllerClassMetaInfo clsInfo) {
        this.clsInfo = clsInfo;
    }

    public ControllerClassMetaInfo classInfo() {
        return clsInfo;
    }

    public T name(String name) {
        this.name = name;
        return me();
    }

    public String name() {
        return name;
    }

    public String fullName() {
        return S.builder(clsInfo.className()).append(".").append(name()).toString();
    }

    @Override
    public int priority() {
        return -1;
    }

    public T appContextViaField(String fieldName) {
        appContextInjection = new AppContextInjection.FieldAppContextInjection(fieldName);
        return me();
    }

    public T appContextViaParam(int paramIndex) {
        appContextInjection = new AppContextInjection.ParamAppContextInjection(paramIndex);
        return me();
    }

    public T appContextViaLocalStorage() {
        appContextInjection = new AppContextInjection.LocalAppContextInjection();
        return me();
    }

    public AppContextInjection appContextInjection() {
        return appContextInjection;
    }

    public T invokeStaticMethod() {
        invokeType = InvokeType.STATIC;
        return me();
    }

    public T invokeInstanceMethod() {
        invokeType = InvokeType.VIRTUAL;
        return me();
    }

    public boolean isStatic() {
        return InvokeType.STATIC == invokeType;
    }

    public T returnType(Type type) {
        returnType = ReturnTypeInfo.of(type);
        return me();
    }

    public T appCtxLocalVariableTableIndex(int index) {
        appCtxLVT_id = index;
        return me();
    }

    public int appCtxLocalVariableTableIndex() {
        return appCtxLVT_id;
    }

    public Type returnType() {
        return returnType.type();
    }

    public boolean hasReturn() {
        return returnType.hasReturn();
    }

    public boolean hasLocalVariableTable() {
        return !locals.isEmpty();
    }

    public HandlerMethodMetaInfo addParam(ParamMetaInfo param) {
        params.add(param);
        if (AsmTypes.APP_CONTEXT.equals(param.type())) {
        }
        return this;
    }

    public T addLocal(LocalVariableMetaInfo local) {
        Label start = local.start();
        Map<Integer, LocalVariableMetaInfo> m = locals.get(start);
        if (null == m) {
            m = C.newMap();
            locals.put(start, m);
        }
        int index = local.index();
        E.illegalStateIf(m.containsKey(index), "Local variable index conflict");
        m.put(local.index(), local);
        return me();
    }

    public LocalVariableMetaInfo localVariable(int index, Label start) {
        Map<Integer, LocalVariableMetaInfo> l = locals.get(start);
        if (null == l) return null;
        return l.get(index);
    }

    public ParamMetaInfo param(int id) {
        return params.get(id);
    }

    public int paramCount() {
        return params.size();
    }

    @Override
    public int hashCode() {
        return _.hc(fullName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof HandlerMethodMetaInfo) {
            HandlerMethodMetaInfo that = (HandlerMethodMetaInfo) obj;
            return _.eq(that.fullName(), fullName());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = S.builder(appContextInjection.toString()).append(" ");
        sb.append(_invokeType())
                .append(_return())
                .append(fullName())
                .append("(")
                .append(_params())
                .append(")");
        return sb.toString();
    }

    private String _invokeType() {
        switch (invokeType) {
            case VIRTUAL:
                return "";
            case STATIC:
                return "static ";
            default:
                assert false;
                return "";
        }
    }

    private String _return() {
        if (returnType.hasReturn()) {
            return returnType.type().getClassName() + " ";
        } else {
            return "";
        }
    }

    private String _params() {
        return S.join(", ", params.map(new _.Transformer<ParamMetaInfo, String>() {
            @Override
            public String transform(ParamMetaInfo paramMetaInfo) {
                return paramMetaInfo.type().getClassName();
            }
        }));
    }

    private T me() {
        return (T) this;
    }

}
