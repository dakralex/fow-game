package client.map;

import messagesbase.messagesfromclient.PlayerHalfMapNode;
import messagesbase.messagesfromserver.FullMapNode;

public class GameMapNode {

    private final Position position;
    private final TerrainType terrainType;
    private FortState fortState;
    private TreasureState treasureState;

    public GameMapNode(Position position, TerrainType terrainType) {
        this.position = position;
        this.terrainType = terrainType;
        this.fortState = FortState.NO_FORT_PRESENT;
        this.treasureState = TreasureState.UNKNOWN_OR_NONE_PRESENT;
    }

    public GameMapNode(Position position, TerrainType terrainType, FortState fortState,
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

    public void placeFort() {
        fortState = FortState.PLAYER_FORT_PRESENT;
    }

    public Position getPosition() {
        return position;
    }

    public TerrainType getTerrainType() {
        return terrainType;
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
            return "F";
        }

        return terrainType.toString();
    }

    @Override
    public int compareTo(GameMapNode other) {
        return this.position.compareTo(other.position);
    }

    @Override
    public final boolean equals(Object other) {
        if (!(other instanceof GameMapNode otherMapNode)) return false;

        return Objects.equals(position, otherMapNode.position);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(position);
    }
}
