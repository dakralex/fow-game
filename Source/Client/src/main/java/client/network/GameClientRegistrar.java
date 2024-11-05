package client.network;

import java.util.Optional;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerRegistration;

public class GameClientRegistrar {

    private final GameServerClient serverClient;
    private final GameClientIdentifier identifier;

    public GameClientRegistrar(GameServerClient serverClient, GameClientIdentifier identifier) {
        this.serverClient = serverClient;
        this.identifier = identifier;
    }

    private static PlayerRegistrationException throwOnEmptyResponse() {
        return new PlayerRegistrationException(
                "Could not register player: Server response was empty.");
    }

    public GameClientToken registerPlayer() {
        String gameId = identifier.gameId();
        String apiUri = String.format("/%s/players", gameId);

        PlayerRegistration playerRegistration = identifier.details().intoPlayerRegistration();
        Optional<UniquePlayerIdentifier> response = serverClient.postAndGetData(apiUri, playerRegistration);
        UniquePlayerIdentifier playerIdentifier = response.orElseThrow(GameClientRegistrar::throwOnEmptyResponse);

        return GameClientToken.fromUniquePlayerIdentifier(gameId, playerIdentifier);
    }
}
