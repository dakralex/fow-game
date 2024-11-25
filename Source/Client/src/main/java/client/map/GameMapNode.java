package client.map;

import messagesbase.messagesfromclient.PlayerHalfMapNode;

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

    public PlayerHalfMapNode intoPlayerHalfMapNode() {
        boolean isFortPresent = fortState == FortState.PLAYER_FORT_PRESENT;

        return new PlayerHalfMapNode(position.x(),
                                     position.y(),
                                     isFortPresent,
                                     terrainType.intoETerrain());
    }

    public void placeFort() {
        fortState = FortState.PLAYER_FORT_PRESENT;
    }

    public void placeTreasure() {
        treasureState = TreasureState.PLAYER_TREASURE_PRESENT;
    }

    public Position getPosition() {
        return position;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public boolean hasFort() {
        return fortState == FortState.PLAYER_FORT_PRESENT;
    }

    public boolean hasTreasure() {
        return treasureState == TreasureState.PLAYER_TREASURE_PRESENT;
    }
}
