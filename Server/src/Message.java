import java.io.Serializable;

public class Message implements Serializable {
    public enum Status {
        SUCCESS,   // 成功
        FAIL,      // 失败
        EXIT,      // 退出连接
        REGISTER,  // 注册用户
        LOGIN,     // 登录用户
        LOGOUT,    // 注销用户
        TYPES,     // 书本类型
        READ,      // 阅读书本
        DELETE,    // 删除书本
        DROP,      // 删除类型
        CREATE,    // 创建类型
        UPLOAD,    // 上传书本
    }

    private Status status;  // 消息状态
    private Object object;  // 消息内容

    public Message(Status status, Object object) {
        this.status = status;
        this.object = object;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Status getStatus() {
        return this.status;
    }

    public Object getObject() {
        return this.object;
    }
}
