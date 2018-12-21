package com.rmcsoft.aws.http;

import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

/**
 * @author Nikita Skornyakov
 * @since 0.1
 */
abstract class HttpClientBuilderBase<T, B extends HttpClientBuilderBase> {

  final HttpClient.Builder builder;

  HttpClientBuilderBase() {
    builder = HttpClient.newBuilder().version(Version.HTTP_2);
  }

  @SuppressWarnings("unchecked")
  private B self() {
    return (B) this;
  }

  /**
   * Requests a specific HTTP protocol version where possible.
   *
   * <p> If this method is not invoked prior to {@linkplain #build()
   * building}, then newly built clients will prefer {@linkplain
   * Version#HTTP_2 HTTP/2}.
   *
   * <p> If set to {@linkplain Version#HTTP_2 HTTP/2}, then each request
   * will attempt to upgrade to HTTP/2. If the upgrade succeeds, then the
   * response to this request will use HTTP/2 and all subsequent requests
   * and responses to the same
   * <a href="https://tools.ietf.org/html/rfc6454#section-4">origin server</a>
   * will use HTTP/2. If the upgrade fails, then the response will be
   * handled using HTTP/1.1
   *
   * @param version the requested HTTP protocol version
   * @return this builder
   * @implNote Constraints may also affect the selection of protocol version.
   * For example, if HTTP/2 is requested through a proxy, and if the implementation
   * does not support this mode, then HTTP/1.1 may be used
   */

  public B version(Version version) {
    builder.version(version);
    return self();
  }

  /**
   * Sets the connect timeout duration for this client.
   *
   * @param duration the duration to allow the underlying connection to be
   * established
   * @return this builder
   * @throws IllegalArgumentException if the duration is non-positive
   */
  public B connectTimeout(Duration duration) {
    builder.connectTimeout(duration);
    return self();
  }

  /**
   * Sets the executor to be used for asynchronous and dependent tasks.
   *
   * <p> If this method is not invoked prior to {@linkplain #build()
   * building}, a default executor is created for each newly built {@code
   * HttpClient}.
   *
   * @param executor the Executor
   * @return this builder
   */
  public B executor(Executor executor) {
    builder.executor(executor);
    return self();
  }

  /**
   * Sets a cookie handler.
   *
   * @param cookieHandler the cookie handler
   * @return this builder
   */
  public B cookieHandler(CookieHandler cookieHandler) {
    builder.cookieHandler(cookieHandler);
    return self();
  }

  /**
   * Specifies whether requests will automatically follow redirects issued
   * by the server.
   *
   * <p> If this method is not invoked prior to {@linkplain #build()
   * building}, then newly built clients will use a default redirection
   * policy of {@link Redirect#NEVER NEVER}.
   *
   * @param policy the redirection policy
   * @return this builder
   */
  public B followRedirects(Redirect policy) {
    builder.followRedirects(policy);
    return self();
  }

  /**
   * Sets an {@code SSLContext}.
   *
   * <p> If this method is not invoked prior to {@linkplain #build()
   * building}, then newly built clients will use the {@linkplain
   * SSLContext#getDefault() default context}, which is normally adequate
   * for client applications that do not need to specify protocols, or
   * require client authentication.
   *
   * @param sslContext the SSLContext
   * @return this builder
   */
  public B sslContext(SSLContext sslContext) {
    builder.sslContext(sslContext);
    return self();
  }

  /**
   * Sets an {@code SSLParameters}.
   *
   * <p> If this method is not invoked prior to {@linkplain #build()
   * building}, then newly built clients will use a default,
   * implementation specific, set of parameters.
   *
   * <p> Some parameters which are used internally by the HTTP Client
   * implementation (such as the application protocol list) should not be
   * set by callers, as they may be ignored. The contents of the given
   * object are copied.
   *
   * @param sslParameters the SSLParameters
   * @return this builder
   */
  public B sslParameters(SSLParameters sslParameters) {
    builder.sslParameters(sslParameters);
    return self();
  }

  /**
   * Sets the default priority for any HTTP/2 requests sent from this
   * client. The value provided must be between {@code 1} and {@code 256}
   * (inclusive).
   *
   * @param priority the priority weighting
   * @return this builder
   * @throws IllegalArgumentException if the given priority is out of range
   */
  public B priority(int priority) {
    builder.priority(priority);
    return self();
  }

  /**
   * Sets a {@link ProxySelector}.
   *
   * @param proxySelector the ProxySelector
   * @return this builder
   * @apiNote {@link ProxySelector#of(InetSocketAddress) ProxySelector::of}
   * provides a {@code ProxySelector} which uses a single proxy for all
   * requests. The system-wide proxy selector can be retrieved by
   * {@link ProxySelector#getDefault()}.
   */
  public B proxy(ProxySelector proxySelector) {
    builder.proxy(proxySelector);
    return self();
  }

  /**
   * Sets an authenticator to use for HTTP authentication.
   *
   * @param authenticator the Authenticator
   * @return this builder
   */
  public B authenticator(Authenticator authenticator) {
    builder.authenticator(authenticator);
    return self();
  }

  public abstract T build();

}
