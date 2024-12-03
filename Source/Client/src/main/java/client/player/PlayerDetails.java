package client.player;

import java.util.Objects;

import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.messagesfromserver.PlayerState;

public record PlayerDetails(String firstName, String lastName, String uaccount) {

    public PlayerDetails {
        Objects.requireNonNull(firstName, "Student's first name must be specified.");
        Objects.requireNonNull(lastName, "Student's last name must be specified.");
        Objects.requireNonNull(uaccount, "Student's uaccount name must be specified.");
    }

    public static PlayerDetails fromPlayerState(PlayerState playerState) {
        return new PlayerDetails(playerState.getFirstName(),
                                 playerState.getLastName(),
                                 playerState.getUAccount());
    }

    public PlayerRegistration intoPlayerRegistration() {
        return new PlayerRegistration(this.firstName, this.lastName, this.uaccount);
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s)", firstName, lastName, uaccount);
    }
}
