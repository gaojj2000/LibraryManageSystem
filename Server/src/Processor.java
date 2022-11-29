import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

public class Processor extends Thread {
    Books books;
    Users users;
    Socket socket;
    ObjectInputStream objectInputStream = null;
    ObjectOutputStream objectOutputStream = null;

    public Processor(Socket socket, Books books, Users users) {
        super();
        this.socket = socket;
        this.books = books;
        this.users = users;
    }

    @Override
    public void run() {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            exit: while (true) {
                try {
                    Message message = (Message) objectInputStream.readObject();
                    switch (message.getStatus()) {
                        case SUCCESS:
                        case FAIL:
                        default:
                            break;
                        case EXIT:
                            break exit;
                        case REGISTER:
                            if (users.checkUserExit(((User) message.getObject()).getUsername())) {
                                message.setStatus(Message.Status.FAIL);
                                message.setObject("该用户已存在！");
                            } else {
                                this.users.addUser((User) message.getObject());
                                message.setStatus(Message.Status.SUCCESS);
                                message.setObject("注册成功！");
                            }
                            break;
                        case LOGIN:
                            if (users.verifyUserPassword((User) message.getObject())) {
                                message.setStatus(Message.Status.SUCCESS);
                                message.setObject("登录成功！");
                            } else {
                                message.setStatus(Message.Status.FAIL);
                                message.setObject("用户名或密码错误！");
                            }
                            break;
                        case LOGOUT:
                            if (users.verifyUserPassword((User) message.getObject())) {
                                this.users.deleteUser(((User) message.getObject()).getUsername());
                                if (users.checkUserExit(((User) message.getObject()).getUsername())) {
                                    message.setStatus(Message.Status.FAIL);
                                    message.setObject("用户注销失败！");
                                } else {
                                    message.setStatus(Message.Status.SUCCESS);
                                    message.setObject("用户注销成功！");
                                }
                            } else {
                                message.setStatus(Message.Status.FAIL);
                                message.setObject("用户名或密码错误！");
                            }
                            break;
                        case TYPES:
                            message.setStatus(Message.Status.SUCCESS);
                            message.setObject(books.getBookTypesString());
                            break;
                        case READ:
                            message.setStatus(Message.Status.SUCCESS);
                            message.setObject(books.getBooksByType((String) message.getObject()));
                            break;
                        case DELETE:
                            books.deleteBook((Book) message.getObject());
                            if (books.getBookByName(((Book) message.getObject()).getBookName()) == null) {
                                message.setStatus(Message.Status.SUCCESS);
                                message.setObject("删除书籍成功！");
                            } else {
                                message.setStatus(Message.Status.FAIL);
                                message.setObject("删除书籍失败！");
                            }
                            break;
                        case DROP:
                            books.deleteBookType((String) message.getObject());
                            if (books.getBookTypesString().contains((String) message.getObject())) {
                                message.setStatus(Message.Status.FAIL);
                                message.setObject("删除书本类型失败！");
                            } else {
                                message.setStatus(Message.Status.SUCCESS);
                                message.setObject("删除书本类型成功！");
                            }
                            break;
                        case CREATE:
                            books.addBookType((String) message.getObject());
                            if (books.getBookTypesString().contains((String) message.getObject())) {
                                message.setStatus(Message.Status.SUCCESS);
                                message.setObject("书籍类型添加成功！");
                            } else {
                                message.setStatus(Message.Status.FAIL);
                                message.setObject("书籍类型添加失败！");
                            }
                            break;
                        case UPLOAD:
                            books.addBook((Book) message.getObject());
                            if (books.getBookByName(((Book) message.getObject()).getBookName()) != null) {
                                message.setStatus(Message.Status.SUCCESS);
                                message.setObject("书本 " + ((Book) message.getObject()).getBookName() + ".txt 上传成功！");
                            } else {
                                message.setStatus(Message.Status.FAIL);
                                message.setObject("书本 " + ((Book) message.getObject()).getBookName() + ".txt 上传失败！");
                            }
                            break;
                    }
                    objectOutputStream.writeObject(message);
                    objectOutputStream.flush();
                } catch (SocketException | EOFException exception) {  // 连接断开（客户端终止程序时会触发）
                    // exception.printStackTrace();
                    System.out.println("客户端 " + socket.getRemoteSocketAddress() + " 下线。");
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (books != null) {
                    books.xmlSave();
                }
                if (users != null) {
                    users.xmlSave();
                }
                if (socket != null) {
                    socket.close();
                }
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
