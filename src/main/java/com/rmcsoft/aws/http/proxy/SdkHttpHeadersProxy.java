package com.rmcsoft.aws.http.proxy;

import java.net.http.HttpResponse.ResponseInfo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.http.SdkHttpFullResponse;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.async.SdkAsyncHttpResponseHandler;

/**
 * Proxy that wraps {@link ResponseInfo} provided by Java 11 Http client to be allowed to consume it by
 * {@link SdkAsyncHttpResponseHandler}.
 *
 * @author Nikita Skornyakov
 * @implNote this class implements {@link SdkHttpFullResponse} instead of
 * {@link software.amazon.awssdk.http.SdkHttpHeaders} due to AWS SDK v2.2.0 requires specifically full response.
 * Otherwise it fails with {@link ClassCastException}.
 * @date 12.12.2018
 */
public final class SdkHttpHeadersProxy implements SdkHttpFullResponse {

  private final ResponseInfo responseInfo;

  private SdkHttpHeadersProxy(ResponseInfo responseInfo) {
    this.responseInfo = responseInfo;
  }

  public static SdkHttpResponse of(ResponseInfo responseInfo) {
    if (responseInfo == null) {
      throw new IllegalArgumentException("responseInfo must not be null");
    }
    return new SdkHttpHeadersProxy(responseInfo);
  }

  @Override
  public Optional<String> statusText() {
    return Optional.empty();
  }

  @Override
  public int statusCode() {
    return responseInfo.statusCode();
  }

  @Override
  public Map<String, List<String>> headers() {
    return responseInfo.headers().map();
  }

  @Override
  public Builder toBuilder() {
    return SdkHttpResponse
      .builder()
      .headers(headers())
      .statusCode(statusCode());
  }

  @Override
  public Optional<AbortableInputStream> content() {
    return Optional.empty(); // will be available at later stage
  }
}

