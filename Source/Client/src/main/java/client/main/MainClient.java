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
import java.util.function.Predicate;

import client.generation.MapGenerator;
import client.map.GameMap;
import client.map.GameMapNode;
import client.map.MapDirection;
import client.map.Position;
import client.network.GameClientIdentifier;
import client.network.GameClientRegistrar;
import client.network.GameClientToken;
import client.network.GameMapSender;
import client.network.GameServerClient;
import client.network.GameStateUpdater;
import client.player.PlayerDetails;
import client.search.AStarPathFinder;
import client.search.PathFinder;
import client.util.ANSIColor;
import client.validation.HalfMapValidator;

public class MainClient {

    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    private static final String FIRST_NAME = "Daniel";
    private static final String LAST_NAME = "Kral";
    private static final String UACCOUNT = "krald88";

    private static final long SERVER_WAIT_TIME_MS = 400L;

    private static GameMap generateGameMap() {
        MapGenerator mapGenerator = new MapGenerator();
        HalfMapValidator mapValidator = new HalfMapValidator();

        return mapGenerator.generateUntilValid(mapValidator);
    }

    private static GameClientToken registerPlayer(GameServerClient serverClient, String gameId) {
        PlayerDetails playerDetails = new PlayerDetails(FIRST_NAME, LAST_NAME, UACCOUNT);
        GameClientIdentifier identifier = new GameClientIdentifier(gameId, playerDetails);
        GameClientRegistrar registrar = new GameClientRegistrar(serverClient, identifier);

        return registrar.registerPlayer();
    }

    private static void suspendForServer(String reason) {
        try {
            Thread.sleep(SERVER_WAIT_TIME_MS);
        } catch (InterruptedException e) {
            logger.warn("Unexpected interrupt while {}", reason, e);
            Thread.currentThread().interrupt();
        }
    }

    private static void waitOn(String reason, Predicate<GameClientState> condition,
                               GameStateUpdater stateUpdater, GameClientState clientState) {
        while (!condition.test(clientState)) {
            logger.info("{}...", reason);

            clientState.update(stateUpdater.pollGameState());

            suspendForServer(reason.toLowerCase());
        }
    }

    private static GameClientState sendMap(GameServerClient serverClient, GameClientToken token,
                                           GameStateUpdater stateUpdater, GameMap gameMap) {
        GameClientState clientState = stateUpdater.pollGameState();

        waitOn("Wait for another client to join",
               state -> state.hasBothPlayers() && state.shouldClientAct(),
               stateUpdater, clientState);

        GameMapSender mapSender = new GameMapSender(serverClient, token);
        mapSender.sendMap(gameMap);

        clientState.update(stateUpdater.pollGameState());

        waitOn("Wait for the full map", GameClientState::hasFullMap, stateUpdater, clientState);

        return clientState;
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
        return state.getMapNodes().stream()
                .filter(GameMapNode::hasTreasure)
                .findFirst()
                .map(GameMapNode::getPosition)
                .orElseThrow();
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
        return state.getMapNodes().stream()
                .filter(GameMapNode::hasEnemyFort)
                .findFirst()
                .map(GameMapNode::getPosition)
                .orElseThrow();
    }

    public static void main(String[] args) {
        // parse these parameters in compliance to the automatic client evaluation
        String serverBaseUrl = args[1];
        String gameId = args[2];

        GameMap gameMap = generateGameMap();
        logger.info("Client generated the following player's half map\n{}", gameMap);

        GameServerClient serverClient = new GameServerClient(serverBaseUrl);

        GameClientToken token = registerPlayer(serverClient, gameId);
        logger.info("Client acquired Player ID {}", token.playerId());

        GameStateUpdater stateUpdater = new GameStateUpdater(serverClient, token);
        GameClientState clientState = sendMap(serverClient, token, stateUpdater, gameMap);

        List<MapDirection> currentDirections = new ArrayList<>();

        GameMap currentMap = clientState.getMap();
        logger.info("Client received the full map\n{}", currentMap);

        while (!clientState.hasFoundTreasure()) {
            if (clientState.shouldClientAct()) {
                if (currentDirections.isEmpty()) {
                    currentDirections.addAll(getNextTreasureFindingWalk(clientState));
                }

                MapDirection currentMove = currentDirections.removeFirst();
                stateUpdater.sendMapMove(currentMove);
            }

            clientState.update(stateUpdater.pollGameState());

            if (clientState.hasClientLost()) {
                System.exit(1);
            }

            suspendForServer("finding the player's treasure");
        }

        // HAS FOUND TREASURE
        currentDirections.clear();

        System.out.println(ANSIColor.format("THE CLIENT HAS FOUND THE TREASURE!", ANSIColor.GREEN));

        while (!clientState.hasCollectedTreasure()) {
            if (clientState.shouldClientAct()) {
                if (currentDirections.isEmpty()) {
                    Position treasurePosition = getTreasurePosition(clientState);
                    currentDirections.addAll(getDirectWalkTo(clientState, treasurePosition));
                }

                stateUpdater.sendMapMove(currentDirections.removeFirst());
            }

            clientState.update(stateUpdater.pollGameState());

            if (clientState.hasClientLost()) {
                System.exit(1);
            }

            suspendForServer("going to the player's treasure");
        }

        // HAS COLLECTED TREASURE
        currentDirections.clear();

        System.out.println(ANSIColor.format("THE CLIENT HAS COLLECTED THE TREASURE!",
                                            ANSIColor.GREEN));

        while (!clientState.hasFoundEnemyFort()) {
            if (clientState.shouldClientAct()) {
                if (currentDirections.isEmpty()) {
                    currentDirections.addAll(getNextFortFindingWalk(clientState));
                }

                stateUpdater.sendMapMove(currentDirections.removeFirst());
            }

            clientState.update(stateUpdater.pollGameState());

            if (clientState.hasClientLost()) {
                System.exit(1);
            }

            suspendForServer("finding enemy's fort");
        }

        // HAS FOUND ENEMY'S FORT
        currentDirections.clear();

        System.out.println(ANSIColor.format("THE CLIENT HAS FOUND THE ENEMY'S FORT!",
                                            ANSIColor.GREEN));

        while (!clientState.hasClientWon()) {
            if (clientState.shouldClientAct()) {
                if (currentDirections.isEmpty()) {
                    Position enemyFortPosition = getFortPosition(clientState);
                    currentDirections.addAll(getDirectWalkTo(clientState, enemyFortPosition));
                }

                stateUpdater.sendMapMove(currentDirections.removeFirst());
            }

            clientState.update(stateUpdater.pollGameState());

            if (clientState.hasClientLost()) {
                System.exit(1);
            }

            suspendForServer("going to the enemy's fort");
        }

        System.out.println(ANSIColor.format("THE CLIENT HAS WON!", ANSIColor.GREEN));
    }
}
