package client.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import client.map.Position;
import client.util.ANSIColor;
import messagesbase.messagesfromserver.PlayerState;

public class Player {

    private static final Logger logger = LoggerFactory.getLogger(Player.class);

    private static final String TREASURE_STRING =
            ANSIColor.format("treasure", ANSIColor.BLACK, ANSIColor.BRIGHT_YELLOW);

    private final String playerId;
    private final PlayerDetails details;
    private PlayerGameState state;
    private Position position;
    private final List<Position> positionHistory;
    private boolean hasTreasure;

    public Player(String playerId, PlayerDetails details, PlayerGameState state,
                  Position position, boolean hasTreasure) {
        this.playerId = playerId;
        this.details = details;
        this.state = state;
        this.position = position;
        this.hasTreasure = hasTreasure;
        this.positionHistory = new ArrayList<>(List.of(position));
    }

    public static Player fromPlayerState(PlayerState playerState, Position position) {
        String playerId = playerState.getUniquePlayerID();
        PlayerDetails details = PlayerDetails.fromPlayerState(playerState);
        PlayerGameState state = PlayerGameState.fromEPlayerGameState(playerState.getState());
        boolean hasTreasure = playerState.hasCollectedTreasure();

        return new Player(playerId, details, state, position, hasTreasure);
    }

    private void throwOnInvalidUpdateId(Player newPlayer) {
        String errorMessage = String.format("Player with id %s was updated with new one with id %s",
                                            playerId,
                                            newPlayer.playerId);

        throw new IllegalArgumentException(errorMessage);
    }

    private void logUpdateChanges(Player newPlayer) {
        Position newPosition = newPlayer.position;

        if (!position.equals(newPosition)) {
            logger.info("Player {} has moved from {} to {}", getHandle(), position, newPosition);
        }

        if (hasTreasure != newPlayer.hasTreasure) {
            logger.info("Player {} has collected their {}", getHandle(), TREASURE_STRING);
        }
    }

    public void appendPosition(Position newPosition) {
        positionHistory.add(newPosition);
    }

    private void updatePosition(Position newPosition) {
        if (position.equals(newPosition)) {
            return;
        }

        if (position.taxicabDistanceTo(newPosition) > 1) {
            positionHistory.clear();
        }

        appendPosition(newPosition);

        position = newPosition;
    }

    public void update(Player newPlayer) {
        if (!playerId.equals(newPlayer.playerId)) {
            throwOnInvalidUpdateId(newPlayer);
        }

        logUpdateChanges(newPlayer);
        updatePosition(newPlayer.position);

        state = newPlayer.state;
        hasTreasure = newPlayer.hasTreasure;
    }

    private String getHandle() {
        int idHandleLength = Math.min(5, playerId.length());

        return String.format("%s~%s", details.uaccount(), playerId.subSequence(0, idHandleLength));
    }

    public Position getPosition() {
        return position;
    }

    public List<Position> getPositionHistory() {
        return Collections.unmodifiableList(positionHistory);
    }

    public boolean shouldPlayerAct() {
        return state == PlayerGameState.MUST_ACT;
    }

    public boolean hasWon() {
        return state == PlayerGameState.WON;
    }

    public boolean hasLost() {
        return state == PlayerGameState.LOST;
    }

    public boolean hasTreasure() {
        return hasTreasure;
    }

    public boolean isPlayerAt(Position otherPosition) {
        return position.equals(otherPosition);
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", details, playerId);
    }
}
