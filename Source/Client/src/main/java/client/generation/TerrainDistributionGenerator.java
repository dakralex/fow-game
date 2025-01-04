package client.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import client.map.TerrainType;

public class TerrainDistributionGenerator {

    private static final int GRASS_MIN_AMOUNT = 24;
    private static final int MOUNTAIN_MIN_AMOUNT = 5;
    private static final int WATER_MIN_AMOUNT = 7;

    public List<TerrainType> generateTerrainQueue(int amount) {
        List<TerrainType> terrainTypeQueue = new ArrayList<>(amount);

        terrainTypeQueue.addAll(Collections.nCopies(GRASS_MIN_AMOUNT, TerrainType.GRASS));
        terrainTypeQueue.addAll(Collections.nCopies(MOUNTAIN_MIN_AMOUNT, TerrainType.MOUNTAIN));
        terrainTypeQueue.addAll(Collections.nCopies(WATER_MIN_AMOUNT, TerrainType.WATER));

        int remainingAmount = amount - terrainTypeQueue.size();
        terrainTypeQueue.addAll(Collections.nCopies(remainingAmount, TerrainType.MOUNTAIN));

        return terrainTypeQueue;
    }
}
