package client.network;

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

    private final WebClient webClient;

    public GameServerClient(String serverBaseUrl) {
        webClient = WebClient.builder()
                .baseUrl(serverBaseUrl + "/games")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .build();
    }

    private Mono<? extends Throwable> handleClientError(ClientResponse response) {
        String message = String.format("Client error: %s", response.statusCode());

        return Mono.error(new GameServerClientException(message));
    }

    private Mono<? extends Throwable> handleServerError(ClientResponse response) {
        String message = String.format("Server error: %s", response.statusCode());

        return Mono.error(new GameServerClientException(message));
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
                .onStatus(HttpStatusCode::is4xxClientError, this::handleClientError)
                .onStatus(HttpStatusCode::is5xxServerError, this::handleServerError)
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
}
