import java.io.Serializable;

public class Book implements Serializable {
    private final String bookType;  // 书类型
    private final String bookName;  // 书名
    private final String author;    // 书作者
    private final String info;      // 书简介
    private final String content;   // 书内容

    public Book(String bookType, String bookName, String author, String info, String content) {
        this.bookType = bookType;
        this.bookName = bookName;
        this.author = author;
        this.info = info;
        this.content = content;
    }

    public String getBookType() {
        return this.bookType;
    }

    public String getBookName() {
        return this.bookName;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getInfo() {
        return this.info;
    }

    public String getContent() {
        return this.content;
    }
}
