package com.rmcsoft.aws.http.proxy;

import java.net.http.HttpRequest.BodyPublisher;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Subscriber;
import org.reactivestreams.FlowAdapters;
import software.amazon.awssdk.http.async.SdkHttpContentPublisher;

/**
 * Publisher proxy between {@link BodyPublisher} used by standard Java HTTP client and Amazon SDK 2.0
 * {@link SdkHttpContentPublisher}.
 *
 * @author Nikita Skornyakov
 * @since 0.1
 */
public final class BodyPublisherProxy implements BodyPublisher {

  private final SdkHttpContentPublisher publisher;

  public static BodyPublisher of(SdkHttpContentPublisher publisher) {
    if (publisher == null) {
      throw new IllegalArgumentException("publisher must not be null");
    }

    return new BodyPublisherProxy(publisher);
  }

  private BodyPublisherProxy(SdkHttpContentPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public long contentLength() {
    return publisher.contentLength().orElse(-1L);
  }

  @Override
  public void subscribe(Subscriber<? super ByteBuffer> subscriber) {
    publisher.subscribe(FlowAdapters.toSubscriber(subscriber));
  }
}
