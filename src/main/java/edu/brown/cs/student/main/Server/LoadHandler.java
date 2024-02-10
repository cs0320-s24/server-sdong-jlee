package edu.brown.cs.student.main.Server;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoadHandler implements Route{

    @Override
    public Object handle(Request request, Response response) throws Exception {
        Set<String> params = request.queryParams();
             System.out.println(params);
        String filepath = request.queryParams("filepath");
             System.out.println(filepath);

        // Creates a hashmap to store the results of the request
        Map<String, Object> responseMap = new HashMap<>();
        try {
            System.out.println("success");
            // Sends a request to the API and receives JSON back
            String filepathJson = this.sendRequest();

            // Deserializes JSON into an Activity
//            Activity activity = ActivityAPIUtilities.deserializeActivity(activityJson);


            // Adds results to the responseMap
            responseMap.put("result", "success");
//            responseMap.put("activity", activity);
            return responseMap;

        } catch (Exception e) {
            e.printStackTrace();
            // This is a relatively unhelpful exception message. An important part of this sprint will be
            // in learning to debug correctly by creating your own informative error messages where Spark
            // falls short.
            responseMap.put("result", "Exception");
        }
        return responseMap;
    }

    private String sendRequest() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest buildBoredApiRequest =
                HttpRequest.newBuilder()
                        .uri(new URI("http://www.boredapi.com/api/activity/"))
                        .GET()
                        .build();

        // Send that API request then store the response in this variable. Note the generic type.
        HttpResponse<String> sentBoredApiResponse =
                HttpClient.newBuilder()
                        .build()
                        .send(buildBoredApiRequest, HttpResponse.BodyHandlers.ofString());

        // What's the difference between these two lines? Why do we return the body? What is useful from
        // the raw response (hint: how can we use the status of response)?
        System.out.println(sentBoredApiResponse);
        System.out.println(sentBoredApiResponse.body());

        return sentBoredApiResponse.body();

    }
}
