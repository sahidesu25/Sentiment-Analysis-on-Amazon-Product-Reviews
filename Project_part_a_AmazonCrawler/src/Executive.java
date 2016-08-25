import java.io.IOException;


public class Executive {
    public static void main(String args[]) throws IOException {
        //call the crawler from the executive
        //http://www.amazon.com/Mojang-Minecraft-Pocket-Edition/dp/B00992CF6W/ref=sr_1_1?s=mobile-apps&ie=UTF8&qid=1462571451&sr=1-1&keywords=minecraft
        //for above url, the product id is B00992CF6W, simlarly extract all the product ids from the url and pass to this executive in code.
        Crawler crawler = new Crawler();
        crawler.fetchReview("B00632HWOG");//YahooMail
    }
}
