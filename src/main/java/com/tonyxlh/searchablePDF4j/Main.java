package com.tonyxlh.searchablePDF4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;


public class Main {
    public static void main(String[] args) throws IOException {
        File image = new File("F://WebTWAINImage.jpg");
        byte[] byteArray = new byte[(int) image.length()];
        try (FileInputStream inputStream = new FileInputStream(image)) {
            inputStream.read(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ArrayList<byte[]> images = new ArrayList<>();
        images.add(byteArray);
        SearchablePDFCreator.create(images,"F://output.pdf");
    }
}