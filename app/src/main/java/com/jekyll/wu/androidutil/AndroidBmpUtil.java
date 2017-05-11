package com.jekyll.wu.androidutil;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Android Bitmap Object to .bmp image (Windows BMP v1 1bit) file util class
 * <p>
 * ref : https://github.com/JieJacket/AndroidUtil.git
 *      http://en.wikipedia.org/wiki/BMP_file_format
 *
 * @since 2017-05-11
 */
public class AndroidBmpUtil {

    private static final int BMP_WIDTH_OF_TIMES = 4;

    /**
     * Android Bitmap Object to Window's v11bit Bmp Format File
     *
     * @param orgBitmap
     * @return file saved result
     */
    public static byte[] convert(@NonNull Bitmap orgBitmap) throws IOException {

        //image size
        int width = orgBitmap.getWidth();
        int height = orgBitmap.getHeight();

        //image dummy data size
        //reason : the amount of bytes per image row must be a multiple of 4 (requirements of bmp format)
        byte[] dummyBytesPerRow = null;//每行需要补充的字节
        boolean hasDummy = false;

        int rowWidthInBytes = width % 8 > 0 ? width / 8 + 1 : width / 8;//source image width * number of bytes to encode one pixel.
        if (rowWidthInBytes % BMP_WIDTH_OF_TIMES > 0) {
            hasDummy = true;
            //the number of dummy bytes we need to add on each row
            dummyBytesPerRow = new byte[(BMP_WIDTH_OF_TIMES - (rowWidthInBytes % BMP_WIDTH_OF_TIMES))];
            //just fill an array with the dummy bytes we need to append at the end of each row
            for (int i = 0; i < dummyBytesPerRow.length; i++) {
                dummyBytesPerRow[i] = (byte) 0xFF;
            }
        }

        //an array to receive the pixels from the source image
        int[] pixels = new int[width * height];

        //the number of bytes used in the file to store raw image data (excluding file headers)
        int imageSize = (rowWidthInBytes + (hasDummy ? dummyBytesPerRow.length : 0)) * height;
        //file headers size
        int imageDataOffset = 0x3E;

        //final size of the file
        int fileSize = imageSize + imageDataOffset;

        //Android Bitmap Image Data
        orgBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        //ByteArrayOutputStream baos = new ByteArrayOutputStream(fileSize);
        ByteBuffer buffer = ByteBuffer.allocate(fileSize);

        /**
         * BITMAP FILE HEADER Write Start
         **/
        buffer.put((byte) 0x42);
        buffer.put((byte) 0x4D);

        //size
        buffer.put(writeInt(fileSize));

        //reserved
        buffer.put(writeShort((short) 0));
        buffer.put(writeShort((short) 0));

        //image data start offset
        buffer.put(writeInt(imageDataOffset));

        /** BITMAP FILE HEADER Write End */

        //*******************************************

        /** BITMAP INFO HEADER Write Start */
        //size
        buffer.put(writeInt(0x28));

        //width, height
        //if we add 3 dummy bytes per row : it means we add a pixel (and the image width is modified.
        buffer.put(writeInt(width + (hasDummy ? 8 * dummyBytesPerRow.length : 0)));
        buffer.put(writeInt(height));

        //planes
        buffer.put(writeShort((short) 1));

        //bit count
        buffer.put(writeShort((short) 1));

        //bit compression
        buffer.put(writeInt(0));

        //image data size
        buffer.put(writeInt(imageSize));

        //horizontal resolution in pixels per meter
        buffer.put(writeInt(0));

        //vertical resolution in pixels per meter (unreliable)
        buffer.put(writeInt(0));

        buffer.put(writeInt(0));

        buffer.put(writeInt(0));

        /** BITMAP INFO HEADER Write End */

        /** BITMAP Palette*/

        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);

        buffer.put((byte) 0xFF);
        buffer.put((byte) 0xFF);
        buffer.put((byte) 0xFF);
        buffer.put((byte) 0x00);


        int row = height;
        int col = width;
        int startPosition = (row - 1) * col;
        int endPosition = row * col;
        while (row > 0) {

            StringBuilder sb = new StringBuilder();
            for (int i = startPosition; i < endPosition; i++) {
                int pixel = pixels[i];
                sb.append(pixel == Color.WHITE ? "1" : "0");

                if (i == endPosition - 1 || sb.length() == 8) {

                    buffer.put((byte) (Integer.parseInt(sb.toString(), 2) & 0xff));
                    sb = new StringBuilder();
                }
            }
            if (hasDummy) {
                buffer.put(dummyBytesPerRow);
            }
            row--;
            endPosition = startPosition;
            startPosition = startPosition - col;
        }

        return buffer.array();
    }

    /**
     * Write integer to little-endian
     *
     * @param value
     * @return
     * @throws IOException
     */
    private static byte[] writeInt(int value) throws IOException {
        byte[] b = new byte[4];

        b[0] = (byte) (value & 0x000000FF);
        b[1] = (byte) ((value & 0x0000FF00) >> 8);
        b[2] = (byte) ((value & 0x00FF0000) >> 16);
        b[3] = (byte) ((value & 0xFF000000) >> 24);

        return b;
    }

    /**
     * Write short to little-endian byte array
     *
     * @param value
     * @return
     * @throws IOException
     */
    private static byte[] writeShort(short value) throws IOException {
        byte[] b = new byte[2];

        b[0] = (byte) (value & 0x00FF);
        b[1] = (byte) ((value & 0xFF00) >> 8);

        return b;
    }
}