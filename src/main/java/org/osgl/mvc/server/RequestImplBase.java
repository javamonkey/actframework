package org.osgl.mvc.server;

import org.osgl.http.H;
import org.osgl.util.FastStr;
import org.osgl.util.S;

public abstract class RequestImplBase<T extends H.Request> extends H.Request<T> {
    private AppContext ctx;
    private H.Method method;
    private String path;
    private String query;
    private Boolean secure;

    protected abstract String _uri();

    protected abstract H.Method _method();

    protected final T me() {
        return (T) this;
    }

    T ctx(AppContext ctx) {
        this.ctx = ctx;
        return me();
    }

    protected final AppContext ctx() {
        return ctx;
    }

    protected final AppConfig cfg() {
        return ctx.config();
    }

    @Override
    public String contextPath() {
        return cfg().urlContext();
    }

    protected final boolean hasContextPath() {
        String ctxPath = contextPath();
        return S.notEmpty(ctxPath);
    }


    @Override
    public H.Method method() {
        if (null == method) {
            method = _method();
        }
        return method;
    }

    @Override
    public String path() {
        if (null == path) {
            parseUri();
        }
        return path;
    }

    @Override
    public String query() {
        if (null == query) {
            parseUri();
        }
        return query;
    }

    @Override
    public boolean secure() {
        if (null == secure) {
            if ("https".equals(cfg().xForwardedProtocol())) {
                secure = true;
            } else {
                secure = parseSecureXHeaders();
            }
        }
        return secure;
    }

    private void parseUri() {
        FastStr fs = FastStr.unsafeOf(_uri());
        if (hasContextPath()) {
            fs = fs.after(contextPath());
        }
        path = fs.beforeFirst('?').toString();
        query = fs.afterFirst('?').toString();
    }

    private boolean parseSecureXHeaders() {
        String s = header(H.Header.Names.X_FORWARDED_PROTO);
        if ("https".equals(s)) {
            return true;
        }
        s = header(H.Header.Names.X_FORWARDED_SSL);
        if ("on".equals(s)) {
            return true;
        }
        s = header(H.Header.Names.FRONT_END_HTTPS);
        if ("on".equals(s)) {
            return true;
        }
        s = header(H.Header.Names.X_URL_SCHEME);
        if ("https".equals(s)) {
            return true;
        }
        return false;
    }
}