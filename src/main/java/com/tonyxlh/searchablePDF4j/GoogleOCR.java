package com.tonyxlh.searchablePDF4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.w3c.dom.Text;

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
                TextLine line = parseAsTextLine(paragraph);
                ocrResult.lines.add(line);
            }
        }
    }

    private static TextLine parseAsTextLine(Map<String,Object> item){
        Map<String,Object> boundingBox = (Map<String, Object>) item.get("boundingBox");
        ArrayList<Point> points = new ArrayList<Point>();

        List<Map<String,Object>> vertices = (List<Map<String, Object>>) boundingBox.get("vertices");
        int minX = (int) vertices.get(0).get("x");
        int minY = (int) vertices.get(0).get("y");
        int maxX = 0;
        int maxY = 0;
        for (Map<String,Object> vertice:vertices) {
            int x = (int) vertice.get("x");
            int y = (int) vertice.get("y");
            minX = Math.min(minX,x);
            minY = Math.min(minY,y);
            maxX = Math.max(maxX,x);
            maxY = Math.max(maxY,y);
        }
        String text = getTextOfParagraph(item);
        return new TextLine(minX,minY,maxX - minX,maxY-minY,text);
    }

    private static String getTextOfParagraph(Map<String,Object> item){
        StringBuilder sb = new StringBuilder();
        List<Map<String,Object>> words = (List<Map<String, Object>>) item.get("words");
        for (Map<String,Object> word:words) {
            boolean hasSpace = true;
            if (word.containsKey("property")) {
                Map<String,Object> property = (Map<String, Object>) word.get("property");
                List<Map<String,Object>> detectedLanguages = (List<Map<String, Object>>) property.get("detectedLanguages");
                for (Map<String,Object> detectedLanguage:detectedLanguages) {
                    String langcode = (String) detectedLanguage.get("languageCode");
                    if (langcode.startsWith("zh") || langcode.startsWith("ja")) {
                        hasSpace = false;
                    }
                }
            }
            List<Map<String,Object>> symbols = (List<Map<String, Object>>) word.get("symbols");
            for (Map<String,Object> symbol:symbols) {
              sb.append(symbol.get("text"));
            }
            if (hasSpace) {
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }
}
