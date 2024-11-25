package client.network;

import client.map.GameMap;
import messagesbase.messagesfromclient.PlayerHalfMap;

public class GameMapSender {

    private final GameServerClient serverClient;
    private final GameClientToken token;

    public GameMapSender(GameServerClient serverClient, GameClientToken token) {
        this.serverClient = serverClient;
        this.token = token;
    }

    public void sendMap(GameMap map) {
        String gameId = token.gameId();
        String apiUri = String.format("/%s/halfmaps", gameId);

        // TODO: Implement polling the game state and check if player can send their map

        PlayerHalfMap playerHalfMap = map.intoPlayerHalfMap(token.playerId());
        serverClient.post(apiUri, playerHalfMap);
    }
}
