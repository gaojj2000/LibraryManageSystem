import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

public class Books {
    private static final Books instance = new Books();   // 单例
    private final String path = "books.xml";                   // xml文件路径（可设置则取消 final 关键字！）
    private Document document;                           // 文档对象
    private Map<String, Element> bookTypes;              // 书类型集合
    private Map<String, Map<String, Book>> bookObjects;  // xml文件内类型集合

    // 单例模式
    private Books() {}
    public static Books getInstance() throws ParserConfigurationException {
        instance.init();
        return instance;
    }

    // 获取当前程序执行地址
    /*
     * public static String getCurrentPath() {
     *     return new File("").getAbsolutePath();
     * }
     */

    // 检查文件存在
    public static boolean checkFileExist(String filepath) {
        return new File(filepath).exists();
    }

    // 读取txt文件
    public static String txtRead(String bookName) {
        try {
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(bookName + ".txt"));
            while ((line = bufferedReader.readLine()) != null) {stringBuilder.append(line).append("\t\r");}
            bufferedReader.close();
            return stringBuilder.toString();
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return "";
        }
    }

    // 写入txt文件
    public static void txtWrite(Book book) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(book.getBookName().toLowerCase() + ".txt"));
            bufferedWriter.write(book.getContent());
            bufferedWriter.flush();
            bufferedWriter.close();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // 设置文件路径
    /*
     * public void setPath(String filepath) throws ParserConfigurationException {
     *     this.path = filepath;
     *     this.init();
     * }
     */

    // 初始化加载
    private void init() throws ParserConfigurationException {
        if (checkFileExist(this.path)) {
            try {
                this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.path);
                this.bookTypes = this.getBookTypes();
                this.bookObjects = this.getBooks();
            } catch (IOException | ParserConfigurationException | SAXException exception) {
                exception.printStackTrace();
                System.out.println("部分文件不存在！");
            }
        } else {
            this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            this.document.appendChild(this.document.createElement("books"));
            this.bookTypes = new HashMap<>();
            this.bookObjects = new HashMap<>();
        }
    }

    // 获取书类型集合
    private Map<String, Element> getBookTypes() {
        Map<String, Element> types = new HashMap<String, Element>() {};
        NodeList nodeList = ((Element) this.document.getElementsByTagName("books").item(0)).getElementsByTagName("type");
        for (int index = 0; index < nodeList.getLength(); index++) {
            Element element = (Element) nodeList.item(index);
            types.put(element.getAttribute("value"), element);
        }
        return types;
    }

    // 获取xml文件内类型集合
    private Map<String, Map<String, Book>> getBooks() {
        Map<String, Map<String, Book>> books = new HashMap<String, Map<String, Book>>() {};
        for (Element element : this.bookTypes.values()) {
            Map<String, Book> temp = new HashMap<>();
            for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (node.getNodeType() != Node.ELEMENT_NODE) {continue;}
                Book book = new Book(
                        element.getAttribute("value"),
                        ((Element) node).getAttribute("name").toLowerCase(),
                        ((Element) node).getAttribute("author"),
                        ((Element) node).getAttribute("info"),
                        txtRead(((Element) node).getAttribute("name"))
                );
                temp.put(((Element) node).getAttribute("name"), book);
            }
            books.put(element.getAttribute("value"), temp);
        }
        return books;
    }

    // 创建书类型标签
    public void addBookType(String bookType) {
        Element bookTypeElement = this.document.createElement("type");
        bookTypeElement.setAttribute("value", bookType);
        this.document.getDocumentElement().appendChild(bookTypeElement);
        this.bookTypes.put(bookType, bookTypeElement);
        Map<String, Book> temp = new HashMap<>();
        this.bookObjects.put(bookType, temp);
    }

    // 删除书类型标签
    public void deleteBookType(String bookType) throws IOException {
        if (this.bookTypes.get(bookType) != null) {
            for (Book book : new ArrayList<>(this.bookObjects.get(bookType).values())) {
                this.deleteBook(book);
            }
            this.document.getDocumentElement().removeChild(this.bookTypes.get(bookType));
            this.bookTypes.remove(bookType);
            this.bookObjects.remove(bookType);
        }
    }

    // 添加书本
    public void addBook(Book book) throws IOException {
        Element bookElement = this.document.createElement("book");
        bookElement.setAttribute("name", book.getBookName().toLowerCase());
        bookElement.setAttribute("author", book.getAuthor());
        bookElement.setAttribute("info", book.getInfo());
        if (this.bookTypes.get(book.getBookType()) == null) {
            addBookType(book.getBookType());
        } else if (this.bookObjects.get(book.getBookType()).containsKey(book.getBookName().toLowerCase())) {
            this.deleteBook(book);  // 仅书名不同才删除原来数据，写入新数据！
        }
        this.bookTypes.get(book.getBookType()).appendChild(bookElement);
        this.bookObjects.get(book.getBookType()).put(book.getBookName().toLowerCase(), book);
        txtWrite(book);
    }

    // 删除书本
    public void deleteBook(Book book) throws IOException {
        File file = new File(book.getBookName().toLowerCase() + ".txt");
        if (file.exists()) {
            if (!file.delete()) {
                System.out.println("文件 " + file.getName() + " 删除失败。");
                throw new IOException();
            }
        }
        for (Node node = this.bookTypes.get(book.getBookType()).getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {continue;}
            if (((Element) node).getAttribute("name").equals(book.getBookName().toLowerCase())) {
                this.bookTypes.get(book.getBookType()).removeChild(node);
                this.bookObjects.get(book.getBookType()).remove(book.getBookName());
            }
        }
    }

    // 通过书类型获取书列表
    public List<Book> getBooksByType(String bookType) {
        Map<String, Book> temp = this.bookObjects.get(bookType);
        if (temp != null) {
            return new ArrayList<>(temp.values());
        }
        return new ArrayList<>();
    }

    // 获取书类型字符串列表
    public List<String> getBookTypesString() {
        return new ArrayList<>(this.bookTypes.keySet());
    }

    // 通过书名获取书对象
    public Book getBookByName(String bookName) {
        for (String bookType : this.bookObjects.keySet()) {
            if (this.bookObjects.get(bookType).containsKey(bookName.toLowerCase())) {
                return this.bookObjects.get(bookType).get(bookName.toLowerCase());
            }
        }
        return null;
    }

    // 保存xml文件
    public void xmlSave() throws IOException {  // , TransformerException
        /*
         * TransformerFactory transformerFactory = TransformerFactory.newInstance();
         * Transformer transformer = transformerFactory.newTransformer();
         * DOMSource domSource = new DOMSource(this.document);
         * StreamResult streamResult = new StreamResult(new FileOutputStream(this.path));
         * transformer.transform(domSource, streamResult);
         */
        // TransformerFactory.newInstance().newTransformer().transform(new DOMSource(this.document), new StreamResult(new FileOutputStream(this.path)));
        OutputFormat outputFormat = new OutputFormat(this.document);
        outputFormat.setLineWidth(65);
        outputFormat.setIndenting(true);
        outputFormat.setIndent(4);
        Writer writer = new StringWriter();
        XMLSerializer xmlSerializer = new XMLSerializer(writer, outputFormat);
        xmlSerializer.serialize(this.document);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(this.path));
        bufferedWriter.write(writer.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }
}
