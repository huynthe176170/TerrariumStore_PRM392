package com.example.project_terrarium_prm392.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeGenerator {
    private static final String TAG = "QRCodeGenerator";
    private static final int MAX_QR_SIZE = 1000; // Giới hạn kích thước tối đa của QR code
    
    /**
     * Tạo QR code từ nội dung chuỗi
     * @param content Nội dung cần mã hóa thành QR code
     * @param width Chiều rộng của QR code
     * @param height Chiều cao của QR code
     * @return Bitmap chứa QR code hoặc null nếu có lỗi
     */
    public static Bitmap generateQRCode(String content, int width, int height) {
        if (content == null || content.isEmpty()) {
            Log.e(TAG, "Content is null or empty");
            return null;
        }
        
        // Giới hạn kích thước để tránh lỗi OutOfMemory
        int safeWidth = Math.min(width, MAX_QR_SIZE);
        int safeHeight = Math.min(height, MAX_QR_SIZE);
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, safeWidth, safeHeight);
            return createBitmap(bitMatrix);
        } catch (WriterException e) {
            Log.e(TAG, "Error encoding QR code", e);
            return null;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory error while creating QR code", e);
            // Thử lại với kích thước nhỏ hơn
            try {
                BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200);
                return createBitmap(bitMatrix);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to create QR with smaller size", ex);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error", e);
            return null;
        }
    }
    
    /**
     * Tạo Bitmap từ BitMatrix
     * @param matrix BitMatrix của QR code
     * @return Bitmap của QR code
     */
    private static Bitmap createBitmap(BitMatrix matrix) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error creating bitmap from matrix", e);
            return null;
        }
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
        try {
            // Format theo chuẩn VietQR
            return String.format("PAYMENT|ORDER:%s|AMOUNT:%.0f|TO:%s|PHONE:%s", 
                                orderId, amount, sellerName, sellerPhone);
        } catch (Exception e) {
            Log.e(TAG, "Error creating payment content", e);
            return "PAYMENT ERROR";
        }
    }
} 