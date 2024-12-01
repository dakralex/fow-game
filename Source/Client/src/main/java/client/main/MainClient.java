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

    public static void main(String[] args) {
        // parse these parameters in compliance to the automatic client evaluation
        String serverBaseUrl = args[1];
        String gameId = args[2];

        GameServerClient serverClient = new GameServerClient(serverBaseUrl);

        PlayerDetails playerDetails = new PlayerDetails(FIRST_NAME, LAST_NAME, UACCOUNT);
        GameClientIdentifier identifier = new GameClientIdentifier(gameId, playerDetails);
        GameClientRegistrar registrar = new GameClientRegistrar(serverClient, identifier);
        GameClientToken token = registrar.registerPlayer();

        logger.info("Client acquired Player ID {}", token.playerId());

        GameStateUpdater stateUpdater = new GameStateUpdater(serverClient, token);
        GameClientState clientState = stateUpdater.pollGameState();

        while (!clientState.hasBothPlayers() || !clientState.shouldClientAct()) {
            logger.info("Wait for another client to join...");

            clientState = stateUpdater.pollGameState();

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // TODO: Implement an actual map generation algorithm, this will fail without that
        GameMapSender mapSender = new GameMapSender(serverClient, token);
        MapGenerator mapGenerator = new MapGenerator();
        HalfMapValidator mapValidator = new HalfMapValidator();
        GameMap gameMap = mapGenerator.generateUntilValid(mapValidator);

        logger.info("Client generated the following player's half map\n{}", gameMap);

        mapSender.sendMap(gameMap);
    }
}
