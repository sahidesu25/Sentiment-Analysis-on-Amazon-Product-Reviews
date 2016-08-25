import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Crawler{

    public static ArrayList<Review> reviewList = new ArrayList<Review>();
    public static String PAGESFILE = "PagesScraped.txt";
    ArrayList<Integer>  pagesScraped;
    /**
     * Fetch all reviews for the item from Amazon.com
     */
    public void fetchReview(String itemID) {
        String url = "http://www.amazon.com/product-reviews/" + itemID
                + "/?showViewpoints=0&sortBy=byRankDescending&pageNumber=" + (1 + (int) (Math.random() * 10));
        //Modify this file name to reflect the name of the product you are reviewing.
        File file = new File("YahooMailReviews.txt");

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file, true);
            // Get the max number of review pages;
            org.jsoup.nodes.Document reviewpage1 = null;
            reviewpage1 = Jsoup.connect(url).timeout(10 * 1000).get();
            int maxpage = 1;
            Elements pagelinks = reviewpage1.select("a[href*=pageNumber=]");
            if (pagelinks.size() != 0) {
                ArrayList<Integer> pagenum = new ArrayList<Integer>();
                for (Element link : pagelinks) {
                    try {
                        pagenum.add(Integer.parseInt(link.text()));
                    } catch (NumberFormatException nfe) {
                    }
                }
                maxpage = Collections.max(pagenum);
            }
            // collect review from each of the review pages;
            int p = 0;
            pagesScraped = readArrayFromFile(PAGESFILE);
            //clean file for writing subsequent updated pages
            while (p <= maxpage) {
                p = 1 + (int) (Math.random() * maxpage);
                if(checkPagePresent(pagesScraped,p))
                    continue;
                pagesScraped.add(p);
                url = "http://www.amazon.com/product-reviews/"
                        + itemID
                        + "/?sortBy=helpful&pageNumber="
                        + p;
                org.jsoup.nodes.Document reviewpage = null;
                reviewpage = Jsoup.connect(url).timeout(10 * 1000).get();
                if (reviewpage.select("div.a-section.review").isEmpty()) {
                } else {
                    Elements reviewsHTMLs = reviewpage.select(
                            "div.a-section.review");
                    for (Element reviewBlock : reviewsHTMLs) {
                        Review theReview = parseReview(reviewBlock);
                        reviewList.add(theReview);
                        System.out.println(theReview);
                        try {
                            fileWriter.write(theReview.toString() + "\n");
                        } catch (IOException e) {
                         //   e.printStackTrace();
                        }
                    }
                }
                if(pagesScraped.size() > 0 && convertIntegers(pagesScraped) != null)
                    writeArrayToFile(PAGESFILE,pagesScraped);
            }

        } catch (Exception e) {
//            System.out.println(e);
//            System.out.println(itemID + " " + "Exception" + " " + e.getClass());
            try {
                Thread.sleep((int)(1000.0 + Math.random() * 10000));
                fetchReview(itemID);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    //helper to check if page has alread been crawled
    public static boolean checkPagePresent(ArrayList<Integer> arr, int targetValue) {
        for(int i: arr){
            if(i == targetValue)
                return true;
        }
        return false;
    }

    //helper function to read from file.
    public static ArrayList<Integer>  readArrayFromFile (String filename) throws IOException {
        ArrayList<Integer> ar = new ArrayList<Integer>();
        Scanner scanner = new Scanner(new File(filename));
        while (scanner.hasNextInt()) {
            ar.add(scanner.nextInt());
        }
        return ar;
    }

    //helper function to convert integers to int.
    public static int[] convertIntegers(ArrayList<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next();
        }
        return ret;
    }

    //helper function to write array to file
    public static void writeArrayToFile (String filename, ArrayList<Integer>  pages) throws IOException {
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter(filename));
        int[] arrPages = convertIntegers(pages);
        for (int i : arrPages) {
            outputWriter.write(Integer.toString(i));
            outputWriter.newLine();
        }
        outputWriter.close();
    }

    //parse review and get the details.
    public Review parseReview(Element reviewBlock) throws ParseException {
        String review_id = "";
        String title = "";
        int rating = 0;
        String reviewContent = "";
        String url = "";
        int classLabel = 0;

        // review id
        review_id = reviewBlock.id();

        // title
        Element reviewTitle = reviewBlock.select("a.review-title").first();
        title = reviewTitle.text();

        // rating
        Element star = reviewBlock.select("i.a-icon-star").first();
        String starinfo = star.text();
        rating = Integer.parseInt(starinfo.substring(0, 1));

        //class label
        if (rating > 3.0)
            classLabel = 1;
        else
            classLabel = 0;

        // review date
        Elements date = reviewBlock.select("span.review-date");
        String datetext = date.first().text();
        datetext = datetext.substring(3); // remove "On "
        Date reviewDate = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
                .parse(datetext);

        // review content
        Element contentDoc = reviewBlock.select("span.review-text").first();
        reviewContent = contentDoc.text();


        return new Review(review_id, title, rating, url, reviewDate, reviewContent, classLabel);

    }
}





