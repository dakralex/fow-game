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

public class GameClientBootstrapper {

    private static final Logger logger = LoggerFactory.getLogger(GameClientBootstrapper.class);

    private static final String FIRST_NAME = "Daniel";
    private static final String LAST_NAME = "Kral";
    private static final String U_ACCOUNT = "krald88";

    private final String gameId;
    private final GameServerClient serverClient;

    public GameClientBootstrapper(String gameId, String serverBaseUrl) {
        this.gameId = gameId;
        this.serverClient = new GameServerClient(serverBaseUrl);
    }

    private static void waitOn(String reason, Predicate<GameClientState> condition,
                               GameStateUpdater stateUpdater, GameClientState clientState) {
        while (!condition.test(clientState)) {
            logger.info("{}...", reason);

            clientState.update(stateUpdater.pollGameState());

            GameServerClient.suspendForServer(reason.toLowerCase());
        }
    }

    private static GameMap generateGameMap() {
        MapGenerator mapGenerator = new MapGenerator();
        HalfMapValidator mapValidator = new HalfMapValidator();

        return mapGenerator.generateUntilValid(mapValidator);
    }

    private static GameClientToken registerPlayer(GameServerClient serverClient, String gameId) {
        PlayerDetails playerDetails = new PlayerDetails(FIRST_NAME, LAST_NAME, U_ACCOUNT);
        GameClientIdentifier identifier = new GameClientIdentifier(gameId, playerDetails);
        GameClientRegistrar registrar = new GameClientRegistrar(serverClient, identifier);

        return registrar.registerPlayer();
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

    /**
     * Bootstraps and returns the {@link GameClientState}.
     * <p>
     * This handles the pre-game setup and requests to the server to provide the
     * {@link GameClientState}, which is ready to engage in the actual game, where the full
     * {@link GameMap} and both players are present, as well as sending map movement requests
     * is possible.
     *
     * @return the server-provided game-ready game state
     */
    public GameClient bootstrap() {
        GameMap gameMap = generateGameMap();
        logger.info("Client generated the following player's half map\n{}", gameMap);

        GameClientToken token = registerPlayer(serverClient, gameId);
        logger.info("Client acquired Player ID {}", token.playerId());

        GameStateUpdater stateUpdater = new GameStateUpdater(serverClient, token);
        GameClientState clientState = sendMap(serverClient, token, stateUpdater, gameMap);

        GameMap currentMap = clientState.getMap();
        logger.info("Client received the full map\n{}", currentMap);

        return new GameClient(clientState, stateUpdater);
    }

}
