public class Administrator {
    private static final Administrator instance = new Administrator();   // 单例
    private boolean quit = false;                                        // 退出标志

    // 单例模式
    private Administrator() {}
    public static Administrator getInstance() {
        return instance;
    }

    public void setQuit() {
        this.quit = !this.quit;
    }

    public boolean getQuit() {
        return this.quit;
    }
}
