package com.blulabellabs.code.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.blulabellabs.code.ApplicationConstants;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;


/**
 * Created by Alex on 10/14/13.
 */
public class QrUtils {

    public static int getDefaultHeightPx(Context context){
        return Converter.dipToPx(context, ApplicationConstants.DEFAULT_HEIGHT_DP);
    }

    public static Bitmap encodeQrCode(String uniqueID, int nWidth, int nHeight, int color, int background) {
        BarcodeFormat barcodeFormat = BarcodeFormat.QR_CODE;

        QRCodeWriter writer = new QRCodeWriter();
        try
        {
            EnumMap<EncodeHintType, Object> hint = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hint.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = writer.encode(uniqueID, barcodeFormat, nWidth, nHeight, hint);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++)
            {
                int offset = y * width;
                for (int x = 0; x < width; x++)
                {

                    pixels[offset + x] = bitMatrix.get(x, y) ? color : background;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String addContactPrefix(String qr){
        return ApplicationConstants.QR_CODE_CONTACT_PREFIX + qr;

    }

    public static String removeContactPrefix(String qr){
        return qr.replaceFirst(ApplicationConstants.QR_CODE_CONTACT_PREFIX,"");
    }

    public static String addChatPrefix(String qr){
        return ApplicationConstants.QR_CODE_CHAT_PREFIX + qr;
    }

    public static String removeChatPrefix(String qr){
        return qr.replaceFirst(ApplicationConstants.QR_CODE_CHAT_PREFIX,"");
    }

}
