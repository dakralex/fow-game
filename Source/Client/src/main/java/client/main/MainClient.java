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

public class MainClient {

    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

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

    public static void main(String[] args) {
        // parse these parameters in compliance to the automatic client evaluation
        String serverBaseUrl = args[1];
        String gameId = args[2];

        GameClientBootstrapper bootstrapper = new GameClientBootstrapper(gameId, serverBaseUrl);
        GameClientState clientState = bootstrapper.bootstrap();
        GameStateUpdater stateUpdater = bootstrapper.getStateUpdater();

        runStage(stateUpdater, clientState, new FindTreasure());
        runStage(stateUpdater, clientState, new WalkToTreasure());
        runStage(stateUpdater, clientState, new FindEnemyFort());
        runStage(stateUpdater, clientState, new WalkToEnemyFort());
    }
}
