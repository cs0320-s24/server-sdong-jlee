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
    private CSVState csvState;
    public LoadHandler(CSVState csvState) {
        this.csvState = csvState;
    }
    @Override
    public Object handle(Request request, Response response) throws Exception {
        Set<String> params = request.queryParams();
             System.out.println(params);
        String filepath = request.queryParams("filepath");
             System.out.println(filepath);

        // Creates a hashmap to store the results of the request
        Map<String, Object> responseMap = new HashMap<>();
        try {
            this.csvState.setFileName(filepath);
            responseMap.put("result", "success");

        } catch (Exception e) {
            e.printStackTrace();
            // This is a relatively unhelpful exception message. An important part of this sprint will be
            // in learning to debug correctly by creating your own informative error messages where Spark
            // falls short.

            responseMap.put("result", "Exception");
        }
        return responseMap;
    }

}
