package com.jekyll.wu.androidutil;

import android.graphics.Bitmap;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jie on 16/7/25.
 */
public class AndroidPbmUtil {
    private static final String TAG = AndroidPbmUtil.class.getSimpleName();
    /**
     * bitmap保存为Pbm P1格式文件
     *
     * @param path
     * @param bitmap
     * @return
     * @throws IOException
     */
//    public boolean convertP1File(String path, Bitmap bitmap) throws IOException {
//        if (bitmap == null) {
//            return false;
//        }
//        String imageText = getImageTextData(bitmap);
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//
//        File file = new File(path);
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//        FileOutputStream fos = new FileOutputStream(file);
//        String header = String.format(Locale.getDefault(), "%s%n%d%n%d%n", "P1", width, height);
//        fos.write(header.getBytes(Charset.defaultCharset()));
//        fos.write(imageText.getBytes(Charset.defaultCharset()));
//        fos.flush();
//        fos.close();
//        return true;
//    }

    /**
     * bitmap转Pbm P4格式文件
     *
     * @param path
     * @param bitmap
     * @return
     * @throws IOException
     */
    public boolean convertP4File(String path, Bitmap bitmap) {
        if (bitmap == null) {
            return false;
        }
        String imageText = getImageTextData(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        String header = String.format(Locale.getDefault(), "%s%n%d%n%d%n", "P4", width, height);
        FileOutputStream fos = null;
        try {
            File file = new File(path);
            boolean newFile = false;
            if (!file.exists()) {
                newFile = file.createNewFile();
            }
            if (newFile) {
                fos = new FileOutputStream(file);
                byte[] imageBinary = convertARGB_88882PBM(imageText, width, height);
                fos.write(header.getBytes(Charset.defaultCharset()));
                fos.write(imageBinary);
                fos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fos)
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return true;
    }

    /**
     * 从bitmap中获取图片数据
     *
     * @param bitmap
     * @return
     */
    public String getImageTextData(Bitmap bitmap) {
        int count = byteSizeOf(bitmap);
        ByteBuffer byteBuffer = ByteBuffer.allocate(count);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] array = byteBuffer.array();
        if (array.length <= 0 || array.length < 4) {
            return new String(array, Charset.defaultCharset());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length / 4; i++) {
            byte b0 = array[i * 4];
            byte b1 = array[i * 4 + 1];
            byte b2 = array[i * 4 + 2];
            if ((b0 & b1 & b2) == -1) {
                sb.append("0");
            } else {
                sb.append("1");
            }
        }
        return sb.toString();
    }

    /**
     * 计算图片大小
     *
     * @param data
     * @return
     */
    private int byteSizeOf(Bitmap data) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            return data.getRowBytes() * data.getHeight();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return data.getByteCount();
        } else {
            return data.getAllocationByteCount();
        }
    }

    /**
     * 将ARGB_8888格式的图片转为Pbm P4文件需要的byte[]
     *
     * @param array
     * @return
     */
    public byte[] convertARGB_88882PBM(String array, int width, int height) {
        List<String> lists = new LinkedList<>();
        for (int i = 0; i < height; i++) {
            lists.add(array.substring(i * width, (i + 1) * width));
        }

        List<String> result = new LinkedList<>();
        for (String s : lists) {
            result.addAll(convert2binaryArrays(s));
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(result.size());
        for (String s : result) {
            byteBuffer.put(handleByte(s));
        }

        return byteBuffer.array();
    }

    /**
     * 字符串转位二进制
     *
     * @param s
     * @return
     */
    private byte handleByte(String s) {
        return (byte) (Integer.parseInt(s, 2) & 0xff);
    }

    /**
     * 普通字符串划分为位二进制字符串
     *
     * @param data
     * @return
     */
    private List<String> convert2binaryArrays(String data) {

        List<String> result = new LinkedList<>();
        int remainder = data.length() % 8;
        int length = data.length() / 8;
        for (int i = 0; i < length; i++) {
            int start = i * 8;
            int end = (i + 1) * 8;
            result.add(data.substring(start, end));
        }

        if (remainder != 0) {
            String finalData = data.substring(data.length() - remainder, data.length());
            StringBuilder sb = new StringBuilder();
            sb.append(finalData);
            for (int i = 0; i < 8 - finalData.length(); i++) {
                sb.insert(0, "0");
            }
            result.add(sb.toString());
        }
        return result;

    }

}
