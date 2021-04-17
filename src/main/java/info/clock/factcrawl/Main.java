
package info.clock.factcrawl;

/**
 *
 * @author Jae
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        FactCrawl fc = new FactCrawl(args[0], 2);
        fc.crawlComplete();
        fc.getFacts().stream().map(f -> f.toString()).forEach(f -> System.out.println(f));
    }
    
}
