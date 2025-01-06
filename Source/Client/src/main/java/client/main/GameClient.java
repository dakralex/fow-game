package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
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

    private static final Collection<Stage> sequentialStages = List.of(
            new FindTreasure(),
            new WalkToTreasure(),
            new FindEnemyFort(),
            new WalkToEnemyFort()
    );

    private final GameClientState clientState;
    private final GameStateUpdater stateUpdater;

    public GameClient(GameClientState clientState, GameStateUpdater stateUpdater) {
        this.clientState = clientState;
        this.stateUpdater = stateUpdater;
    }

    private void runStage(Stage stage) {
        List<MapDirection> currentDirections = new ArrayList<>();
        String stageStartMessage = stage.getStageStartMessage();
        String stageCompletionMessage = stage.getStageCompletionMessage();

        logger.info("--> Stage Start: {}",
                    ANSIColor.format(stageStartMessage, ANSIColor.BRIGHT_RED));

        while (!stage.hasReachedObjective(clientState)) {
            if (clientState.shouldClientAct()) {
                if (currentDirections.isEmpty()) {
                    currentDirections.addAll(stage.retrieveNextDirections(clientState));
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
        sequentialStages.forEach(this::runStage);
    }
}
