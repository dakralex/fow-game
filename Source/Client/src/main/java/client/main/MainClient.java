package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import client.generation.MapGenerator;
import client.map.GameMap;
import client.network.GameClientIdentifier;
import client.network.GameClientRegistrar;
import client.network.GameClientToken;
import client.network.GameMapSender;
import client.network.GameServerClient;
import client.network.GameStateUpdater;
import client.player.PlayerDetails;
import client.validation.HalfMapValidator;

public class MainClient {

    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    private static final String FIRST_NAME = "Daniel";
    private static final String LAST_NAME = "Kral";
    private static final String UACCOUNT = "krald88";

    private static final int SERVER_WAIT_TIME_MS = 400;

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
        sendMap(serverClient, token, stateUpdater, gameMap);
    }
}
