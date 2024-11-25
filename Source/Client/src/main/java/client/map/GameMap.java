package client.map;

import java.util.Collection;
import java.util.stream.Collectors;

import messagesbase.messagesfromclient.PlayerHalfMap;
import messagesbase.messagesfromclient.PlayerHalfMapNode;

public class GameMap {

    private final Collection<GameMapNode> nodes;

    public GameMap(Collection<GameMapNode> nodes) {
        this.nodes = nodes;
    }

    public PlayerHalfMap intoPlayerHalfMap(String playerId) {
        Collection<PlayerHalfMapNode> halfMapNodes = nodes.stream()
                .map(GameMapNode::intoPlayerHalfMapNode)
                .collect(Collectors.toSet());

        return new PlayerHalfMap(playerId, halfMapNodes);
    }
}
