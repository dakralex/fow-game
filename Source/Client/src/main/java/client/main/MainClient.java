package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.network.GameServerClient;
import client.network.GameStateUpdater;
import client.search.AStarPathFinder;
import client.search.PathFinder;
import client.util.ANSIColor;

public class MainClient {

    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    private static void runStage(String reason, Predicate<GameClientState> condition,
                                 GameStateUpdater stateUpdater, GameClientState clientState,
                                 Function<GameClientState, Collection<MapDirection>> nextDirectionsSupplier) {
        List<MapDirection> currentDirections = new ArrayList<>();

        while (!condition.test(clientState)) {
            if (clientState.shouldClientAct()) {
                if (currentDirections.isEmpty()) {
                    currentDirections.addAll(nextDirectionsSupplier.apply(clientState));
                }

                stateUpdater.sendMapMove(currentDirections.removeFirst());
            }

            clientState.update(stateUpdater.pollGameState());

            if (clientState.hasClientLost()) {
                System.exit(1);
            }

            GameServerClient.suspendForServer(reason.toLowerCase());
        }
    }

    private static List<GameMapNode> getUnvisitedMapNodes(Collection<GameMapNode> mapNodes) {
        return new ArrayList<>(
                mapNodes.stream()
                        .filter(GameMapNode::isUnvisited)
                        .filter(GameMapNode::isAccessible)
                        .sorted((a, b) -> {
                            if (a.isLootable() && !b.isLootable()) {
                                return 1;
                            } else if (!a.isLootable() && b.isLootable()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        })
                        .toList());
    }

    private static Position getRandomUnvisitedMapNode(Collection<GameMapNode> mapNodes) {
        List<GameMapNode> unvisitedMapNodes = getUnvisitedMapNodes(mapNodes);
        Collections.shuffle(unvisitedMapNodes);

        return unvisitedMapNodes.stream()
                .findFirst()
                .orElseThrow()
                .getPosition();
    }

    private static Comparator<GameMapNode> getNeighborCountComparator(GameMap map) {
        return (a, b) -> {
            int aNeighborCount = map.getReachableNeighbors(a.getPosition()).size();
            int bNeighborCount = map.getReachableNeighbors(b.getPosition()).size();

            return bNeighborCount - aNeighborCount;
        };
    }

    private static Position getDeadEndUnvisitedMapNode(GameMap map,
                                                       Collection<GameMapNode> mapNodes) {
        List<GameMapNode> unvisitedDeadEndMapNodes = getUnvisitedMapNodes(mapNodes).stream()
                .sorted(getNeighborCountComparator(map))
                .toList();

        return unvisitedDeadEndMapNodes.stream()
                .findFirst()
                .map(GameMapNode::getPosition)
                .orElseGet(() -> getRandomUnvisitedMapNode(mapNodes));
    }

    private static Comparator<Position> getFarthestAwayComparator(Position source) {
        return (a, b) -> {
            int aDistance = source.taxicabDistanceTo(a);
            int bDistance = source.taxicabDistanceTo(b);

            return aDistance - bDistance;
        };
    }

    private static Optional<Position> getRandomNearbyLootableFields(Position source, GameMap map) {
        Comparator<Position> farthestAwayComparator = getFarthestAwayComparator(source);
        List<Position> lootableNodes = new ArrayList<>();
        Collection<Position> nearbyLootableNodes = new ArrayList<>();

        do {
            nearbyLootableNodes.clear();
            nearbyLootableNodes.addAll(
                    map.getReachableNeighbors(source)
                            .stream()
                            .filter(GameMapNode::isUnvisited)
                            .filter(GameMapNode::isLootable)
                            .map(GameMapNode::getPosition)
                            .filter(position -> !lootableNodes.contains(position))
                            .sorted(farthestAwayComparator)
                            .toList());
            lootableNodes.addAll(nearbyLootableNodes);
        } while (!nearbyLootableNodes.isEmpty());

        Collections.shuffle(lootableNodes);

        return lootableNodes.stream().findFirst();
    }

    private static List<MapDirection> getNextWalkToUnvisitedNode(Position source, GameMap map,
                                                                 Collection<GameMapNode> nodeHaystack) {
        Position unvisitedPosition = getRandomNearbyLootableFields(source, map)
                .orElseGet(() -> getDeadEndUnvisitedMapNode(map, nodeHaystack));
        PathFinder pathFinder = new AStarPathFinder(map.getMapNodes());

        return pathFinder.findPath(source, unvisitedPosition).intoMapDirections(map);
    }

    private static List<MapDirection> getNextTreasureFindingWalk(GameClientState clientState) {
        GameMap currentMap = clientState.getMap();
        Position currentPosition = clientState.getPlayer().getPosition();
        Collection<GameMapNode> playerMapNodes = currentMap.getPlayerMapNodes();

        return getNextWalkToUnvisitedNode(currentPosition, currentMap, playerMapNodes);
    }

    private static List<MapDirection> getDirectWalkTo(GameClientState clientState,
                                                      Position destination) {
        PathFinder pathFinder = new AStarPathFinder(clientState.getMapNodes());

        return pathFinder
                .findPath(clientState.getPlayer().getPosition(), destination)
                .intoMapDirections(clientState.getMap());
    }

    private static Position getTreasurePosition(GameClientState state) {
        // TODO Improve error handling here (by transitioning back to searching the treasure?)
        return state.getMapNodePosition(GameMapNode::hasTreasure).orElseThrow();
    }

    private static Optional<Position> getWaterProtectedFortPosition(GameMap map,
                                                                    Collection<GameMapNode> mapNodes) {
        return mapNodes.stream()
                .filter(GameMapNode::isLootable)
                .filter(GameMapNode::isUnvisited)
                .map(GameMapNode::getPosition)
                .filter(mapNodePosition -> {
                    Set<GameMapNode> neighborNodes = map.getAllNeighbors(mapNodePosition);

                    return neighborNodes.stream()
                            .filter(neighborNode -> !neighborNode.isAccessible())
                            .count() > 2L;
                })
                .findFirst();
    }

    private static List<MapDirection> getNextFortFindingWalk(GameClientState clientState) {
        GameMap currentMap = clientState.getMap();
        Position currentPosition = clientState.getPlayer().getPosition();
        Collection<GameMapNode> enemyMapNodes = currentMap.getEnemyMapNodes();

        return getWaterProtectedFortPosition(currentMap, enemyMapNodes)
                .map(possiblePosition -> getDirectWalkTo(clientState, possiblePosition))
                .orElseGet(() -> getNextWalkToUnvisitedNode(currentPosition,
                                                            currentMap,
                                                            enemyMapNodes));
    }

    private static Position getFortPosition(GameClientState state) {
        // TODO: Improve error handling here (by transitioning back to searching enemy fort?)
        return state.getMapNodePosition(GameMapNode::hasEnemyFort).orElseThrow();
    }

    public static void main(String[] args) {
        // parse these parameters in compliance to the automatic client evaluation
        String serverBaseUrl = args[1];
        String gameId = args[2];

        GameClientBootstrapper bootstrapper = new GameClientBootstrapper(gameId, serverBaseUrl);
        GameClientState clientState = bootstrapper.bootstrap();
        GameStateUpdater stateUpdater = bootstrapper.getStateUpdater();

        runStage("finding the player's treasure",
                 GameClientState::hasFoundTreasure,
                 stateUpdater,
                 clientState,
                 MainClient::getNextTreasureFindingWalk);

        // HAS FOUND TREASURE
        logger.info(ANSIColor.format("THE CLIENT HAS FOUND THE TREASURE!", ANSIColor.GREEN));

        runStage("going to the player's treasure",
                 GameClientState::hasCollectedTreasure,
                 stateUpdater,
                 clientState,
                 currentClientState -> {
                     Position treasurePosition = getTreasurePosition(currentClientState);
                     return getDirectWalkTo(currentClientState, treasurePosition);
                 });

        // HAS COLLECTED TREASURE
        logger.info(ANSIColor.format("THE CLIENT HAS COLLECTED THE TREASURE!", ANSIColor.GREEN));

        runStage("finding enemy's fort",
                 GameClientState::hasFoundEnemyFort,
                 stateUpdater,
                 clientState,
                 MainClient::getNextFortFindingWalk);

        // HAS FOUND ENEMY'S FORT
        logger.info(ANSIColor.format("THE CLIENT HAS FOUND THE ENEMY'S FORT!", ANSIColor.GREEN));

        runStage("going to the enemy's fort",
                 GameClientState::hasClientWon,
                 stateUpdater,
                 clientState,
                 currentClientState -> {
                     Position enemyFortPosition = getFortPosition(currentClientState);
                     return getDirectWalkTo(currentClientState, enemyFortPosition);
                 });

        logger.info(ANSIColor.format("THE CLIENT HAS WON!", ANSIColor.GREEN));
    }
}
