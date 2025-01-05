package client.map.comparator;

import java.util.Comparator;

import client.map.GameMapNode;

public class LootabilityComparator implements Comparator<GameMapNode> {

    @Override
    public int compare(GameMapNode o1, GameMapNode o2) {
        boolean o1Lootable = o1.isLootable();
        boolean o2Lootable = o2.isLootable();

        boolean onlyO1Lootable = o1Lootable && !o2Lootable;
        boolean onlyO2Lootable = !o1Lootable && o2Lootable;

        if (onlyO1Lootable) {
            return 1;
        } else if (onlyO2Lootable) {
            return -1;
        } else {
            return 0;
        }
    }
}
