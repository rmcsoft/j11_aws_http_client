package com.rmcsoft.aws.http;

/**
 * @author Nikita Skornyakov
 * @since 0.1
 */
public class AsyncHttpClientBuilder extends HttpClientBuilderBase<J11SdkAsyncClient, AsyncHttpClientBuilder> {

  @Override
  public J11SdkAsyncClient build() {
    return new J11SdkAsyncClient(builder.build());
  }
}
