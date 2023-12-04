package com.tonyxlh.searchablePDF4j;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

public class SearchablePDFCreator {
    public static void create(ArrayList<byte[]> images,String outputPath) throws IOException {
        // new PDF document
        PDDocument document = new PDDocument();
        int index = 0;
        for (byte[] imageBytes:images) {
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            OCRResult result = OCRSpace.detect(base64);
            addPage(imageBytes,result,document,index);
            index = index + 1;
        }
        // Save the new PDF document
        document.save(new File(outputPath));
        document.close();
    }

    public static void addPage(byte[] imageBytes,OCRResult result, PDDocument document,int pageIndex,PDFont pdFont) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(imageBytes);
        BufferedImage bi = ImageIO.read(in);
        // Create a new PDF page
        PDRectangle rect = new PDRectangle((float) bi.getWidth(),(float) bi.getHeight());
        PDPage page = new PDPage(rect);
        document.addPage(page);
        // Set the font and size for the text
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        PDImageXObject image
                = PDImageXObject.createFromByteArray(document,imageBytes,String.valueOf(pageIndex));
        contentStream.drawImage(image, 0, 0);
        addTextOverlay(contentStream,result,bi.getHeight(),pdFont);
        contentStream.close();
    }

    public static void addPage(byte[] imageBytes,OCRResult result, PDDocument document,int pageIndex) throws IOException {
        addPage(imageBytes,result,document,pageIndex,new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN));
    }
    public static void addTextOverlay(PDPageContentStream contentStream,OCRResult result, double pageHeight) throws IOException {
        addTextOverlay(contentStream,result,pageHeight,new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN));
    }
    public static void addTextOverlay(PDPageContentStream contentStream,OCRResult result, double pageHeight, PDFont pdFont) throws IOException {
        PDFont font = pdFont;
        contentStream.setFont(font, 16);
        contentStream.setRenderingMode(RenderingMode.NEITHER);
        for (int i = 0; i <result.lines.size() ; i++) {
            TextLine line = result.lines.get(i);
            FontInfo fi = calculateFontSize(font,line.text, (float) line.width, (float) line.height);
            contentStream.beginText();
            contentStream.setFont(font, fi.fontSize);
            contentStream.newLineAtOffset((float) line.left, (float) (pageHeight - line.top - line.height));
            contentStream.showText(line.text);
            contentStream.endText();
        }
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
