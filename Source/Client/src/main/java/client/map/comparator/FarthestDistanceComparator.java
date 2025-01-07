package client.map.comparator;

import java.util.Comparator;

import client.map.Position;

public class FarthestDistanceComparator implements Comparator<Position> {

    private final Position source;

    public FarthestDistanceComparator(Position source) {
        this.source = source;
    }

    @Override
    public int compare(Position o1, Position o2) {
        int aDistance = source.taxicabDistanceTo(o1);
        int bDistance = source.taxicabDistanceTo(o2);

        return aDistance - bDistance;
    }
}
