package client.player;

import java.util.Objects;

import messagesbase.messagesfromclient.PlayerRegistration;

public record PlayerDetails(String firstName, String lastName, String uaccount) {

    public PlayerDetails {
        Objects.requireNonNull(firstName, "Student's first name must be specified.");
        Objects.requireNonNull(lastName, "Student's last name must be specified.");
        Objects.requireNonNull(uaccount, "Student's uaccount name must be specified.");
    }

    public PlayerRegistration intoPlayerRegistration() {
        return new PlayerRegistration(this.firstName, this.lastName, this.uaccount);
    }
}
