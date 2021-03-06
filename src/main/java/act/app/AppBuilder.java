package act.app;

import act.Act;
import org.osgl.util.E;
import org.osgl.util.IO;

import java.io.File;
import static act.app.RuntimeDirs.*;

/**
 * Build Application when Act running in DEV mode.
 */
class AppBuilder {

    // When Act running is other mode than DEV mode, there is no need to
    // build an app as the app has already been built and deployed
    private static final AppBuilder PLACE_HOLDER = new AppBuilder() {
        @Override
        public void build() {
            // just placeholder
        }
    };

    private final App app;
    private final ProjectLayout layout;
    private final File appBase;
    private File tgtLib;
    private File tgtAsset;
    private File tgtClasses;
    private File tgtConf;

    private AppBuilder() {
        app = null;
        layout = null;
        appBase = null;
    }

    private AppBuilder(App app) {
        this.app = app;
        this.layout = app.layout();
        this.appBase = app.base();
    }

    public void build() {
        prepareTargetDir();
        copyFiles();
    }

    private void prepareTargetDir() {
        File target = layout.target(appBase);
        verifyDir(target, "target", true);
        tgtClasses = verifyDir(new File(target, CLASSES), CLASSES, true);
        tgtLib = verifyDir(new File(target, LIB), LIB, true);
        tgtAsset = verifyDir(new File(target, ASSET), ASSET, true);
        tgtConf = verifyDir(new File(tgtClasses, CONF), CONF, true);
    }

    private void copyFiles() {
        copyLibs();
        copyAssets();
        copyResources();
        copyConf();
        copyRoutes();
    }

    void copyAssets() {
        File asset = layout.asset(appBase);
        if (null == asset || !asset.canRead()) {
            return;
        }
        verifyDir(asset, "asset", false);
        IO.copyDirectory(asset, tgtAsset);
    }

    void copyResources() {
        File resource = layout.resource(appBase);
        if (null == resource || !resource.canRead()) {
            return;
        }
        verifyDir(resource, "resource", false);
        IO.copyDirectory(resource, tgtClasses);
    }

    void copyResources(File... files) {
        File base = layout.resource(appBase);
        int baseLen = base.getAbsolutePath().length();
        for (File file : files) {
            String path = file.getAbsolutePath().substring(baseLen);
            File target = new File(tgtClasses, path);
            IO.copyDirectory(file, target);
        }
    }

    void copyRoutes() {
        File routes = layout.routeTable(appBase);
        if (null == routes || !routes.canRead()) return;
        if (routes.exists() && routes.canRead()) {
            IO.copyDirectory(routes, RuntimeDirs.routes(app));
        }
    }

    void copyConf() {
        File conf = layout.conf(appBase);
        if (null == conf || !conf.canRead()) return;
        if (conf.isDirectory()) {
            IO.copyDirectory(conf, tgtConf);
        } else {
            IO.copyDirectory(conf, new File(tgtConf, conf.getName()));
        }
    }

    void copyLibs() {
        File lib = layout.lib(appBase);
        if (null == lib || !lib.exists()) {
            return;
        }
        verifyDir(lib, "lib", false);
        IO.copyDirectory(lib, tgtLib);
    }

    private File verifyDir(File dir, String label, boolean create) {
        if (create && !dir.exists()) {
            E.unexpectedIf(!dir.mkdirs(), "Cannot create %s dir %s for %s", dir, label, app);
            return dir;
        }
        E.unexpectedIf(!dir.isDirectory(), "%s %s is not a directory", label, dir);
        E.unexpectedIf(!dir.canRead(), "Cannot read %s dir %s", label, dir);
        E.unexpectedIf(!dir.canWrite(), "Cannot write %s dir %s", label, dir);
        return dir;
    }

    public static AppBuilder build(App app) {
        AppBuilder builder = PLACE_HOLDER;
        if (Act.mode() == Act.Mode.DEV) {
            builder = new AppBuilder(app);
        }
        builder.build();
        return builder;
    }
}
