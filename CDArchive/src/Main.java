public class Main implements IApp{
    public static void main(String[] args) {

        IApp app = new Main();
        ShutdownInterceptor shutdownInterceptor = new ShutdownInterceptor(app);
        Runtime.getRuntime().addShutdownHook(shutdownInterceptor);
        app.start();

        new MainWindow();
        new SecondWindow();
    }
    public void start() {

        try {

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void shutDown() {
        // Do a graceful shutdown here
        System.out.println("Shutdown is called");

        try {
            System.out.println("Sleeping for 5 seconds before shutting down");
            Thread.sleep(5000);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}