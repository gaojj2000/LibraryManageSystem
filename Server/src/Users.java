import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMSource;
//import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Users {
    private static final Users instance = new Users();   // 单例
    private final String path = "users.xml";                   // xml文件路径（可设置则取消 final 关键字！）
    private Document document;                           // 文档对象
    private Element usersElement;                        // 用户标签
    private Map<String, User> userObjects;               // 用户对象

    // 单例模式
    private Users() {}
    public static Users getInstance() throws ParserConfigurationException {
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
                this.usersElement = (Element) this.document.getElementsByTagName("users").item(0);
                this.userObjects = this.getUsers();
            } catch (IOException | ParserConfigurationException | SAXException exception) {
                exception.printStackTrace();
                System.out.println("部分文件不存在！");
            }
        } else {
            this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            this.document.appendChild(this.document.createElement("users"));
            this.usersElement = (Element) this.document.getElementsByTagName("users").item(0);
            this.userObjects = new HashMap<>();
        }
    }

    private Map<String, User> getUsers() {
        Map<String, User> books = new HashMap<String, User>() {};
        for (Node node = this.usersElement.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {continue;}
            User user = new User(
                    ((Element) node).getAttribute("username"),
                    ((Element) node).getAttribute("password")
            );
            books.put(((Element) node).getAttribute("username"), user);
        }
        return books;
    }

    // 添加用户
    public void addUser(User user) {
        if (!this.userObjects.containsKey(user.getUsername())) {
            Element userElement = this.document.createElement("book");
            userElement.setAttribute("username", user.getUsername());
            userElement.setAttribute("password", user.getPassword());
            this.usersElement.appendChild(userElement);
            this.userObjects.put(user.getUsername(), user);
        }
    }

    // 删除用户
    public void deleteUser(String username) {
        for (Node node = this.usersElement.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {continue;}
            if (((Element) node).getAttribute("username").equals(username)) {
                this.usersElement.removeChild(node);
                this.userObjects.remove(username);
            }
        }
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

    // 判断用户存在
    public boolean checkUserExit(String username) {
        return this.userObjects.containsKey(username);
    }

    // 判断密码正确——接收用户对象
    public boolean verifyUserPassword(User user) {
        return this.verifyUserPassword(user.getUsername(), user.getPassword());
    }

    // 判断密码正确——接收账号密码字符串
    public boolean verifyUserPassword(String username, String password) {
        return checkUserExit(username) && this.userObjects.get(username).getPassword().equals(password);
    }
}
