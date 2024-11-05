package client.network;

import java.util.Objects;

import messagesbase.UniquePlayerIdentifier;

public record GameClientToken(String gameId, String playerId) {

    public GameClientToken {
        Objects.requireNonNull(gameId, "GameID must be specified.");
        Objects.requireNonNull(playerId, "PlayerID must be specified.");
    }

    public static GameClientToken fromUniquePlayerIdentifier(String gameId, UniquePlayerIdentifier playerIdentifier) {
        return new GameClientToken(gameId, playerIdentifier.getUniquePlayerID());
    }
}
