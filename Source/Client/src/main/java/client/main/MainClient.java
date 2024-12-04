package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static GameClientState sendMap(GameServerClient serverClient, GameClientToken token,
                                           GameStateUpdater stateUpdater, GameMap gameMap) {
        GameClientState clientState = stateUpdater.pollGameState();

        while (!clientState.hasBothPlayers() || !clientState.shouldClientAct()) {
            logger.info("Wait for another client to join...");

            clientState.update(stateUpdater.pollGameState());

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                logger.warn("Unexpected interrupt while waiting on other client to join", e);
                Thread.currentThread().interrupt();
            }
        }

        GameMapSender mapSender = new GameMapSender(serverClient, token);
        mapSender.sendMap(gameMap);

        clientState.update(stateUpdater.pollGameState());

        while (!clientState.hasFullMap()) {
            logger.info("Wait for other client to send their half map...");

            clientState.update(stateUpdater.pollGameState());

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                logger.warn("Unexpected interrupt while waiting on the full map", e);
                Thread.currentThread().interrupt();
            }
        }

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
