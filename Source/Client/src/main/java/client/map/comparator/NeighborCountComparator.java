package client.map.comparator;

import java.util.Comparator;

import client.map.GameMap;
import client.map.GameMapNode;

public class NeighborCountComparator implements Comparator<GameMapNode> {

    private final GameMap map;

    public NeighborCountComparator(GameMap map) {
        this.map = map;
    }

    @Override
    public int compare(GameMapNode o1, GameMapNode o2) {
        int aNeighborCount = map.getReachableNeighbors(o1.getPosition()).size();
        int bNeighborCount = map.getReachableNeighbors(o2.getPosition()).size();

        return bNeighborCount - aNeighborCount;
    }
}
