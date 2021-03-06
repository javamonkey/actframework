package act.xio;

import org.osgl._;
import org.osgl.exception.NotAppliedException;
import org.osgl.http.H;
import org.osgl.mvc.result.NotFound;
import act.app.App;
import act.app.AppContext;
import act.app.RequestRefreshClassLoader;
import act.app.RequestServerRestart;
import act.handler.RequestHandler;
import act.route.Router;
import org.osgl.util.E;

public class NetworkClient extends _.F1<AppContext, Void> {
    private App app;

    public NetworkClient(App app) {
        E.NPE(app);
        this.app = app;
    }

    public App app() {
        return app;
    }

    public void handle(AppContext ctx) {
        H.Request req = ctx.req();
        String url = req.url();
        H.Method method = req.method();
        try {
            app.detectChanges();
        } catch (RequestRefreshClassLoader refreshRequest) {
            app.refresh();
        } catch (RequestServerRestart requestServerRestart) {
            app.refresh();
        }
        try {
            RequestHandler rh = router().getInvoker(method, url, ctx);
            rh.handle(ctx);
        } catch (NotFound r) {
            r.apply(req, ctx.resp());
        } finally {
            ctx.clear();
        }
    }

    @Override
    public Void apply(AppContext ctx) throws NotAppliedException, _.Break {
        handle(ctx);
        return null;
    }

    private Router router() {
        return app.router();
    }
}
