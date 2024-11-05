package client.main;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import messagesbase.UniquePlayerIdentifier;
import messagesbase.messagesfromclient.PlayerRegistration;
import messagesbase.ResponseEnvelope;
import messagesbase.messagesfromclient.ERequestState;
import messagesbase.messagesfromserver.GameState;
import reactor.core.publisher.Mono;

public class MainClient {

	private static final String FIRST_NAME = "Daniel";
	private static final String LAST_NAME = "Kral";
	private static final String UACCOUNT = "krald88";

	public static void main(String[] args) {
		// parse these parameters in compliance to the automatic client evaluation
		String serverBaseUrl = args[1];
		String gameId = args[2];

		WebClient baseWebClient = WebClient.builder().baseUrl(serverBaseUrl + "/games")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
				.build();

		PlayerRegistration playerReg = new PlayerRegistration(FIRST_NAME, LAST_NAME, UACCOUNT);
		Mono<ResponseEnvelope> webAccess = baseWebClient
				.method(HttpMethod.POST)
				.uri("/" + gameId + "/players")
				.body(BodyInserters.fromValue(playerReg))
				.retrieve().bodyToMono(ResponseEnvelope.class);

		ResponseEnvelope<UniquePlayerIdentifier> resultReg = webAccess.block();

		if (resultReg.getState() == ERequestState.Error) {
			System.err.println("Client error, errormessage: " + resultReg.getExceptionMessage());
		} else {
			UniquePlayerIdentifier uniqueID = resultReg.getData().get();
			System.out.println("My Player ID: " + uniqueID.getUniquePlayerID());
		}
	}

	public static void exampleForGetRequests() throws Exception {
		String baseUrl = "UseValueFromARGS_1 FROM main";
		String gameId = "UseValueFromARGS_2 FROM main";
		String playerId = "From the client registration";

		WebClient baseWebClient = WebClient.builder().baseUrl(baseUrl + "/games")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE).build();

		Mono<ResponseEnvelope> webAccess = baseWebClient.method(HttpMethod.GET)
				.uri("/" + gameId + "/states/" + playerId).retrieve().bodyToMono(ResponseEnvelope.class);

		ResponseEnvelope<GameState> requestResult = webAccess.block();

		if (requestResult.getState() == ERequestState.Error) {
			System.err.println("Client error, errormessage: " + requestResult.getExceptionMessage());
		} else {
			GameState currentServerGameState = requestResult.getData().get();
		}
	}
}
