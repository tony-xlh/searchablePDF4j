package com.tonyxlh.searchablePDF4j;

import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

public class SearchablePDFCreator {
    public static void create(ArrayList<byte[]> images,String outputPath) throws IOException {
        // Load the PDF document
        PDDocument document = new PDDocument();
        int index = 0;
        for (byte[] imageBytes:images) {
            addPage(imageBytes,document,index);
            index = index + 1;
        }

        // Save the new PDF document
        document.save(new File(outputPath));
        document.close();
    }

    private static void addPage(byte[] imageBytes,PDDocument document,int index) throws IOException {
        Image img = new Image(new ByteArrayInputStream(imageBytes));
        // Create a new PDF page
        PDRectangle rect = new PDRectangle((float) img.getWidth(),(float) img.getHeight());
        PDPage page = new PDPage(rect);
        document.addPage(page);
        // Set the font and size for the text
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        PDImageXObject image
                = PDImageXObject.createFromByteArray(document,imageBytes,String.valueOf(index));
        contentStream.drawImage(image, 0, 0);
        PDFont font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        contentStream.setFont(font, 16);
        contentStream.setRenderingMode(RenderingMode.NEITHER);
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        OCRResult result = OCRSpace.detect(base64);
        for (int i = 0; i <result.lines.size() ; i++) {
            TextLine line = result.lines.get(i);
            FontInfo fi = calculateFontSize(font,line.text, (float) line.width, (float) line.height);
            contentStream.beginText();
            contentStream.setFont(font, fi.fontSize);
            contentStream.newLineAtOffset((float) line.left, (float) (img.getHeight() - line.top - line.height));
            contentStream.showText(line.text);
            contentStream.endText();
        }
        contentStream.close();
    }
    private static FontInfo calculateFontSize(PDFont font, String text, float bbWidth, float bbHeight) throws IOException {
        int fontSize = 17;
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;
        float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;

        if(textWidth > bbWidth){
            while(textWidth > bbWidth){
                fontSize -= 1;
                textWidth = font.getStringWidth(text) / 1000 * fontSize;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
            }
        }
        else if(textWidth < bbWidth){
            while(textWidth < bbWidth){
                fontSize += 1;
                textWidth = font.getStringWidth(text) / 1000 * fontSize;
                textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
            }
        }

        FontInfo fi = new FontInfo();
        fi.fontSize = fontSize;
        fi.textHeight = textHeight;
        fi.textWidth = textWidth;

        return fi;
    }

}
