# AppSyncGraphQlSigV4Auth
SigV4 Auth example in Java when using netflix graphql-dgs-client.
You can find the list of dependencies in the pom.xml file.

If you reached this page it means that you want to generate a graphQl client in java and use it with AWS AppSync or any other graphQl server that uses SigV4 authentication.

I could not find any example for how to SigV4 authentication in java when using netflix graphql-dgs-client. So I decided to create this example.

Tips for AWS AppSync:
- Give the required permissions to the service which you are running.
- Use AWS_IAM as the authorization type. Do not set up any additional authorization types.
The documentation is not clear about this, but then I tried to use AWS_IAM with API_KEY authorization it did not work.