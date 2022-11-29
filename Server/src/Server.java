import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Server {
    private static final Administrator administrator = Administrator.getInstance();

    public static void main(String[] args) {
        Books books = null;
        Users users = null;
        ServerSocket serverSocket = null;
        try {
            books = Books.getInstance();
            users = Users.getInstance();
            serverSocket = new ServerSocket(12345, 50, InetAddress.getByAddress(new byte[] {(byte) 127, 0, 0, 1}));
            serverSocket.setSoTimeout(1000);
            System.out.println("服务端 " + serverSocket.getLocalSocketAddress() + " 已启动！");
            System.out.println("输入\033[1;31m QUIT \033[0m将退出服务器端！");
            Thread thread = new Thread(Server::exit);
            thread.start();
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("监听到客户端连接：" + socket.getRemoteSocketAddress());
                    Processor processor = new Processor(socket, books, users);
                    processor.start();
                } catch (SocketTimeoutException exception) {
                    // exception.printStackTrace();
                    if (administrator.getQuit()) {
                        System.out.println("服务端已由指令控制执行退出，等待所有客户端主动关闭连接...");  // 退出指令：QUIT
                        break;
                    }
                }
            }
        } catch (ParserConfigurationException | IOException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (books != null) {
                    books.xmlSave();
                }
                if (users != null) {
                    users.xmlSave();
                }
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static void exit() {
        String string = new Scanner(System.in).next();
        if (string.equals("QUIT")) {
            administrator.setQuit();
        }
    }
}
