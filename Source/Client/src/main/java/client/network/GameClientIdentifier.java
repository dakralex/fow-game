package client.network;

import java.util.Objects;

import client.player.PlayerDetails;

public record GameClientIdentifier(String gameId, PlayerDetails details) {

    public GameClientIdentifier {
        Objects.requireNonNull(gameId, "GameID must be specified.");
        Objects.requireNonNull(details, "Player details must be specified.");
    }
}
