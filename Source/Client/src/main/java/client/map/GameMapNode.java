package client.map;

import java.util.Objects;

import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromserver.FullMapNode;
import client.util.ANSIColor;

public class GameMapNode implements Comparable<GameMapNode> {

    private final Position position;
    private TerrainType terrainType;
    private FortState fortState;
    private TreasureState treasureState;

    public GameMapNode(Position position, TerrainType terrainType, FortState fortState,
                       TreasureState treasureState) {
        this.position = position;
        this.terrainType = terrainType;
        this.fortState = fortState;
        this.treasureState = treasureState;
    }

    public GameMapNode(Position position, TerrainType terrainType, FortState fortState) {
        this(position, terrainType, fortState, TreasureState.UNKNOWN);
    }

    public GameMapNode(Position position, TerrainType terrainType) {
        this(position, terrainType, FortState.UNKNOWN);
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

    /**
     * Resets the current intelligence on the GameMapNode, i.e. whether the presence of the
     * player's or enemy's fort and the presence of the player's treasure is known, if that
     * information is known to be unreliable.
     * <p>
     * This method should be called if a new server-sourced GameMapNode is added to the
     * {@link GameMap}, as the server doesn't provide the same values for {@link FortState} and
     * {@link TreasureState} as we do, and so we cannot rely on them initially.
     *
     * @see GameMapNode#updateFortState(FortState, boolean)
     * @see GameMapNode#updateTreasureState(TreasureState, boolean)
     */
    public void resetIntelligence() {
        // Do not reset if the fort state is already properly known
        if (fortState == FortState.NO_FORT_PRESENT) {
            fortState = FortState.UNKNOWN;
        }

        // Do not reset if the treasure state is already properly known
        if (treasureState == TreasureState.NO_TREASURE_PRESENT) {
            treasureState = TreasureState.UNKNOWN;
        }
    }

    /**
     * Marks the GameMapNode as having the player fort placed on it.
     * <p>
     * This method should only be used for testing purposes, as the actual player fort should only
     * be placed during half map generation through the
     * {@link GameMapNode#GameMapNode(Position, TerrainType, FortState, TreasureState)} constructor.
     */
    public void placePlayerFort() {
        this.fortState = FortState.PLAYER_FORT_PRESENT;
    }

    /**
     * Marks the GameMapNode as having the enemy fort placed on it.
     * <p>
     * This method should only be used for testing purposes, as the actual enemy fort should only
     * be placed by adding a server-sourced GameMapNode when receiving the full {@link GameMap}
     * at game start.
     */
    public void placeEnemyFort() {
        this.fortState = FortState.ENEMY_FORT_PRESENT;
    }

    /**
     * Marks the GameMapNode as having the player treasure placed on it.
     * <p>
     * This method should only be used for testing purposes, as the actual player treasure should
     * only be placed by the server and revealed if the player is near enough to see it.
     */
    public void placePlayerTreasure() {
        this.treasureState = TreasureState.PLAYER_TREASURE_PRESENT;
    }

    /**
     * Change the GameMapNode's terrain type retrospectively.
     * <p>
     * This method should only be used for testing purposes, as the terrain type should only be set
     * during half map generation through the constructor.
     *
     * @param terrainType the new terrain type
     */
    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
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

    public boolean hasEnemyFort() {
        return fortState == FortState.ENEMY_FORT_PRESENT;
    }

    public boolean hasAnyFort() {
        return hasPlayerFort() || hasEnemyFort();
    }

    public boolean hasTreasure() {
        return treasureState == TreasureState.PLAYER_TREASURE_PRESENT;
    }

    @Override
    public String toString() {
        if (hasPlayerFort()) {
            return ANSIColor.format("F", ANSIColor.BRIGHT_BLACK, ANSIColor.RED);
        }

        if (hasEnemyFort()) {
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
