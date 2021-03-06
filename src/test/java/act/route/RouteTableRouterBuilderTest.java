package act.route;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgl.http.H;
import org.osgl.mvc.result.NotFound;
import act.handler.RequestHandler;
import act.handler.RequestHandlerResolver;
import act.handler.builtin.Echo;
import act.handler.builtin.StaticFileGetter;
import act.TestBase;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.osgl.http.H.Method.*;

public class RouteTableRouterBuilderTest extends RouterTestBase {

    private static final String SVC_ID_ALL_ONE = "111111111111111111111111";
    private RouteTableRouterBuilder builder;

    @Override
    protected void provisionControllerLookup(RequestHandlerResolver controllerLookup) {
        when(controllerLookup.resolve(anyString())).thenAnswer(new Answer<RequestHandler>() {
            @Override
            public RequestHandler answer(InvocationOnMock invocation) throws Throwable {
                return new NamedMockHandler((String) invocation.getArguments()[0]);
            }
        });
    }

    @Override
    protected void buildRouteMapping(Router router) {
    }

    @Test
    public void withAnyMethod () {
        addRouteMap("* /somewhere Application.foo");
        for (H.Method m: Router.supportedHttpMethods()) {
            verify("Application.foo", m, "/somewhere");
        }
    }

    @Test
    public void withDynamicPath() {
        addRouteMap("GET /service/{id}/cost Services.cost");
        verify("Services.cost", GET, "/service/abc/cost");
        Mockito.verify(ctx).param("id", "abc");
    }

    @Test
    public void withDynamicPathAndRegex() {
        addRouteMap("GET /service/{<[0-9]{3}>id}/cost Services.cost");
        verify("Services.cost", GET, "/service/123/cost");
        Mockito.verify(ctx).param("id", "123");
    }

    @Test(expected = NotFound.class)
    public void dynamicPathNotMatchRegEx() {
        addRouteMap("GET /service/{<[0-9]{3}>id}/cost Services.cost");
        verify("Services.cost", GET, "/service/1234/cost");
    }

    @Test
    public void withBuiltInHandler() {
        addRouteMap("GET /public staticDir:/public");
        RequestHandler h = router.getInvoker(GET, "/public/file1.txt", ctx);
        yes(h instanceof StaticFileGetter);
        eq("/public", TestBase.fieldVal(h, "base"));
    }

    @Test
    public void payloadContainsBlank() {
        addRouteMap("GET /magic_words echo: Hello world!");
        RequestHandler h = router.getInvoker(GET, "/magic_words", ctx);
        yes(h instanceof Echo);
        eq("Hello world!", TestBase.fieldVal(h, "msg"));
    }

    private void verify(String expected, H.Method method, String url) {
        router.getInvoker(method, url, ctx).handle(ctx);
        controllerInvoked(expected);
    }

    private void controllerInvoked(String payload) {
        eq(payload, NamedMockHandler.getName());
    }

    private void addRouteMap(String routeMap) {
        builder = new RouteTableRouterBuilder(routeMap);
        builder.build(router);
    }

}
