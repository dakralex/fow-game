package client.map;

public record Step(MapDirection direction, int leaveCost, int enterCost) {

    public Step(MapDirection direction, TerrainType leaveTerrain, TerrainType enterTerrain) {
        this(direction, leaveTerrain.getLeaveCost(), enterTerrain.getEnterCost());
    }

    public int getTravelCost() {
        return leaveCost + enterCost;
    }
}
