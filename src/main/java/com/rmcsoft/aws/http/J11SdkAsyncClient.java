package com.rmcsoft.aws.http;

import static java.util.function.Predicate.not;

import com.rmcsoft.aws.http.proxy.BodyHandlerProxy;
import com.rmcsoft.aws.http.proxy.BodyPublisherProxy;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import org.reactivestreams.FlowAdapters;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;

/**
 * {@link SdkAsyncHttpClient} implementation over Java 11 http client.
 *
 * @author Nikita Skornyakov
 * @since 0.1
 */
public class J11SdkAsyncClient implements SdkAsyncHttpClient {

  private final HttpClient client;

  private static final Set<String> DISALLOWED_HEADERS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER) {{
    addAll(
      Set.of("Connection",
        "Content-Length",
        "Date",
        "Expect",
        "From",
        "Host",
        "Origin",
        "Referer",
        "Upgrade",
        "via",
        "Warning"));
  }};

  J11SdkAsyncClient(HttpClient client) {
    this.client = client;
  }

  /**
   * Returns new builder for {@link SdkAsyncHttpClient} that wraps standard java {@link HttpClient}.
   *
   * @return new builder instance.
   * @see HttpClient.Builder
   */
  public static AsyncHttpClientBuilder newBuilder() {
    return new AsyncHttpClientBuilder();
  }

  @Override
  public CompletableFuture<Void> execute(AsyncExecuteRequest request) {

    var httpRequestBuilder = HttpRequest.newBuilder();

    var method = request.request().method();

    switch (method) {
      case GET:
        httpRequestBuilder.GET();
        break;
      case DELETE:
        httpRequestBuilder.DELETE();
        break;
      default:
        httpRequestBuilder.method(method.name(), BodyPublisherProxy.of(request.requestContentPublisher()));
        break;
    }

    request
      .request()
      .headers()
      .entrySet()
      .stream()
      .filter(not(x -> DISALLOWED_HEADERS.contains(x.getKey())))
      .forEach(x -> x.getValue().forEach(h -> httpRequestBuilder.header(x.getKey(), h)));
    httpRequestBuilder
      .uri(request.request().getUri());

    var responseHandler = request.responseHandler();
    var bodyHandler = BodyHandlerProxy.of(responseHandler);

    return client
      .sendAsync(httpRequestBuilder.build(), bodyHandler)
      .thenApply(HttpResponse::body)
      .thenApply(FlowAdapters::toPublisher)
      .thenAccept(responseHandler::onStream)
      .exceptionally(t -> {
        responseHandler.onError(t);
        return null;
      });
  }

  @Override
  public void close() {
    //do nothing
  }
}
