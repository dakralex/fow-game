package client.main;

public class MainClient {

    public static void main(String[] args) {
        // parse these parameters in compliance to the automatic client evaluation
        String serverBaseUrl = args[1];
        String gameId = args[2];

        GameClientBootstrapper bootstrapper = new GameClientBootstrapper(gameId, serverBaseUrl);
        GameClientView mainView = bootstrapper.bootstrap();

        mainView.run();
    }
}
