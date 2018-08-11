package com.face.sdk.preprocessing;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;


public class FramePreprocess {
    private static int lastBufflen;
    private static byte[] grayBuff;
    private static int[] rgbs;

    public static void getFrameBuff(int bufflen) {
        if (lastBufflen != bufflen) {
            grayBuff = new byte[bufflen];
            rgbs = new int[bufflen];
            lastBufflen = bufflen;
        }
    }

    /**
     * @param buff   帧数据
     * @param width  帧宽
     * @param height 帧高
     * @return 3通道的bitmap
     */
    public static Bitmap craeteBitmap(byte[] buff, int width, int height, boolean isGray) {
        if (isGray) {
            int bufflen = width * height;
            getFrameBuff(bufflen);
            ByteBuffer bbuffer = ByteBuffer.wrap(buff);
            bbuffer.get(grayBuff, 0, bufflen);
            gray8toRGB32(grayBuff, width, height, rgbs);
            return Bitmap.createBitmap(rgbs, width, height, Bitmap.Config.RGB_565);
        } else {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            // face detection: first convert the image from NV21 to RGB_565
            YuvImage yuv = new YuvImage(buff, ImageFormat.NV21,
                    bitmap.getWidth(), bitmap.getHeight(), null);
            // TODO: make rect a member and use it for width and height values above
            Rect rectImage = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            // TODO: use a threaded option or a circular buffer for converting streams?
            //see http://ostermiller.org/convert_java_outputstream_inputstream.html
            ByteArrayOutputStream baout = new ByteArrayOutputStream();
            if (!yuv.compressToJpeg(rectImage, 100, baout)) {
                Log.e("CreateBitmap", "compressToJpeg failed");
            }

            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeStream(
                    new ByteArrayInputStream(baout.toByteArray()), null, bfo);
            return bitmap;
        }
    }


    /**
     * @param bitmap 缩放目标bitmap
     * @param scaleW 缩放后的宽
     * @param scaleH 缩放后的高
     * @return 缩放后的bitmap
     */
    public static Bitmap scaleBitmap(Bitmap bitmap, int scaleW, int scaleH) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaleW, scaleH, false);
        return scaledBitmap;
    }


    /**
     * @param b       bitmap
     * @param degrees 旋转角度
     * @return bitmap
     */
    public final static Bitmap rotateBitmap(Bitmap b, float degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2,
                    (float) b.getHeight() / 2);

            Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                    b.getHeight(), m, true);
            if (b != b2) {
                b.recycle();
                b = b2;
            }

        }
        return b;
    }


    private static void gray8toRGB32(byte[] gray8, int width, int height, int[] rgb_32s) {
        final int endPtr = width * height;
        int ptr = 0;
        while (true) {
            if (ptr == endPtr)
                break;

            final int Y = gray8[ptr] & 0xff;
            rgb_32s[ptr] = 0xff000000 + (Y << 16) + (Y << 8) + Y;
            ptr++;
        }
    }

}
