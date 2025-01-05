package client.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;
import java.util.Optional;

import messagesbase.ResponseEnvelope;
import messagesbase.messagesfromclient.ERequestState;
import reactor.core.publisher.Mono;

public class GameServerClient {

    private static final Logger logger = LoggerFactory.getLogger(GameServerClient.class);

    private static final long SERVER_WAIT_TIME_MS = 400L;

    private final WebClient webClient;

    public GameServerClient(String serverBaseUrl) {
        webClient = WebClient.builder()
                .baseUrl(serverBaseUrl + "/games")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();
    }

    private static Mono<? extends Throwable> handleClientError(ClientResponse response) {
        String message = String.format("Client error: %s", response.statusCode());

        return Mono.error(new GameServerClientException(message));
    }

    private static Mono<? extends Throwable> handleServerError(ClientResponse response) {
        String message = String.format("Server error: %s", response.statusCode());

        return Mono.error(new GameServerClientException(message));
    }

    /**
     * Suspend the current thread to wait for the server.
     * <p>
     * This ensures that we wait at least the amount of the time to allow the server to process
     * requests from other clients with respect to fairness.
     *
     * @param reason message to print while waiting
     */
    public static void suspendForServer(String reason) {
        try {
            Thread.sleep(SERVER_WAIT_TIME_MS);
        } catch (InterruptedException e) {
            logger.warn("Unexpected interrupt while {}", reason, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Makes a blocking HTTP POST request to the given server's API endpoint {@code relativeUri} by
     * sending the given input data {@code data} in the request's body and returns the server's
     * response on success. This method will throw a {@code GameServerClientException}, if the
     * server did not respond or responded with an error status code.
     *
     * @param relativeUri relative API endpoint URI
     * @param data        the data to send to the API endpoint
     * @param <R>         the expected type of the API endpoint's response
     * @param <T>         the type of the data sent to the API endpoint
     * @return response envelope from the server
     */
    public <R, T> ResponseEnvelope<R> post(String relativeUri, T data) {
        var requestBody = BodyInserters.fromValue(data);

        Mono<ResponseEnvelope> request = webClient
                .method(HttpMethod.POST)
                .uri(relativeUri)
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, GameServerClient::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, GameServerClient::handleServerError)
                .bodyToMono(ResponseEnvelope.class);

        ResponseEnvelope<R> response =
                Objects.requireNonNull(request.block(),
                                       "Client error: Response must not be empty.");

        if (response.getState() == ERequestState.Error) {
            String message = String.format("Client error: %s", response.getExceptionMessage());

            throw new GameServerClientException(message);
        }

        return response;
    }

    /**
     * Makes a blocking HTTP GET request to the given server's API endpoint {@code relativeUri} and
     * returns  the server's response on success. This method will throw a
     * {@code GameServerClientException}, if the server did not respond or responded with an error
     * status code.
     *
     * @param relativeUri relative API endpoint URI
     * @param <R>         the expected type of the API endpoint's response
     * @return response envelope from the server
     */
    private <R> ResponseEnvelope<R> get(String relativeUri) {
        Mono<ResponseEnvelope> request = webClient
                .method(HttpMethod.GET)
                .uri(relativeUri)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, GameServerClient::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, GameServerClient::handleServerError)
                .bodyToMono(ResponseEnvelope.class);

        ResponseEnvelope<R> response =
                Objects.requireNonNull(request.block(),
                                       "Client error: Response must not be empty.");

        if (response.getState() == ERequestState.Error) {
            String message = String.format("Client error: %s", response.getExceptionMessage());

            throw new GameServerClientException(message);
        }

        return response;
    }

    public <R, T> Optional<R> postAndGetData(String relativeUri, T data) {
        ResponseEnvelope<R> response = post(relativeUri, data);

        return response.getData();
    }

    public <R> Optional<R> getAndGetData(String relativeUri) {
        ResponseEnvelope<R> response = get(relativeUri);

        return response.getData();
    }
}
