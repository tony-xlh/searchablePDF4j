package com.tonyxlh.searchablePDF4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GoogleOCR {
    public static String key = "";
    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    public static OCRResult detect(String base64) throws IOException {
        OCRResult result = new OCRResult();
        File jsonFile = new File("F://ocr.json");
        if (jsonFile.exists()) {
            String content = new String(Files.readAllBytes(Paths.get("F://ocr.json")));
            parse(content,result);
        }else{
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
                    parse(json,result);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return result;
    }

    private static void parse(String json,OCRResult ocrResult) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String,Object> body = objectMapper.readValue(json,new TypeReference<Map<String,Object>>() {});
        List<Map<String,Object>> responses = (List<Map<String, Object>>) body.get("responses");
        Map<String,Object> response = responses.get(0);
        Map<String,Object> fullTextAnnotation = (Map<String, Object>) response.get("fullTextAnnotation");
        List<Map<String,Object>> pages = (List<Map<String, Object>>) fullTextAnnotation.get("pages");
        Map<String,Object> page = pages.get(0);
        List<Map<String,Object>> blocks = (List<Map<String, Object>>) page.get("blocks");
        for (Map<String,Object> block:blocks) {
            List<Map<String,Object>> paragraphs = (List<Map<String, Object>>) block.get("paragraphs");
            for (Map<String,Object> paragraph:paragraphs) {

                List<Map<String,Object>> words = (List<Map<String, Object>>) paragraph.get("words");
                for (Map<String,Object> word:words) {
                    List<Map<String,Object>> symbols = (List<Map<String, Object>>) word.get("symbols");
                    for (Map<String,Object> symbol:symbols) {
                        System.out.println(symbol);
                        Symbol ocrSymbol = new Symbol();
                        ocrSymbol.text = (String) symbol.get("text");
                        ocrSymbol.points = parseBoundingBox((Map<String, Object>) symbol.get("boundingBox"));
                        ocrResult.symbols.add(ocrSymbol);
                    }
                }
            }
        }
    }

    private static ArrayList<Point> parseBoundingBox(Map<String,Object> boundingBox){
        ArrayList<Point> points = new ArrayList<Point>();
        List<Map<String,Object>> vertices = (List<Map<String, Object>>) boundingBox.get("vertices");
        for (Map<String,Object> vertice:vertices) {
            int x = (int) vertice.get("x");
            int y = (int) vertice.get("y");
            Point point = new Point();
            point.x = x;
            point.y = y;
            points.add(point);
        }
        return points;
    }
}
