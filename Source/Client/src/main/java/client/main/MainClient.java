package client.main;

import client.generation.MapGenerator;
import client.map.GameMap;
import client.network.GameClientIdentifier;
import client.network.GameClientRegistrar;
import client.network.GameClientToken;
import client.network.GameMapSender;
import client.network.GameServerClient;
import client.network.GameStateUpdater;
import client.player.PlayerDetails;

public class MainClient {

	private static final String FIRST_NAME = "Daniel";
	private static final String LAST_NAME = "Kral";
	private static final String UACCOUNT = "krald88";

	public static void main(String[] args) {
		// parse these parameters in compliance to the automatic client evaluation
		String serverBaseUrl = args[1];
		String gameId = args[2];

		GameServerClient serverClient = new GameServerClient(serverBaseUrl);

		PlayerDetails playerDetails = new PlayerDetails(FIRST_NAME, LAST_NAME, UACCOUNT);
		GameClientIdentifier identifier = new GameClientIdentifier(gameId, playerDetails);
		GameClientRegistrar registrar = new GameClientRegistrar(serverClient, identifier);
		GameClientToken token = registrar.registerPlayer();

        GameStateUpdater stateUpdater = new GameStateUpdater(serverClient, token);
        GameClientState clientState = stateUpdater.pollGameState();

        while (!clientState.hasBothPlayers() || !clientState.shouldClientAct()) {
            System.out.println("Waiting...");

            clientState = stateUpdater.pollGameState();

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // TODO: Implement an actual map generation algorithm, this will fail without that
        GameMapSender mapSender = new GameMapSender(serverClient, token);
        MapGenerator mapGenerator = new MapGenerator();
        GameMap gameMap = mapGenerator.generateMap();

        System.out.println(gameMap);

        mapSender.sendMap(gameMap);

		System.out.println("My Player ID: " + token.playerId());
	}
}
