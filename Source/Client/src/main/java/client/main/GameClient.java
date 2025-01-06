package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import client.main.stage.FindEnemyFort;
import client.main.stage.FindTreasure;
import client.main.stage.Stage;
import client.main.stage.WalkToEnemyFort;
import client.main.stage.WalkToTreasure;
import client.map.MapDirection;
import client.network.GameServerClient;
import client.network.GameStateUpdater;
import client.util.ANSIColor;

public class GameClient implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    private final GameClientState clientState;
    private final GameStateUpdater stateUpdater;

    public GameClient(GameClientState clientState, GameStateUpdater stateUpdater) {
        this.clientState = clientState;
        this.stateUpdater = stateUpdater;
    }

    private static void runStage(GameStateUpdater stateUpdater, GameClientState clientState,
                                 Stage stageHandler) {
        List<MapDirection> currentDirections = new ArrayList<>();
        String stageStartMessage = stageHandler.getStageStartMessage();
        String stageCompletionMessage = stageHandler.getStageCompletionMessage();

        logger.info("--> Stage Start: {}",
                    ANSIColor.format(stageStartMessage, ANSIColor.BRIGHT_RED));

        while (!stageHandler.hasReachedObjective(clientState)) {
            if (clientState.shouldClientAct()) {
                if (currentDirections.isEmpty()) {
                    currentDirections.addAll(stageHandler.retrieveNextDirections(clientState));
                }

                stateUpdater.sendMapMove(currentDirections.removeFirst());
            }

            clientState.update(stateUpdater.pollGameState());

            if (clientState.hasClientLost()) {
                logger.info("--> {}", ANSIColor.format("CLIENT HAS LOST", ANSIColor.BRIGHT_RED));
                System.exit(0);
            }

            GameServerClient.suspendForServer(stageStartMessage);
        }

        logger.info("--> Stage completed: {}",
                    ANSIColor.format(stageCompletionMessage.toUpperCase(), ANSIColor.GREEN));
    }

    @Override
    public void run() {
        runStage(stateUpdater, clientState, new FindTreasure());
        runStage(stateUpdater, clientState, new WalkToTreasure());
        runStage(stateUpdater, clientState, new FindEnemyFort());
        runStage(stateUpdater, clientState, new WalkToEnemyFort());
    }
}
