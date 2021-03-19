import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import java.util.concurrent.CompletionStage;
import play.Logger;
import java.io.*;

import java.lang.reflect.Method;

public class SomeActionCreator implements play.http.ActionCreator {
    @Override
    public Action createAction(Http.Request request, Method actionMethod) {
        return new Action.Simple() {
            @Override
            public CompletionStage<Result> call(Http.Context ctx) {
				Logger.debug("SomeActionCreator");
                String[] x = request.queryString().get("bar");
                if(x != null && x.length > 0) {
                    try {
                        FileInputStream fis = new FileInputStream("/tmp/" + x[0]);      // CWEID 73
                        Logger.debug(new BufferedReader(new InputStreamReader(fis)).readLine());
                    } catch (Exception ignore) {
                    }
                }
                return delegate.call(ctx);
            }
        };
    }
}
