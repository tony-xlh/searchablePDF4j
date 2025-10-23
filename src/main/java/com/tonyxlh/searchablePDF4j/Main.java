package com.tonyxlh.searchablePDF4j;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;


public class Main {
    public static void main(String[] args) throws IOException {
        test();
        //SearchablePDFCreator.convert("F://WebTWAINImage.pdf","F://WebTWAINImage-searchable.pdf");
    }

    public static void test2() throws  IOException {
        File image = new File("F://WebTWAINImage.jpg");
        byte[] byteArray = new byte[(int) image.length()];
        try (FileInputStream inputStream = new FileInputStream(image)) {
            inputStream.read(byteArray);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //ArrayList<byte[]> images = new ArrayList<>();
        //images.add(byteArray);
        //SearchablePDFCreator.create(images,"F://output.pdf");
        String base64 = Base64.getEncoder().encodeToString(byteArray);
        OCRSpace.key = "04da52533288957";
        OCRResult result = OCRSpace.detect(base64);
        TextLine line = new TextLine(10,10,50,100,"我爱我的祖国！");
        result.lines.add(line);
        PDDocument document = new PDDocument();
        PDFont f = SearchablePDFCreator.loadFont(document,"G://ArialUnicodeMS.ttf");
        SearchablePDFCreator.addPage(byteArray,result,document,0,f);
        document.save(new File("F://output.pdf"));
        document.close();
    }

    public static void test() throws IOException {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDType0Font font = PDType0Font.load(doc, new File("G://ArialUnicodeMS.ttf"));

        PDPageContentStream stream = new PDPageContentStream(doc, page);
        stream.beginText();
        stream.setFont(font, 20);

        String text = "这是一个右起竖排的中文段落示例。我们希望文字可以正确复制。";

        float fontSize = 20f;
        float lineSpacing = fontSize * 1.2f;
        float columnWidth = fontSize * 1.5f;

        PDRectangle mediaBox = page.getMediaBox();
        float topY = mediaBox.getUpperRightY() - 80;
        float rightX = mediaBox.getUpperRightX() - 80;

        int charsPerColumn = 15;
        int col = 0;
        int line = 0;

        // ✅ 按阅读顺序绘制：先右列再左列，每列上到下
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            float x = rightX - col * columnWidth;
            float y = topY - line * lineSpacing;

            // 每个字符单独 beginText/endText，确保顺序稳定
            stream.endText();
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(x, y);
            stream.showText(String.valueOf(c));

            line++;
            if (line >= charsPerColumn) {
                line = 0;
                col++;
            }
        }

        stream.endText();
        stream.close();

        doc.save("vertical_copy_friendly.pdf");
        doc.close();
    }
}