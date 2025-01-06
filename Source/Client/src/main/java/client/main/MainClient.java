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

    private static void runStage(String reason, GameStateUpdater stateUpdater,
                                 GameClientState clientState, Stage stageHandler) {
        List<MapDirection> currentDirections = new ArrayList<>();

        while (!stageHandler.hasReachedObjective(clientState)) {
            if (clientState.shouldClientAct()) {
                if (currentDirections.isEmpty()) {
                    currentDirections.addAll(stageHandler.retrieveNextDirections(clientState));
                }

                stateUpdater.sendMapMove(currentDirections.removeFirst());
            }

            clientState.update(stateUpdater.pollGameState());

            if (clientState.hasClientLost()) {
                System.exit(1);
            }

            GameServerClient.suspendForServer(reason.toLowerCase());
        }
    }

    public static void main(String[] args) {
        // parse these parameters in compliance to the automatic client evaluation
        String serverBaseUrl = args[1];
        String gameId = args[2];

        GameClientBootstrapper bootstrapper = new GameClientBootstrapper(gameId, serverBaseUrl);
        GameClientState clientState = bootstrapper.bootstrap();
        GameStateUpdater stateUpdater = bootstrapper.getStateUpdater();

        runStage("finding the player's treasure", stateUpdater, clientState, new FindTreasure());

        // HAS FOUND TREASURE
        logger.info(ANSIColor.format("THE CLIENT HAS FOUND THE TREASURE!", ANSIColor.GREEN));

        runStage("going to the player's treasure", stateUpdater, clientState, new WalkToTreasure());

        // HAS COLLECTED TREASURE
        logger.info(ANSIColor.format("THE CLIENT HAS COLLECTED THE TREASURE!", ANSIColor.GREEN));

        runStage("finding enemy's fort", stateUpdater, clientState, new FindEnemyFort());

        // HAS FOUND ENEMY'S FORT
        logger.info(ANSIColor.format("THE CLIENT HAS FOUND THE ENEMY'S FORT!", ANSIColor.GREEN));

        runStage("going to the enemy's fort", stateUpdater, clientState, new WalkToEnemyFort());

        logger.info(ANSIColor.format("THE CLIENT HAS WON!", ANSIColor.GREEN));
    }
}
