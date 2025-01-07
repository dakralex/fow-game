package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.map.MapDirection;
import client.util.ANSIColor;
import client.util.Observer;

public class GameClientView implements Runnable, Observer<GameClientEvent> {

    private static final Logger logger = LoggerFactory.getLogger(GameClientView.class);

    private final GameClient gameClient;
    private final GameClientController controller;

    public GameClientView(GameClient gameClient, GameClientController controller) {
        this.gameClient = gameClient;
        this.controller = controller;
    }

    private static void printMessage(String message, ANSIColor color) {
        logger.info("--> {}", ANSIColor.format(message, color));
    }

    @Override
    public void run() {
        gameClient.subscribe(this, GameClientEvent.allEvents());
        controller.runStage();
    }

    private void handleLost() {
        printMessage("CLIENT HAS LOST!", ANSIColor.BRIGHT_RED);
        Thread.currentThread().interrupt();
    }

    private void handleWin() {
        printMessage("CLIENT HAS WON!", ANSIColor.BRIGHT_GREEN);
        Thread.currentThread().interrupt();
    }

    private void handleMoveSend(GameClient gameClient) {
        MapDirection direction = gameClient.getNextMove();

        controller.sendMapMove(direction);
    }

    private void handleStateUpdate() {
        controller.updateState();
    }

    private void handleSuspend() {
        controller.suspend();
    }

    private void handleStageComplete(GameClient gameClient) {
        String stageCompletionMessage = gameClient.getCurrentStage().getStageCompletionMessage();
        String message = String.format("Stage completed: %s", stageCompletionMessage);
        printMessage(message, ANSIColor.GREEN);

        controller.runNextStage();
    }

    private void handleStageStart(GameClient gameClient) {
        String stageStartMessage = gameClient.getCurrentStage().getStageStartMessage();
        String message = String.format("Stage started: %s", stageStartMessage);
        printMessage(message, ANSIColor.BRIGHT_RED);
    }

    @Override
    public void update(GameClientEvent event) {
        switch (event) {
            case HAS_LOST -> handleLost();
            case HAS_WON -> handleWin();
            case SHOULD_SEND_MOVE -> handleMoveSend(gameClient);
            case SHOULD_SUSPEND -> handleSuspend();
            case SHOULD_UPDATE_STATE -> handleStateUpdate();
            case STAGE_COMPLETE -> handleStageComplete(gameClient);
            case STAGE_START -> handleStageStart(gameClient);
        }
    }
}
