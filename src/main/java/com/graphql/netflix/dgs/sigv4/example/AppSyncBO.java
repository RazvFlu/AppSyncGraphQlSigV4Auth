package com.graphql.netflix.dgs.sigv4.example;

import com.netflix.graphql.dgs.client.CustomMonoGraphQLClient;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import graphql.example.generated.client.ReadGraphQLQuery;
import graphql.example.generated.client.ReadProjectionRoot;
import graphql.example.generated.client.WriteGraphQLQuery;
import graphql.example.generated.client.WriteProjectionRoot;
import graphql.example.generated.types.ReadData;
import graphql.example.generated.types.WriteDataInput;
import org.intellij.lang.annotations.Language;
import reactor.core.publisher.Mono;

public class AppSyncBO {
  private final CustomMonoGraphQLClient graphQLClient;

  public AppSyncBO(CustomMonoGraphQLClient graphQLClient) {
    this.graphQLClient = graphQLClient;
  }

  public String queryData() {
    ReadData readData = ReadData.newBuilder()
        .userId("testUserId")
        .build();

    @Language("graphql") String query = new GraphQLQueryRequest(
        ReadGraphQLQuery.newRequest()
            .input(readData)
            .queryName("read")
            .build(),
        new ReadProjectionRoot()
            .userId()
    ).serialize();

    Mono<GraphQLResponse> graphQLResponseMono = graphQLClient.reactiveExecuteQuery(query);

    GraphQLResponse graphQLResponse = graphQLResponseMono.block(); // or subscribe... whatever you want

    return graphQLResponse.getData().get("read").toString();
  }

  public void writeMutation(String userId) {
    WriteDataInput writeDataInput = WriteDataInput.newBuilder()
        .userId(userId)
        .build();

    @Language("graphql") String query = new GraphQLQueryRequest(
        WriteGraphQLQuery.newRequest()
            .input(writeDataInput)
            .queryName("write")
            .build(),
        new WriteProjectionRoot()
            .userId()
    ).serialize();

    Mono<GraphQLResponse> graphQLResponseMono = graphQLClient.reactiveExecuteQuery(query);

    graphQLResponseMono.block(); // or subscribe
  }
}
