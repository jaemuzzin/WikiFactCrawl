import com.google.gson.Gson;
import info.clock.factcrawl.FactCrawl;
import io.javalin.Javalin;

/**
 *
 * @author Jae
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {int port=8080;
        if(System.getenv("PORT")!=null){
            port = Integer.parseInt(System.getenv("PORT"));
        }
        Javalin app = Javalin.create().start(port);
        app.get("/test", ctx -> {
            ctx.result("hello world");
        });
        app.get("/q", ctx -> {
            FactCrawl fc = new FactCrawl(ctx.queryParam("wikiDataID", "Q1"), Math.min(5,Integer.parseInt(ctx.queryParam("maxDepth", "1"))));
            fc.crawlComplete();
            StringBuilder out = new StringBuilder();
            out.append("[");
            fc.getFacts().stream().map(f -> new Gson().toJson(f)).forEach(f -> out.append(f+",\n "));            
            out.append("]");
             ctx.result(out.toString());
        });
    }
    
}
