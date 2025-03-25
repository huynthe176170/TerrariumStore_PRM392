package com.example.project_terrarium_prm392.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeGenerator {
    
    /**
     * Tạo QR code từ nội dung chuỗi
     * @param content Nội dung cần mã hóa thành QR code
     * @param width Chiều rộng của QR code
     * @param height Chiều cao của QR code
     * @return Bitmap chứa QR code
     */
    public static Bitmap generateQRCode(String content, int width, int height) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            return createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Tạo Bitmap từ BitMatrix
     * @param matrix BitMatrix của QR code
     * @return Bitmap của QR code
     */
    private static Bitmap createBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
    
    /**
     * Tạo nội dung chuỗi cho QR code thanh toán
     * @param orderId ID đơn hàng
     * @param amount Số tiền cần thanh toán
     * @param sellerName Tên người bán
     * @param sellerPhone Số điện thoại người bán
     * @return Chuỗi nội dung QR code
     */
    public static String createPaymentContent(String orderId, double amount, String sellerName, String sellerPhone) {
        // Format theo chuẩn VietQR
        return String.format("PAYMENT|ORDER:%s|AMOUNT:%.0f|TO:%s|PHONE:%s", 
                             orderId, amount, sellerName, sellerPhone);
    }
} 