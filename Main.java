import java.io.*;
import java.util.*;

abstract class Person {
    protected String username, password;
    Person(String u, String p) { username = u; password = p; }
    boolean login(Scanner sc) {
        System.out.print("Username: ");
        String u = sc.next();
        System.out.print("Password: ");
        String p = sc.next();
        if (u.equals(username) && p.equals(password)) {
            System.out.println("Login Successful!");
            return true;
        }
        System.out.println("Wrong Username or Password!");
        return false;
    }
    abstract void menu(Scanner sc);
}

class Admin extends Person {
    Admin(String u, String p) { super(u, p); }
    void menu(Scanner sc) { Main.adminMenu(sc); }
}

class Student extends Person {
    Student(String u, String p) { super(u, p); }
    void menu(Scanner sc) { Main.studentMenu(sc); }
}

class Book {
    private int id, qty, borrowed;
    private String title, author;

    Book(int id, String title, String author, int qty, int borrowed) {
        this.id = id; this.title = title; this.author = author;
        this.qty = qty; this.borrowed = borrowed;
    }

    int getId() { return id; }
    String getTitle() { return title; }
    String getAuthor() { return author; }
    int getQty() { return qty; }
    int getBorrowed() { return borrowed; }

    void setQty(int qty) { this.qty = qty; }
    void borrowBook() { qty--; borrowed++; }
    void returnBook() { qty++; if (borrowed > 0) borrowed--; }

    public String toString() {
        return id + "," + title + "," + author + "," + qty + "," + borrowed;
    }

    static Book fromString(String s) {
        String[] d = s.split(",");
        return new Book(Integer.parseInt(d[0]), d[1], d[2], Integer.parseInt(d[3]), Integer.parseInt(d[4]));
    }

    void show() {
        System.out.println("ID: " + id + " | " + title + " | " + author + " | Stock: " + qty + " | Borrowed: " + borrowed);
    }
}

public class Main {
    static ArrayList<Book> books = new ArrayList<>();
    static HashMap<String, ArrayList<Integer>> studentBooks = new HashMap<>();
    static final String FILE = "books.txt";
    static int nextId = 1;
    static Student currentStudent;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        loadBooks();

        System.out.println("=== Set Admin Account ===");
        System.out.print("Enter Admin Username: ");
        Admin admin = new Admin(sc.next(), readPass(sc));

        System.out.println("\n=== Set Student Account ===");
        System.out.print("Enter Student Username: ");
        Student student = new Student(sc.next(), readPass(sc));

