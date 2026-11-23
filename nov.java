import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Scanner;

public class Main {

    static final String URL = "https://books.toscrape.com/";
    static final int MAX = 20;

    static String[] titles = new String[MAX];
    static String[] authors = new String[MAX];
    static String[] links = new String[MAX];
    static int count = 0;
    static int readingCount = 0;

    static String[] readingTitles = new String[MAX];
    static String[] readingAuthors = new String[MAX];
    static String[] readingLinks = new String[MAX];

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            printMenu();
            System.out.print("Enter your choice: ");
            choice = readIntSafe(scanner);

            switch (choice) {
                case 1:
                    exploreBooks();
                    break;
                case 2:
                    if (count == 0) {
                        System.out.println("Explore books first (Option 1).");
                    } else {
                        System.out.print("Enter title keyword: ");
                        String titleKeyword = scanner.nextLine();
                        searchBook(titleKeyword);
                    }
                    break;
                case 3:
                    if (count == 0) {
                        System.out.println("Explore books first (Option 1).");
                    } else {
                        System.out.print("Enter author keyword: ");
                        String authorKeyword = scanner.nextLine();
                        searchBook(authorKeyword, true);
                    }
                    break;
                case 4:
                    addBookToReadingList(scanner);
                    break;
                case 5:
                    viewReadingList();
                    break;
                case 6:
                    removeBookFromReadingList(scanner);
                    break;
                case 7:
                    displayBookDetails(scanner);
                    break;
                case 0:
                    System.out.println("Exiting program. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 0);

        scanner.close();
    }

    static void printMenu() {
        System.out.println("\n--- BOOK EXPLORER MENU ---");
        System.out.println("1. Explore Books");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Author");
        System.out.println("4. Add Book to Reading List");
        System.out.println("5. View Reading List");
        System.out.println("6. Remove Book");
        System.out.println("7. Display Book Details");
        System.out.println("0. Exit");
    }

    static void exploreBooks() {
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements books = doc.select("article.product_pod");
            count = 0;

            System.out.println("\n--- EXPLORED BOOKS ---");
            for (int idx = 0; idx < books.size() && count < MAX; idx++) {
                Element a = books.get(idx).selectFirst("h3 > a");
                if (a != null) {
                    String title = a.attr("title").trim();
                    String relHref = a.attr("href");
                    String absUrl = a.absUrl("href");

                    String author = fetchAuthorFromBookPage(absUrl);

                    titles[count] = title;
                    authors[count] = author;
                    links[count] = absUrl.isEmpty() ? relHref : absUrl;

                    System.out.println((count + 1) + ". " + titles[count]);
                    count++;
                }
            }

            if (count == 0) {
                System.out.println("No books found.");
            }
        } catch (Exception e) {
            System.out.println("Error fetching books: " + e.getMessage());
        }
    }

    static String fetchAuthorFromBookPage(String bookUrl) {
        if (bookUrl == null || bookUrl.isEmpty()) {
            return "Unknown Author";
        }
        try {
            Document bookDoc = Jsoup.connect(bookUrl).get();
            Element authorRow = bookDoc.selectFirst("table.table-striped tr:has(th:containsOwn(Author)) td");
            if (authorRow != null && !authorRow.text().trim().isEmpty()) {
                return authorRow.text().trim();
            }
        } catch (Exception ignored) {
        }
        return "Unknown Author";
    }

    // Overloaded method 1: Search by title
    static void searchBook(String title) {
        String keyword = title.trim().toLowerCase();
        boolean found = false;

        for (int i = 0; i < count; i++) {
            if (titles[i].toLowerCase().contains(keyword)) {
                System.out.println((i + 1) + ". " + titles[i]);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching book title found.");
        }
    }

    // Overloaded method 2: Search by author when true, otherwise by title
    static void searchBook(String keyword, boolean searchByAuthor) {
        if (!searchByAuthor) {
            searchBook(keyword);
            return;
        }

        String query = keyword.trim().toLowerCase();
        boolean found = false;

        for (int i = 0; i < count; i++) {
            if (authors[i].toLowerCase().contains(query)) {
                System.out.println((i + 1) + ". " + titles[i] + " | Author: " + authors[i]);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matching author found.");
        }
    }

    static void addBookToReadingList(Scanner scanner) {
        if (count == 0) {
            System.out.println("Explore books first (Option 1).");
            return;
        }
        if (readingCount >= MAX) {
            System.out.println("Reading list is full.");
            return;
        }

        System.out.print("Enter book number to add (1-" + count + "): ");
        int num = readIntSafe(scanner);

        if (num < 1 || num > count) {
            System.out.println("Invalid book number.");
            return;
        }

        int idx = num - 1;
        for (int i = 0; i < readingCount; i++) {
            if (readingTitles[i].equalsIgnoreCase(titles[idx])) {
                System.out.println("Book is already in your reading list.");
                return;
            }
        }

        readingTitles[readingCount] = titles[idx];
        readingAuthors[readingCount] = authors[idx];
        readingLinks[readingCount] = links[idx];
        readingCount++;

        System.out.println("Added to reading list: " + titles[idx]);
    }

    static void viewReadingList() {
        if (readingCount == 0) {
            System.out.println("Reading list is empty.");
            return;
        }

        System.out.println("\n--- READING LIST ---");
        for (int i = 0; i < readingCount; i++) {
            System.out.println((i + 1) + ". " + readingTitles[i] + " (" + readingAuthors[i] + ")");
        }
    }

    static void removeBookFromReadingList(Scanner scanner) {
        if (readingCount == 0) {
            System.out.println("Reading list is empty.");
            return;
        }

        viewReadingList();
        System.out.print("Enter item number to remove (1-" + readingCount + "): ");
        int num = readIntSafe(scanner);

        if (num < 1 || num > readingCount) {
            System.out.println("Invalid item number.");
            return;
        }

        int idx = num - 1;
        String removedTitle = readingTitles[idx];

        for (int i = idx; i < readingCount - 1; i++) {
            readingTitles[i] = readingTitles[i + 1];
            readingAuthors[i] = readingAuthors[i + 1];
            readingLinks[i] = readingLinks[i + 1];
        }

        readingTitles[readingCount - 1] = null;
        readingAuthors[readingCount - 1] = null;
        readingLinks[readingCount - 1] = null;
        readingCount--;

        System.out.println("Removed from reading list: " + removedTitle);
    }

    static void displayBookDetails(Scanner scanner) {
        if (count == 0) {
            System.out.println("Explore books first (Option 1).");
            return;
        }

        System.out.print("Enter book number for details (1-" + count + "): ");
        int num = readIntSafe(scanner);

        if (num < 1 || num > count) {
            System.out.println("Invalid book number.");
            return;
        }

        int idx = num - 1;
        System.out.println("\n--- BOOK DETAILS ---");
        System.out.println("Title : " + titles[idx]);
        System.out.println("Author: " + authors[idx]);
        System.out.println("Link  : " + links[idx]);
    }

    static int readIntSafe(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        int val = scanner.nextInt();
        scanner.nextLine();
        return val;
    }
}
