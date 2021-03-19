package controllers;

import play.Logger;
import play.mvc.*;
import java.io.*;
import play.data.*;
import javax.inject.*;
import play.mvc.Http.*;
import akka.util.ByteString;
import java.nio.file.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import com.fasterxml.jackson.databind.node.*;
import org.w3c.dom.*;

import views.html.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
    @Inject() private FormFactory formFactory;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result another() {
        return ok("wat<script>alert(1);</script>").as("text/html");
    }

    public Result post1() {
        RequestBody b = request().body();
        String foo = b.asFormUrlEncoded().get("foo")[0];
        return ok("post1: " + foo).as("text/html");         // CWEID 80
    }

    public Result post2() {
        RequestBody bod = request().body();



        Http.RawBuffer rawbuf = bod.asRaw();
        if(rawbuf != null) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(rawbuf.asFile()));
                String bufstr = reader.readLine();
                return ok("bufstr: " + bufstr).as("text/html");         // CWEID 80
            } catch (Exception ignore) { 
                try {
                    BufferedReader reader = new BufferedReader(new FileReader("/etc/group"));
                    String bufstr = reader.readLine();
                    return ok("bufstr: " + bufstr).as("text/html");
                } catch (Exception ignore2) {
                }
            }
        }
        
        Document doc = bod.asXml();
        if(doc != null) {
            Element el = doc.getElementById("xyzzy");
            if(el != null) {
                String bad1 = el.getChildNodes().item(0).toString();
                String safe1 = "hi";
                Http.Cookie cook1 = new Http.Cookie(bad1, "xyz", 99999, "/", "localhost", false, false);       // CWEID 99
                Http.Cookie cook2 = new Http.Cookie(safe1, "xyz", 99999, "/", "localhost", false, false);
                Http.Cookie cook3 = new Http.Cookie(safe1, bad1, 99999, "/", "localhost", false, false);
                response().setCookie(cook1);
                response().setCookie(cook2);
                response().setCookie(cook3);
                response().setCookie("xyz", "abc");
                response().setCookie(bad1, "abc");  // CWEID 99
                response().setCookie(safe1, "abc");
                response().setCookie(safe1, bad1);

                return ok("elval: " + el.getChildNodes().item(0)).as("text/html");         // CWEID 80
            }
        }

        String str3 = bod.asText();
        if(str3 != null) {
            return redirect(str3);      // CWEID 601
        }

        String str1 = bod.as(String.class);
        if(str1 != null) {
            return ok("str1: " + str1).as("text/html");         // CWEID 80
        }

        JsonNode root = bod.asJson();
        if(root != null) {
            if(root instanceof ObjectNode) {
                Object o = ((ObjectNode)root).get("x");
                return ok("object x: " + o); // CWEID 80
            }
            if(root.asText() != null) {
                return ok("root.asText: " + root.asText()).as("text/html"); // CWEID 80
            }
            return ok("root: " + root); // CWEID 80
        }
        
        if(bod.asMultipartFormData() != null) {
            Http.MultipartFormData mpf = bod.asMultipartFormData();
            String[] arr = (String[])mpf.asFormUrlEncoded().get("x");
            if(arr != null) {
                return ok("mpf x: " + arr[0]);          // CWEID 80
            }
        }

        ByteString bs = bod.asBytes();
        if(bs != null) {
            return ok("str2: " + bs.utf8String()).as("text/html");    // CWEID 80
        }
        return ok("nothing").as("text/html");
    }

    public Result plainone() {
        Result ret = ok("plain1: " + request().getQueryString("foo"));     // CWEID 80
        return ret.as("text/html");
    }

    public Result redir1() {
        Http.Cookie cook = request().cookies().get("aaa");
        if(cook != null) {
            return found(cook.value());     // CWEID 601
        }
        String uname = request().username();
        if(uname != null) {
            return paymentRequired(uname).as("text/html");      // CWEID 80
        }
        String uri = request().uri();
        if(uri != null) {
            return unauthorized(uri).as("text/html");       // CWEID 80
        }
        String hdr = request().getHeader("Host");
        if(request().headers().get("Host") != null) {
            String hdr1 = request().headers().get("Host")[0];
            return internalServerError(hdr1);               // CWEID 80
        }
        if(hdr != null) {
            return status(200, hdr).as("text/html");        // CWEID 80
        }
        return ok("nothing");
    }

    public Result ctx1() {
        String[] foo1 = request().queryString().get("foo1");
        Http.Context ctxt = ctx();
        ctxt.args.put("safe1", "nothing");
        if(foo1 != null) {
            ctxt.args.put("bad1", foo1[0]);
            return ok("bad1: " + ctxt.args.get("bad1"));      // CWEID 80
        } else {
            return ok("safe1: " + ctxt.args.get("safe1"));
        }
    }
    public Result ctx2() {
        String foo1 = request().getQueryString("foo1");
        if(foo1 != null) {
            flash("bad1", foo1);
            flash().put("safe1", "nothing to see here");
            if(foo1.contains("x")) {
                return ok("bad1: " + flash().get("bad1"));      // CWEID 80
            } else {
                return ok("bad1: " + flash("bad1"));      // CWEID 80
            }
        } else {
            if(request().getQueryString("nonce") != null) {
                return ok("safe1: " + flash().get("safe1"));
            } else {
                return ok("safe1: " + flash("safe1"));
            }
        }
    }
    public Result ctx3() {
        String foo1 = request().getQueryString("foo1");
        if(foo1 != null) {
            session("bad1", foo1);
            session().put("safe1", "nothing to see here");
            if(foo1.contains("x")) {
                return ok("bad1: " + session().get("bad1"));      // CWEID 80
            } else {
                return ok("bad1: " + session("bad1"));      // CWEID 80
            }
        } else {
            if(request().getQueryString("nonce") != null) {
                return ok("safe1: " + session().get("safe1"));
            } else {
                return ok("safe1: " + session("safe1"));
            }
        }

    }
    public Result avc(long id) {
        String bad = ""+id;
        String safe = "this is ok";
        return ok(viewd.render(bad, safe));
    }

    public Result avb(String id) {
        String bad = id;
        String safe = "this is ok";
        return ok(viewc.render(bad, safe));
    }
    public Result viewb() {
        String bad = request().getQueryString("bar");
        String safe = "this is fine";
        return ok(viewb.render(bad, safe));
    }
    public Result viewa() {
        String[] bars = request().queryString().get("bar");
        String bar = "";
        if(bars != null) {
            bar = bars[0];
        }
        String[] safes = new String[] { "wat", "nothing" };
        return ok(viewa.render("foo", bar, bars, safes));
    }

    public Result form1() {
        Form<User> userForm = formFactory.form(User.class);
        User user = userForm.bindFromRequest().get();
        Map<String, String> ufmap = userForm.data();
        try {
            if(ufmap != null && ufmap.containsKey("email")) {
                FileInputStream fis = new FileInputStream("/tmp/" + ufmap.get("email"));        // CWEID 73
                Logger.debug(new BufferedReader(new InputStreamReader(fis)).readLine());
            }
        } catch (Exception ignore) {
        }

        Form<User> userForm2 = formFactory.form(User.class);
        HashMap map = new HashMap();
        map.put("email", "haha");
        User okuser = (User)userForm2.bind(map).get();
        Logger.debug("XXX: " + okuser.getEmail());
        String bad = user.getEmail();
        String safe = okuser.getEmail();
        Logger.debug("bad: " + bad);
        Logger.debug("safe: " + safe);
        return ok(viewe.render(bad, safe));
    }
    public Result form2() {
        Form<User> userForm = formFactory.form(User.class);
        HashMap badmap = new HashMap();
        badmap.put("email", request().getQueryString("email"));
        User user = (User)userForm.bind(badmap).get();
        Map<String, String> ufmap = userForm.data();
        try {
            if(ufmap != null && ufmap.containsKey("email")) {
                FileInputStream fis = new FileInputStream("/tmp/" + ufmap.get("email"));        // CWEID 73
                Logger.debug(new BufferedReader(new InputStreamReader(fis)).readLine());
            }
        } catch (Exception ignore) {
        }

        Form<User> userForm2 = formFactory.form(User.class);
        HashMap map = new HashMap();
        map.put("email", "haha");
        User okuser = (User)userForm2.bind(map).get();
        Logger.debug("XXX: " + okuser.getEmail());
        String bad = user.getEmail();
        Logger.debug("bad: " + bad);
        String safe = okuser.getEmail();
        Logger.debug("safe: " + safe);
        return ok(viewf.render(bad, safe));
    }
}
