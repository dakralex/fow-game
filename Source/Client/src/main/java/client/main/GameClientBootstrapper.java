package client.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

import client.generation.MapGenerator;
import client.main.stage.FindEnemyFort;
import client.main.stage.FindTreasure;
import client.main.stage.Stage;
import client.main.stage.WalkToEnemyFort;
import client.main.stage.WalkToTreasure;
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

    private static final long DEFAULT_WAIT_TIME_MS = 400L;

    private static final List<Stage> GAME_STAGES = List.of(
            new FindTreasure(),
            new WalkToTreasure(),
            new FindEnemyFort(),
            new WalkToEnemyFort()
    );

    private final String gameId;
    private final GameServerClient serverClient;
    private final long waitTimeMs;

    private GameClientBootstrapper(String gameId, String serverBaseUrl, long waitTimeMs) {
        this.gameId = gameId;
        this.serverClient = new GameServerClient(serverBaseUrl);
        this.waitTimeMs = waitTimeMs;
    }

    public GameClientBootstrapper(String gameId, String serverBaseUrl) {
        this(gameId, serverBaseUrl, DEFAULT_WAIT_TIME_MS);
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

    private void waitOn(String reason, Predicate<GameClientState> condition,
                        GameStateUpdater stateUpdater, GameClientState clientState) {
        while (!condition.test(clientState)) {
            logger.info("{}...", reason);

            clientState.update(stateUpdater.pollGameState());

            GameServerClient.suspendForServer(reason.toLowerCase(), waitTimeMs);
        }
    }

    private GameClientState sendMap(GameServerClient serverClient, GameClientToken token,
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
     * Bootstraps and returns the {@link GameClientView}.
     * <p>
     * This handles any pre-game setup and requests to the server to construct the game-ready
     * {@link GameClient}, which is ready to engage in the actual game, where the full
     * {@link GameMap} and both players are present, as well as sending map movement requests
     * is possible.
     *
     * @return the view to visually follow the game with
     */
    public GameClientView bootstrap() {
        GameMap gameMap = generateGameMap();
        logger.info("Client generated the following player's half map\n{}", gameMap);

        GameClientToken token = registerPlayer(serverClient, gameId);
        logger.info("Client acquired Player ID {}", token.playerId());

        GameStateUpdater stateUpdater = new GameStateUpdater(serverClient, token);
        GameClientState clientState = sendMap(serverClient, token, stateUpdater, gameMap);

        GameMap currentMap = clientState.getMap();
        logger.info("Client received the full map\n{}", currentMap);

        GameClient gameClient = new GameClient(clientState, GAME_STAGES);
        GameClientController controller = new GameClientController(gameClient,
                                                                   stateUpdater,
                                                                   DEFAULT_WAIT_TIME_MS);

        return new GameClientView(gameClient, controller);
    }

}
