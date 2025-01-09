package client.main;

import java.util.ArrayList;
import java.util.List;

import client.main.stage.Stage;
import client.map.MapDirection;
import client.util.Observable;

public class GameClient extends Observable<GameClientEvent> {

    private Stage currentStage;
    private final List<Stage> stages;
    private final List<MapDirection> currentDirections;
    private final GameClientState currentState;

    public GameClient(GameClientState currentState, List<Stage> stages) {
        this.stages = new ArrayList<>(stages);
        this.currentStage = this.stages.removeFirst();
        this.currentDirections = new ArrayList<>(1);
        this.currentState = currentState;
    }

    public void runStage() {
        notifyObservers(GameClientEvent.STAGE_START);

        while (!currentStage.hasReachedObjective(currentState)) {
            if (currentState.shouldClientAct()) {
                notifyObservers(GameClientEvent.SHOULD_SEND_MOVE);
            }

            notifyObservers(GameClientEvent.SHOULD_UPDATE_STATE);

            if (currentState.hasClientWon()) {
                notifyObservers(GameClientEvent.HAS_WON);
                return;
            }

            if (currentState.hasClientLost()) {
                notifyObservers(GameClientEvent.HAS_LOST);
                return;
            }

            notifyObservers(GameClientEvent.SHOULD_SUSPEND);
        }

        notifyObservers(GameClientEvent.STAGE_COMPLETE);
    }

    public void runNextStage() {
        if (stages.isEmpty()) {
            notifyObservers(GameClientEvent.HAS_LOST);
        }

        currentStage = stages.removeFirst();

        runStage();
    }

    public Stage getCurrentStage() {
        return currentStage;
    }

    public String getCurrentTitle() {
        return currentStage.getStageStartMessage();
    }

    public MapDirection getNextMove() {
        if (currentDirections.isEmpty()) {
            currentDirections.addAll(currentStage.retrieveNextDirections(currentState));
        }

        return currentDirections.removeFirst();
    }

    public void updateState(GameClientState newState) {
        boolean hasUpdated = currentState.update(newState);

        if (hasUpdated) {
            notifyObservers(GameClientEvent.SHOULD_DRAW_MAP);
        }
    }

    public GameClientState getCurrentState() {
        return currentState;
    }
}
