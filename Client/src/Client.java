import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static Socket socket = null;
    private static ObjectInputStream objectInputStream = null;
    private static ObjectOutputStream objectOutputStream = null;
    private static final Scanner input = new Scanner(System.in);

    public static void main(String[] args) throws ClassNotFoundException {
        try {
            socket = new Socket("localhost", 12345);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("连接到服务器：" + socket.getRemoteSocketAddress());
            System.out.println("欢迎来到图书管理系统！");
            menu1();
        } catch (IOException exception) {
//            exception.printStackTrace();
            System.out.println("服务器未开启或服务器地址错误或服务器异常关闭！");
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    // 选择范围限制
    private static int choose(String tip, int min, int max) {
        while (true) {
            System.out.print(tip);
            String string = input.next();
            try {
                int answer = Integer.parseInt(string);
                if (answer >= min & answer <= max) {
                    return answer;
                }
                System.out.println("请输入\033[1;31m正确的范围\033[0m，请重新输入！");
            } catch (NumberFormatException exception) {
                // exception.printStackTrace();
                System.out.println("请输入\033[1;31m合法的数字\033[0m，请重新输入！");
            }
        }
    }

    // 最少输入字符串长度限制
    private static String leastString (String tip, int length) {
        while (true) {
            System.out.print(tip);
            String string = input.next();
            if (string.length() >= length) {
                return string;
            }
            System.out.println("请至少输入\033[1;31m" + length + "\033[0m个字符！");
        }
    }

    // 读取txt文件
    private static String txtRead(String filePath) {
        try {
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
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
    private static void txtWrite(Book book) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(book.getBookName().toLowerCase() + ".txt"));
            bufferedWriter.write(book.getContent());
            bufferedWriter.flush();
            bufferedWriter.close();
            System.out.println("下载成功！");
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // 登录菜单
    private static boolean login() throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(new Message(Message.Status.LOGIN, new User(leastString("请输入用户名：", 3), leastString("请输入密码：", 6))));
        objectOutputStream.flush();
        Message message = (Message) objectInputStream.readObject();
        System.out.println((String) message.getObject());
        return message.getStatus().equals(Message.Status.SUCCESS);
    }

    // 注册菜单
    private static boolean register() throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(new Message(Message.Status.REGISTER, new User(leastString("请输入用户名：", 3), leastString("请输入密码：", 6))));
        objectOutputStream.flush();
        Message message = (Message) objectInputStream.readObject();
        System.out.println((String) message.getObject());
        return message.getStatus().equals(Message.Status.SUCCESS);
    }

    // 注销菜单
    private static void logout() throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(new Message(Message.Status.LOGOUT, new User(leastString("请输入用户名：", 3), leastString("请输入密码：", 6))));
        objectOutputStream.flush();
        Message message = (Message) objectInputStream.readObject();
        System.out.println((String) message.getObject());
    }

    // 同类型书列表菜单
    // @SuppressWarnings("unchecked")
    private static void info(String bookType) throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(new Message(Message.Status.READ, bookType));
        objectOutputStream.flush();
        Message message = (Message) objectInputStream.readObject();
        if (message.getStatus().equals(Message.Status.SUCCESS)) {
            List<?> books = (List<?>) message.getObject();
            // List<Book> books = (List<Book>) message.getObject();
            System.out.println("\n===" + bookType + "小说列表===");
            System.out.println("序号\t名称\t作者\t简介");
            for (int index = 0; index < books.size(); index++) {
                Book book = (Book) books.get(index);
                System.out.println((index + 1) + "\t" + book.getBookName() + "\t" + book.getAuthor() + "\t" + book.getInfo());
            }
            int num = choose("\n输入文件序号，删除书本类型输入\033[1;31m -2 \033[0m，上传文件输入\033[1;31m -1 \033[0m，返回请输入\033[1;31m 0 \033[0m：", -2, books.size());
            switch (num) {
                case -2:
                    if (choose("删除书本类型将同步删除书本信息和书本文件，继续删除输入\033[1;31m 1 \033[0m，取消删除输入\033[1;31m 0 \033[0m：", 0, 1) == 1) {
                        objectOutputStream.writeObject(new Message(Message.Status.DROP, bookType));
                        objectOutputStream.flush();
                        message = (Message) objectInputStream.readObject();
                        System.out.println((String) message.getObject());
                        if (message.getStatus().equals(Message.Status.SUCCESS)) {
                            menu2();
                        } else {
                            info(bookType);
                        }
                    } else {
                        info(bookType);
                    }
                    break;
                case -1:
                    upload(bookType);
                    info(bookType);
                    break;
                case 0:
                    menu2();
                    break;
                default:
                    processor((Book) books.get(num - 1));
                    break;
            }
        }
    }

    // 添加书籍类型菜单
    private static boolean addBookType(String bookType) throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(new Message(Message.Status.CREATE, bookType));
        objectOutputStream.flush();
        Message message = (Message) objectInputStream.readObject();
        System.out.println((String) message.getObject());
        return message.getStatus().equals(Message.Status.SUCCESS);
    }

    // 上传书籍菜单
    private static void upload(String bookType) throws IOException, ClassNotFoundException {
        Book book = new Book(
                bookType,
                leastString("请输入书名：", 1),
                leastString("请输入作者：", 1),
                leastString("请输入简介：", 1),
                txtRead(leastString("请输入书籍路径(含文件名)：", 1))
        );
        objectOutputStream.writeObject(new Message(Message.Status.UPLOAD, book));
        objectOutputStream.flush();
        Message message = (Message) objectInputStream.readObject();
        System.out.println((String) message.getObject());
    }

    // 处理书籍菜单
    private static void processor(Book book) throws IOException, ClassNotFoundException {
        System.out.println("\n1.在线阅读\n2.下载\n3.删除\n4.返回上一级");
        switch (choose("请选择功能：", 1, 4)) {
            case 1:
                System.out.println("===开始阅读 \033[1;31m" + book.getBookName() + "\033[0m (\033[1;31m" + book.getAuthor() + "\033[0m) ===\n" + book.getContent() + "\n===阅读结束===");
                processor(book);
                break;
            case 2:
                txtWrite(book);
                processor(book);
                break;
            case 3:
                switch (choose("\n删除本地文件请输入\033[1;31m 1 \033[0m，删除远程文件请输入\033[1;31m 2 \033[0m，取消删除文件输入\033[1;31m 3 \033[0m", 1, 3)) {
                    case 1:
                        File file = new File(book.getBookName().toLowerCase() + ".txt");
                        if (file.exists()) {
                            if (!file.delete()) {
                                System.out.println("文件 " + file.getName() + " 删除失败。");
                            }
                        }
                        processor(book);
                        break;
                    case 2:
                        objectOutputStream.writeObject(new Message(Message.Status.DELETE, book));
                        objectOutputStream.flush();
                        info(book.getBookType());
                        break;
                    case 3:
                        processor(book);
                        break;
                }
                break;
            case 4:
                info(book.getBookType());
                break;
            default:
                break;
        }
    }

    // 一级菜单
    private static void menu1() throws IOException, ClassNotFoundException {
        System.out.println("\n===一级菜单===\n1.登录\n2.注册\n3.注销\n4.退出");
        switch (choose("请选择功能：", 1, 4)) {
            case 1:
                if (login()) {
                    menu2();
                } else {
                    menu1();
                }
                break;
            case 2:
                if (register()) {
                    menu2();
                } else {
                    menu1();
                }
                break;
            case 3:
                logout();
                menu1();
                break;
            case 4:
                objectOutputStream.writeObject(new Message(Message.Status.EXIT, null));
                objectOutputStream.flush();
                break;
            default:
                break;
        }
    }

    // 二级菜单
    // @SuppressWarnings("unchecked")
    private static void menu2() throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(new Message(Message.Status.TYPES, null));
        objectOutputStream.flush();
        Message message = (Message) objectInputStream.readObject();
        List<?> types = (List<?>) message.getObject();
        // List<String> types = (List<String>) message.getObject();
        System.out.println("\n===二级菜单===\n0.退出登录");
        for (int index = 0; index < types.size(); index++) {
            System.out.println((index + 1) + "." + types.get(index));
        }
        int num = choose("输入书本类型序号，新建书本类型输入\033[1;31m -1 \033[0m，退出登录请输入\033[1;31m 0 \033[0m：", -1, types.size());
        switch (num) {
            case -1:
                String bookType = leastString("请输入书本类型：", 2);
                if (addBookType(bookType)) {
                    info(bookType);
                } else {
                    menu2();
                }
                break;
            case 0:
                menu1();
                break;
            default:
                info((String) types.get(num - 1));
                break;
        }
    }
}