        while (true) {
            System.out.println("\n=== Library Management System ===");
            System.out.println("1. Admin Login");
            System.out.println("2. Student Login");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            int ch = readInt(sc);

            switch (ch) {
                case 1 -> { if (admin.login(sc)) admin.menu(sc); }
                case 2 -> {
                    if (student.login(sc)) {
                        currentStudent = student;
                        studentBooks.putIfAbsent(student.username, new ArrayList<>());
                        student.menu(sc);
                    }
                }
                case 3 -> { saveBooks(); System.out.println("System Closed."); return; }
                default -> System.out.println("Invalid Choice!");
            }
        }
    }

    static String readPass(Scanner sc) {
        System.out.print("Enter Password: ");
        return sc.next();
    }

    static int readInt(Scanner sc) {
        while (!sc.hasNextInt()) {
            System.out.print("Enter valid number: ");
            sc.next();
        }
        return sc.nextInt();
    }

    static void adminMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- ADMIN PANEL ---");
            System.out.println("1. Add Book");
            System.out.println("2. View Books");
            System.out.println("3. Search Book");
            System.out.println("4. Remove Book");
            System.out.println("5. Update Quantity");
            System.out.println("6. Library Stats");
            System.out.println("7. Logout");
            System.out.print("Choice: ");
            int ch = readInt(sc);

            switch (ch) {
                case 1 -> addBook(sc);
                case 2 -> viewBooks();
                case 3 -> searchBook(sc);
                case 4 -> removeBook(sc);
                case 5 -> updateQty(sc);
                case 6 -> stats();
                case 7 -> { System.out.println("Admin Logout Successful."); return; }
                default -> System.out.println("Invalid Choice!");
            }
        }
    }

    static void studentMenu(Scanner sc) {
        while (true) {
            System.out.println("\n--- STUDENT PANEL ---");
            System.out.println("1. View Books");
            System.out.println("2. Search Book");
            System.out.println("3. Borrow Book");
            System.out.println("4. Return Book");
            System.out.println("5. My Books");
            System.out.println("6. Logout");
            System.out.print("Choice: ");
            int ch = readInt(sc);

            switch (ch) {
                case 1 -> viewBooks();
                case 2 -> searchBook(sc);
                case 3 -> borrowBook(sc);
                case 4 -> returnBook(sc);
                case 5 -> myBooks();
                case 6 -> { System.out.println("Student Logout Successful."); return; }
                default -> System.out.println("Invalid Choice!");
            }
        }
    }

    static void addBook(Scanner sc) {
        System.out.print("Enter Title: ");
        String title = sc.next();
        System.out.print("Enter Author: ");
        String author = sc.next();
        System.out.print("Enter Quantity: ");
        int qty = readInt(sc);
        books.add(new Book(nextId++, title, author, qty, 0));
        saveBooks();
        System.out.println("Book Added Successfully!");
    }

    static void viewBooks() {
        if (books.isEmpty()) { System.out.println("No Books Available!"); return; }
        System.out.println("\n--- BOOK LIST ---");
        for (Book b : books) b.show();
    }

    static void searchBook(Scanner sc) {
        System.out.print("Enter Title Keyword: ");
        String key = sc.next().toLowerCase();
        boolean found = false;
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(key)) {
                b.show();
                found = true;
            }
        }
        if (!found) System.out.println("Book Not Found!");
    }

    static Book findById(int id) {
        for (Book b : books) if (b.getId() == id) return b;
        return null;
    }

    static void removeBook(Scanner sc) {
        System.out.print("Enter Book ID: ");
        int id = readInt(sc);
        Book b = findById(id);
        if (b == null) System.out.println("Book Not Found!");
        else {
            books.remove(b);
            saveBooks();
            System.out.println("Book Removed Successfully!");
        }
    }

    static void updateQty(Scanner sc) {
        System.out.print("Enter Book ID: ");
        int id = readInt(sc);
        Book b = findById(id);
        if (b == null) System.out.println("Book Not Found!");
        else {
            System.out.print("Enter New Quantity: ");
            b.setQty(readInt(sc));
            saveBooks();
            System.out.println("Quantity Updated!");
        }
    }

    static void borrowBook(Scanner sc) {
        System.out.print("Enter Book ID: ");
        int id = readInt(sc);
        Book b = findById(id);
        if (b == null) System.out.println("Book Not Found!");
        else if (b.getQty() <= 0) System.out.println("Out of Stock!");
        else {
            b.borrowBook();
            studentBooks.get(currentStudent.username).add(id);
            saveBooks();
            System.out.println("Book Borrowed Successfully!");
        }
    }

    static void returnBook(Scanner sc) {
        System.out.print("Enter Book ID: ");
        int id = readInt(sc);
        ArrayList<Integer> list = studentBooks.get(currentStudent.username);
        if (!list.contains(id)) System.out.println("You did not borrow this book!");
        else {
            Book b = findById(id);
            if (b != null) b.returnBook();
            list.remove((Integer) id);
            saveBooks();
            System.out.println("Book Returned Successfully!");
        }
    }

    static void myBooks() {
        ArrayList<Integer> list = studentBooks.get(currentStudent.username);
        if (list.isEmpty()) { System.out.println("No borrowed books."); return; }
        System.out.println("--- MY BOOKS ---");
        for (int id : list) {
            Book b = findById(id);
            if (b != null) b.show();
        }
    }

    static void stats() {
        int totalTitles = books.size(), totalStock = 0, totalBorrowed = 0;
        for (Book b : books) {
            totalStock += b.getQty();
            totalBorrowed += b.getBorrowed();
        }
        System.out.println("Total Titles: " + totalTitles);
        System.out.println("Available Stock: " + totalStock);
        System.out.println("Total Borrow Count: " + totalBorrowed);
    }

    static void saveBooks() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Book b : books) pw.println(b);
        } catch (Exception e) {
            System.out.println("Error Saving File!");
        }
    }

    static void loadBooks() {
        try {
            File f = new File(FILE);
            if (!f.exists()) return;
            Scanner fs = new Scanner(f);
            while (fs.hasNextLine()) {
                Book b = Book.fromString(fs.nextLine());
                books.add(b);
                nextId = Math.max(nextId, b.getId() + 1);
            }
            fs.close();
        } catch (Exception e) {
            System.out.println("Error Loading File!");
        }
    }
}
