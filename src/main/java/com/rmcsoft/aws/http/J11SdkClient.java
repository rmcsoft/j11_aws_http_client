package com.rmcsoft.aws.http;

import software.amazon.awssdk.http.ExecutableHttpRequest;
import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.SdkHttpClient;

/**
 * @author Nikita Skornyakov
 * @since 0.1
 */
public class J11SdkClient implements SdkHttpClient {

  @Override
  public ExecutableHttpRequest prepareRequest(HttpExecuteRequest request) {
    return null; //TODO: implement method
  }

  @Override
  public void close() {
    //TODO: implement method
  }
}
