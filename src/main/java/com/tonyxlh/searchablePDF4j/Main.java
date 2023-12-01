package com.tonyxlh.searchablePDF4j;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class Main {
    public static void main(String[] args) {
        File image = new File("F://WebTWAINImage.jpg");
        byte[] byteArray = new byte[(int) image.length()];
        try (FileInputStream inputStream = new FileInputStream(image)) {
            inputStream.read(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String base64 = Base64.getEncoder().encodeToString(byteArray);
        try {
            GoogleOCR.detect(base64);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Hello world!");
    }
}