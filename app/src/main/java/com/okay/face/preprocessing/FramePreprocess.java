package com.okay.face.preprocessing;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import com.okay.face.utils.ImageUtils;

import java.io.IOException;
import java.nio.ByteBuffer;


public class FramePreprocess {
    // 规范进入的Frame
    // 规范预处理后的Frame
    private static String TAG = "Frame Process - ";

    /**
     * @param buff   帧数据
     * @param width  帧宽
     * @param height 帧高
     * @return 3通道的bitmap
     */
    public static Bitmap craeteBitmap(byte[] buff, int width, int height) {
        int bufflen = width * height;
        byte[] grayBuff = new byte[bufflen];
        int[] rgbs = new int[bufflen];

        ByteBuffer bbuffer = ByteBuffer.wrap(buff);
        bbuffer.get(grayBuff, 0, bufflen);
        gray8toRGB32(grayBuff, width, height, rgbs);
        Bitmap bitmap = Bitmap.createBitmap(rgbs, width, height, Bitmap.Config.RGB_565);
        return bitmap;
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
        long t_start = System.currentTimeMillis();
        final int endPtr = width * height;
        int ptr = 0;
        while (true) {
            if (ptr == endPtr)
                break;

            final int Y = gray8[ptr] & 0xff;
            rgb_32s[ptr] = 0xff000000 + (Y << 16) + (Y << 8) + Y;
            ptr++;
        }
        Log.i(TAG, "Gray8 to RGB32 Cost Time:" + (System.currentTimeMillis() - t_start) + "ms");
    }
}
