package com.tonyxlh.searchablePDF4j;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws IOException {
        File image = new File("F://WebTWAINImage.jpg");
        byte[] byteArray = new byte[(int) image.length()];
        try (FileInputStream inputStream = new FileInputStream(image)) {
            inputStream.read(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String base64 = Base64.getEncoder().encodeToString(byteArray);
        OCRResult result = GoogleOCR.detect(base64);
        System.out.println(result);
        System.out.println(result.symbols.size());
        System.out.println(result.symbols.get(0).points.get(0).x);
        System.out.println(result.symbols.get(0).points.get(0).y);
    }
}