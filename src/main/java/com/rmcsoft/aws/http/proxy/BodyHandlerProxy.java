package com.rmcsoft.aws.http.proxy;

import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Publisher;
import software.amazon.awssdk.http.async.SdkAsyncHttpResponseHandler;

/**
 * Body Handler proxy between {@link BodyHandler} used by standard Java HTTP client and Amazon SDK 2.0
 * {@link SdkAsyncHttpResponseHandler}.
 *
 * @author Nikita Skornyakov
 * @date 12.12.2018
 */
public final class BodyHandlerProxy implements BodyHandler<Publisher<ByteBuffer>> {

  private final SdkAsyncHttpResponseHandler handler;

  public static BodyHandler<Publisher<ByteBuffer>> of(SdkAsyncHttpResponseHandler handler) {
    if (handler == null) {
      throw new IllegalArgumentException("handler must be not null");
    }

    return new BodyHandlerProxy(handler);
  }

  private BodyHandlerProxy(SdkAsyncHttpResponseHandler responseHandler) {
    handler = responseHandler;
  }

  @Override
  public BodySubscriber<Publisher<ByteBuffer>> apply(ResponseInfo responseInfo) {
    handler.onHeaders(SdkHttpHeadersProxy.of(responseInfo));
    return new BodySubscriberProxy();
  }
}
