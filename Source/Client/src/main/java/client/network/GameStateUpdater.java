package client.network;

import java.util.Optional;

import client.main.GameClientState;
import client.map.MapDirection;
import messagesbase.messagesfromclient.PlayerMove;
import messagesbase.messagesfromserver.GameState;

public class GameStateUpdater {

    private final GameServerClient serverClient;
    private final GameClientToken token;

    public GameStateUpdater(GameServerClient serverClient, GameClientToken token) {
        this.serverClient = serverClient;
        this.token = token;
    }

    private static GameStateUpdaterException throwOnEmptyStateResponse() {
        return new GameStateUpdaterException(
                "Could not update game state: Server response was empty.");
    }

    public GameClientState pollGameState() {
        String gameId = token.gameId();
        String playerId = token.playerId();
        String apiUrl = String.format("/%s/states/%s", gameId, playerId);

        Optional<GameState> response = serverClient.getAndGetData(apiUrl);
        GameState gameState = response.orElseThrow(GameStateUpdater::throwOnEmptyStateResponse);

        return GameClientState.fromGameState(gameState, gameId, playerId);
    }

    public void sendMapMove(MapDirection direction) {
        String gameId = token.gameId();
        String playerId = token.playerId();
        String apiUrl = String.format("/%s/moves", gameId);

        PlayerMove playerMove = PlayerMove.of(playerId, direction.intoEMove());
        serverClient.post(apiUrl, playerMove);
    }
}
