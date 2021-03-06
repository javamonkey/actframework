package act.view.rythm;

import act.view.TemplateBase;
import org.osgl.http.H;
import act.app.App;
import act.app.AppContext;
import org.osgl.util.E;
import org.osgl.util.IO;
import org.rythmengine.RythmEngine;
import org.rythmengine.template.ITemplate;

import java.util.Map;

public class RythmTemplate extends TemplateBase {

    private RythmEngine engine;
    private String path;

    public RythmTemplate(RythmEngine engine, String path, App app) {
        E.NPE(engine);
        this.engine = engine;
        this.path = path;
    }

    @Override
    protected void prepareMerge(AppContext context) {
        // TODO set locale
    }

    @Override
    protected void merge(Map<String, Object> renderArgs, H.Response response) {
        // TODO handle render exception
        ITemplate t = template(renderArgs);
        String s = t.render();
        IO.writeContent(s, response.writer());
    }

    private org.rythmengine.template.ITemplate template(Map<String, Object> renderArgs) {
        return engine.getTemplate(path, renderArgs);
    }
}
