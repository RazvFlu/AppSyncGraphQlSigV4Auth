package com.graphql.netflix.dgs.sigv4.example;

import com.netflix.graphql.dgs.client.CustomMonoGraphQLClient;
import com.netflix.graphql.dgs.client.HttpResponse;
import com.netflix.graphql.dgs.client.MonoGraphQLClient;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.utils.IoUtils;
import software.amazon.awssdk.utils.StringInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class AppSyncGraphQlSigV4Example {
  // Replace the endpoint with the url of the AWS AppSync API
  private final static String GRAPH_QL_ENDPOINT = "https://api.test.io/graphql";
  private final static String AWS_REGION = "us-east-1";

  /**
   * This code is an example for how to sign requests made by the Netflix DGS GraphQL client to AWS AppSync.
   * Documentation: <a href="https://netflix.github.io/dgs/advanced/java-client/">...</a>
   */
  public static void main(String[] args) {
    // Create a SigV4 signer, use your default credentials or profile credentials or container credentials

    Aws4Signer signer = Aws4Signer.create();
    Aws4SignerParams signerParams = Aws4SignerParams.builder()
        .awsCredentials(DefaultCredentialsProvider.create().resolveCredentials())
        .signingName("appsync")
        .signingRegion(Region.of(AWS_REGION))
        .build();

    // Create an Apache HTTP client
    SdkHttpClient sdkHttpClient = ApacheHttpClient.builder().build();

    // Create a custom reactive GraphQL client (
    CustomMonoGraphQLClient client = MonoGraphQLClient.createCustomReactive(GRAPH_QL_ENDPOINT, (requestUrl, headers, body) -> {
      HttpHeaders httpHeaders = new HttpHeaders();
      headers.forEach(httpHeaders::addAll);

      try {
        // Create the request to be signed
        SdkHttpFullRequest request = SdkHttpFullRequest.builder()
            .uri(new URI(GRAPH_QL_ENDPOINT))
            .method(SdkHttpMethod.POST)
            .headers(httpHeaders)
            .contentStreamProvider(() -> new StringInputStream(body))
            .build();

        SdkHttpFullRequest signedRequest = signer.sign(request, signerParams);

        // Create the executor request, do not forget to add the body in this class otherwise the request will fail
        HttpExecuteRequest.Builder executeRequestBuilder = HttpExecuteRequest.builder()
            .request(signedRequest);
        request.contentStreamProvider().ifPresent(executeRequestBuilder::contentStreamProvider);

        HttpExecuteResponse executeResponse = sdkHttpClient.prepareRequest(executeRequestBuilder.build()).call();
        SdkHttpResponse httpResponse = executeResponse.httpResponse();

        // Process the response and send it back to the client
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        executeResponse.responseBody().ifPresentOrElse(
            abortableInputStream -> {
              try {
                IoUtils.copy(abortableInputStream, byteArrayOutputStream);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }, () -> System.out.println("ERROR: No response body."));

        return Mono.just(new HttpResponse(httpResponse.statusCode(),
            byteArrayOutputStream.toString(StandardCharsets.UTF_8),
            httpResponse.headers()));

      } catch (URISyntaxException | IOException e) {
        throw new RuntimeException(e);
      }
    });


    // Use the client to make a request
    AppSyncBO appSyncBO = new AppSyncBO(client);

    // Test your stuff
    appSyncBO.writeMutation("testUserId");
    System.out.println(appSyncBO.queryData());
  }
}
