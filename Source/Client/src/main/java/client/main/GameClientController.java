package client.main;

import client.map.MapDirection;
import client.network.GameServerClient;
import client.network.GameStateUpdater;

public class GameClientController {

    private final GameClient gameClient;
    private final GameStateUpdater stateUpdater;
    private final long waitTimeMs;

    public GameClientController(GameClient gameClient,
                                GameStateUpdater stateUpdater,
                                long waitTimeMs) {
        this.gameClient = gameClient;
        this.stateUpdater = stateUpdater;
        this.waitTimeMs = waitTimeMs;
    }

    public void runStage() {
        gameClient.runStage();
    }

    public void sendMapMove(MapDirection direction) {
        stateUpdater.sendMapMove(direction);
    }

    public void updateState() {
        GameClientState newState = stateUpdater.pollGameState();

        gameClient.updateState(newState);
    }

    public void suspend() {
        String stageTitle = gameClient.getCurrentTitle();

        GameServerClient.suspendForServer(stageTitle, waitTimeMs);
    }

    public void runNextStage() {
        gameClient.runNextStage();
    }
}
