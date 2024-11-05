package client.network;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerRegistration;

public class GameClientRegistrar {

    private final GameServerClient serverClient;
    private final GameClientIdentifier identifier;

    public GameClientRegistrar(GameServerClient serverClient, GameClientIdentifier identifier) {
        this.serverClient = serverClient;
        this.identifier = identifier;
    }

    public GameClientToken registerPlayer() {
        String gameId = identifier.gameId();
        String apiUri = String.format("/%s/players", gameId);

        PlayerRegistration playerRegistration = identifier.details().intoPlayerRegistration();
        UniquePlayerIdentifier playerIdentifier = serverClient.post(apiUri, playerRegistration);

        return GameClientToken.fromUniquePlayerIdentifier(gameId, playerIdentifier);
    }
}
