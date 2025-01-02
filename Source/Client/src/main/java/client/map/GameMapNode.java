package client.map;

import java.util.Objects;

import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromserver.FullMapNode;
import client.util.ANSIColor;

public class GameMapNode implements Comparable<GameMapNode> {

    private final Position position;
    private final TerrainType terrainType;
    private FortState fortState = FortState.UNKNOWN;
    private TreasureState treasureState = TreasureState.UNKNOWN;

    public GameMapNode(Position position, TerrainType terrainType) {
        this.position = position;
        this.terrainType = terrainType;
    }

    public GameMapNode(Position position, TerrainType terrainType, FortState fortState) {
        this.position = position;
        this.terrainType = terrainType;
        this.fortState = fortState;
    }

    private GameMapNode(Position position, TerrainType terrainType, FortState fortState,
                        TreasureState treasureState) {
        this.position = position;
        this.terrainType = terrainType;
        this.fortState = fortState;
        this.treasureState = treasureState;
    }

    public static GameMapNode fromFullMapNode(FullMapNode fullMapNode) {
        Position position = new Position(fullMapNode.getX(), fullMapNode.getY());
        TerrainType terrainType = TerrainType.fromETerrain(fullMapNode.getTerrain());
        FortState fortState = FortState.fromEFortState(fullMapNode.getFortState());
        TreasureState treasureState = TreasureState.fromETreasureState(fullMapNode.getTreasureState());

        return new GameMapNode(position, terrainType, fortState, treasureState);
    }

    public PlayerHalfMapNode intoPlayerHalfMapNode() {
        return new PlayerHalfMapNode(position.x(),
                                     position.y(),
                                     hasPlayerFort(),
                                     terrainType.intoETerrain());
    }

    private void throwOnInvalidUpdatePosition(GameMapNode newGameMapNode) {
        String errorMessage = String.format("GameMapNode at %s was updated with new one at %s.",
                                            position,
                                            newGameMapNode.position);

        throw new IllegalArgumentException(errorMessage);
    }

    private void throwOnInvalidUpdateTerrain(GameMapNode newGameMapNode) {
        String errorMessage = String.format("GameMapNode at %s changed terrain from %s to %s",
                                            position,
                                            terrainType,
                                            newGameMapNode.terrainType);

        throw new IllegalArgumentException(errorMessage);
    }

    private void updateFortState(FortState newFortState, boolean isNodeInSight) {
        // Once the fort state is known, it will be the same
        if (fortState != FortState.UNKNOWN) {
            return;
        }

        // We can only trust fort state changes, when the node is in sight
        if (isNodeInSight && fortState != newFortState) {
            fortState = newFortState;
        }
    }

    private void updateTreasureState(TreasureState newTreasureState, boolean isNodeInSight) {
        // Once the treasure state is known, it will be the same
        if (treasureState != TreasureState.UNKNOWN) {
            return;
        }

        // We can only trust treasure state changes, when the node is in sight
        if (isNodeInSight && treasureState != newTreasureState) {
            treasureState = newTreasureState;
        }
    }

    public void update(GameMapNode newGameMapNode, boolean isNodeInSight) {
        if (!position.equals(newGameMapNode.position)) {
            throwOnInvalidUpdatePosition(newGameMapNode);
        }

        if (terrainType != newGameMapNode.terrainType) {
            throwOnInvalidUpdateTerrain(newGameMapNode);
        }

        updateFortState(newGameMapNode.fortState, isNodeInSight);
        updateTreasureState(newGameMapNode.treasureState, isNodeInSight);
    }

    public void resetVisibility() {
        // TODO: Find a better way to initially set all map nodes to unknown
        if (fortState == FortState.NO_FORT_PRESENT) {
            fortState = FortState.UNKNOWN;
        }

        if (treasureState == TreasureState.NO_TREASURE_PRESENT) {
            treasureState = TreasureState.UNKNOWN;
        }
    }

    public Position getPosition() {
        return position;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public boolean isAccessible() {
        return terrainType == TerrainType.GRASS || terrainType == TerrainType.MOUNTAIN;
    }

    public boolean isLootable() {
        return terrainType == TerrainType.GRASS;
    }

    public boolean isUnvisited() {
        return fortState == FortState.UNKNOWN;
    }

    public boolean hasPlayerFort() {
        return fortState == FortState.PLAYER_FORT_PRESENT;
    }

    public boolean hasOpponentFort() {
        return fortState == FortState.ENEMY_FORT_PRESENT;
    }

    public boolean hasTreasure() {
        return treasureState == TreasureState.PLAYER_TREASURE_PRESENT;
    }

    @Override
    public String toString() {
        if (hasPlayerFort()) {
            return ANSIColor.format("F", ANSIColor.BRIGHT_BLACK, ANSIColor.RED);
        }

        if (hasOpponentFort()) {
            return ANSIColor.format("F", ANSIColor.BRIGHT_BLACK, ANSIColor.BRIGHT_RED);
        }

        if (hasTreasure()) {
            return ANSIColor.format("T", ANSIColor.BRIGHT_BLACK, ANSIColor.BRIGHT_MAGENTA);
        }

        return terrainType.toString();
    }

    @Override
    public int compareTo(GameMapNode other) {
        return this.position.compareTo(other.position);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GameMapNode otherMapNode)) return false;

        return Objects.equals(position, otherMapNode.position);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(position);
    }
}
