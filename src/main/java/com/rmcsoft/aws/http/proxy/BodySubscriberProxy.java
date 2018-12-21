package com.rmcsoft.aws.http.proxy;

import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Modified implementation of standard subscriber provided by {@link BodySubscribers#ofPublisher()} to accept single
 * ByteBuffer instead of {@link List}.
 * Used to provide compatible implementation with {@link software.amazon.awssdk.http.async.SdkAsyncHttpResponseHandler}
 *
 * @author Nikita Skornyakov
 * @since 0.1
 */
public class BodySubscriberProxy implements BodySubscriber<Publisher<ByteBuffer>> {

  private final CompletableFuture<Subscription>
      subscriptionCF = new CompletableFuture<>();
  private final CompletableFuture<SubscriberRef>
      subscribedCF = new CompletableFuture<>();
  private AtomicReference<SubscriberRef>
      subscriberRef = new AtomicReference<>();
  private final CompletableFuture<Publisher<ByteBuffer>> body =
      subscriptionCF.thenCompose(
          (s) -> CompletableFuture.completedFuture(this::subscribe));

  private final CompletableFuture<Void> completionCF;

  BodySubscriberProxy() {
    completionCF = new CompletableFuture<>();
    completionCF.whenComplete(
        (r, t) -> subscribedCF.thenAccept(s -> complete(s, t)));
  }

  public CompletionStage<Publisher<ByteBuffer>> getBody() {
    return body;
  }

  static final class SubscriberRef {

    Subscriber<? super ByteBuffer> ref;

    SubscriberRef(Subscriber<? super ByteBuffer> subscriber) {
      ref = subscriber;
    }

    Subscriber<? super ByteBuffer> get() {
      return ref;
    }

    Subscriber<? super ByteBuffer> clear() {
      Subscriber<? super ByteBuffer> res = ref;
      ref = null;
      return res;
    }
  }

  final static class SubscriptionRef implements Subscription {

    final Subscription subscription;
    final SubscriberRef subscriberRef;

    SubscriptionRef(Subscription subscription,
        SubscriberRef subscriberRef) {
      this.subscription = subscription;
      this.subscriberRef = subscriberRef;
    }

    @Override
    public void request(long n) {
      if (subscriberRef.get() != null) {
        subscription.request(n);
      }
    }

    @Override
    public void cancel() {
      subscription.cancel();
      subscriberRef.clear();
    }

    void subscribe() {
      Subscriber<?> subscriber = subscriberRef.get();
      if (subscriber != null) {
        subscriber.onSubscribe(this);
      }
    }

    @Override
    public String toString() {
      return String
          .format("SubscriptionRef/%s@%s", subscription.getClass().getName(), System.identityHashCode(subscription));
    }

  }

  // This is a callback for the subscribedCF.
  // Do not call directly!
  private void complete(SubscriberRef ref, Throwable t) {
    Subscriber<?> s = ref.clear();
    // maybe null if subscription was cancelled
    if (s == null) {
      return;
    }
    if (t != null) {
      s.onError(t);
      return;
    }

    try {
      s.onComplete();
    } catch (Throwable x) {
      s.onError(x);
    }
  }

  private void signalError(Throwable err) {
    completionCF.completeExceptionally(err != null ? err : new IllegalArgumentException("null throwable"));
  }

  private void signalComplete() {
    completionCF.complete(null);
  }

  private void subscribe(Subscriber<? super ByteBuffer> subscriber) {
    if (subscriber == null) {
      throw new IllegalArgumentException("subscriber must not be null");
    }
    SubscriberRef ref = new SubscriberRef(subscriber);
    if (subscriberRef.compareAndSet(null, ref)) {
      subscriptionCF.thenAccept((s) -> {
        SubscriptionRef subscription = new SubscriptionRef(s, ref);
        try {
          subscription.subscribe();
          subscribedCF.complete(ref);
        } catch (Throwable t) {
          subscription.cancel();
        }
      });
    } else {
      subscriber.onSubscribe(new Subscription() {
        @Override
        public void request(long n) {
        }

        @Override
        public void cancel() {
        }
      });
      subscriber.onError(new IllegalStateException("This publisher has already one subscriber"));
    }
  }

  @Override
  public void onSubscribe(Subscription subscription) {
    subscriptionCF.complete(subscription);
  }

  @Override
  public void onNext(List<ByteBuffer> item) {
    try {
      SubscriberRef ref = subscriberRef.get();
      Subscriber<? super ByteBuffer> subscriber = ref.get();
      if (subscriber != null) { // may be null if subscription was cancelled.
        item.forEach(subscriber::onNext);
      }
    } catch (Throwable err) {
      signalError(err);
      subscriptionCF.thenAccept(Subscription::cancel);
    }
  }

  @Override
  public void onError(Throwable throwable) {
    // onError can be called before request(1), and therefore can
    // be called before subscriberRef is set.
    signalError(throwable);
  }

  @Override
  public void onComplete() {
    // cannot be called before onSubscribe()
    if (!subscriptionCF.isDone()) {
      signalError(new InternalError("onComplete called before onSubscribed"));
    } else {
      // onComplete can be called before request(1),
      // and therefore can be called before subscriberRef
      // is set.
      signalComplete();
    }
  }
}
