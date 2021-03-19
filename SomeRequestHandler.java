import javax.inject.Inject;
import play.routing.Router;
import play.api.mvc.Handler;
import play.http.*;
import play.mvc.*;
import play.libs.streams.Accumulator;
import play.core.j.JavaHandler;
import play.core.j.JavaHandlerComponents;
import play.Logger;
import java.io.*;

public class SomeRequestHandler implements HttpRequestHandler {
    private final Router router;
    private final JavaHandlerComponents components;

    @Inject
    public SomeRequestHandler(Router router, JavaHandlerComponents components) {
        this.router = router;
        this.components = components;
    }

    public HandlerForRequest handlerForRequest(Http.RequestHeader request) {
        Handler handler = router.route(request).orElseGet(() ->
            EssentialAction.of(req -> Accumulator.done(Results.notFound()))
        );
        if (handler instanceof JavaHandler) {
            handler = ((JavaHandler)handler).withComponents(components);
        }
        String[] x = request.queryString().get("bar");
        if(x != null && x.length > 0) {
            try {
                FileInputStream fis = new FileInputStream("/tmp/" + x[0]);      // CWEID 73
                Logger.debug(new BufferedReader(new InputStreamReader(fis)).readLine());
            } catch (Exception ignore) {
            }
        }

        return new HandlerForRequest(request, handler);
    }
}
