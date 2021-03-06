package act.controller.meta;

import act.asm.Type;
import act.util.AsmTypes;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.C;

import java.util.List;
import java.util.Map;

public class ControllerClassMetaInfoManager {

    private static final Logger logger = L.get(ControllerClassMetaInfoManager.class);

    private Map<String, ControllerClassMetaInfo> controllers = C.newMap();
    private Map<Type, List<ControllerClassMetaInfo>> subTypeInfo = C.newMap();

    public ControllerClassMetaInfoManager() {
    }

    public void registerControllerMetaInfo(ControllerClassMetaInfo metaInfo) {
        String className = Type.getObjectType(metaInfo.className()).getClassName();
        controllers.put(className, metaInfo);
        if (metaInfo.isController()) {
            Type superType = metaInfo.superType();
            if (!AsmTypes.OBJECT_TYPE.equals(superType)) {
                ControllerClassMetaInfo superInfo = controllerMetaInfo(superType.getClassName());
                if (null != superInfo) {
                    metaInfo.parent(superInfo);
                } else {
                    List<ControllerClassMetaInfo> subTypes = subTypeInfo.get(superType);
                    if (null == subTypes) {
                        subTypes = C.newList();
                    }
                    subTypes.add(metaInfo);
                }
            }
        }
        List<ControllerClassMetaInfo> subTypes = subTypeInfo.get(metaInfo.type());
        if (null != subTypes) {
            for (ControllerClassMetaInfo subTypeInfo : subTypes) {
                subTypeInfo.parent(metaInfo);
            }
            subTypeInfo.remove(metaInfo.type());
        }
        logger.trace("Controller meta info registered for: %s", className);
    }

    public ControllerClassMetaInfo controllerMetaInfo(String className) {
        return controllers.get(className);
    }

    public void mergeActionMetaInfo() {
        for (ControllerClassMetaInfo info : controllers.values()) {
            info.merge(this);
        }
    }

}
