//package com.keeptrip.keeptrip.utils;
//
//
//import android.graphics.Bitmap;
//import android.graphics.pdf.PdfDocument;
//import android.view.View;
//
//import java.io.File;
//
//public class PdfUtils {
//
//    public void saveImageToPDF(View title, Bitmap bitmap, String filename) {
//
//    File mFile = new File(new File(), filename + ".pdf");
//    if (!mFile.exists()) {
//        int height = title.getHeight() + bitmap.getHeight();
//        PdfDocument document = new PdfDocument();
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), height, 1).create();
//        PdfDocument.Page page = document.startPage(pageInfo);
//        Canvas canvas = page.getCanvas();
//        title.draw(canvas);
//
//        canvas.drawBitmap(bitmap, null, new Rect(0, title.getHeight(), bitmap.getWidth(),bitmap.getHeight()), null);
//
//        document.finishPage(page);
//
//        try {
//            mFile.createNewFile();
//            OutputStream out = new FileOutputStream(mFile);
//            document.writeTo(out);
//            document.close();
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
//    //Adding the content to the document
//    private void addContent(Document document, View view)
//            throws DocumentException
//    {
//        try
//        {
//            view.buildDrawingCache();
//
//            Bitmap bmp = view.getDrawingCache();
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            Image image = Image.getInstance(stream.toByteArray());
//            image.scalePercent(70);
//            image.setAlignment(Image.MIDDLE);
//            document.add(image);
//        }
//        catch (Exception ex)
//        {
//            Log.e("TAG-ORDER PRINT ERROR", ex.getMessage());
//        }
//    }
//}
