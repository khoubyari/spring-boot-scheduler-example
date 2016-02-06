Look in resources/application.yml for the path to the output directory

Run this sample with maven: `mvn clean spring-boot:run`

This sample uses Spring scheduler to periodicallycall a sample REST API, binds the JSON response to a Java POJO, then writes the JSON string to a file. Uses RestTemplate to make the API call.

