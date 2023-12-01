package com.tonyxlh.searchablePDF4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GoogleOCR {
    public static String key = "";
    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    public static OCRResult detect(String base64) throws IOException {
        OCRResult result = new OCRResult();
        Map<String,Object> body = new HashMap<String,Object>();
        ArrayList<Map<String,Object>> requests = new ArrayList<>();
        Map<String,Object> request = new HashMap<String,Object>();
        Map<String,Object> image = new HashMap<String,Object>();
        image.put("content",base64);
        ArrayList<Map<String,Object>> features = new ArrayList<>();
        Map<String,Object> feature = new HashMap<String,Object>();
        feature.put("type","TEXT_DETECTION");
        features.add(feature);
        request.put("image",image);
        request.put("features",features);
        requests.add(request);
        body.put("requests",requests);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonBody = objectMapper.writeValueAsString(body);
        System.out.println(jsonBody);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .build();
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request httpRequest = new Request.Builder()
                .url("https://vision.googleapis.com/v1/images:annotate?key="+key)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(httpRequest).execute()) {
            try {
                String json = response.body().string();
                String path = "F:\\ocr.json";
                Files.write( Paths.get(path), json.getBytes());
                System.out.println(json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }
}
