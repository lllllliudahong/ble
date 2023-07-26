package com.czw.bluetoothlib.core.jl.util;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntRange;

import com.jieli.bluetooth_connect.constant.BluetoothConstant;
import com.jieli.jl_rcsp.constant.StateCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author zqjasonZhong
 * @since 2021/3/9
 */
public class HealthUtil {

    public static String createTestOtaFiles(Context context) {
        String SD_PATH = getPath(context)
                + File.separator + "TestOta";
        File sd_file = new File(SD_PATH);
        if (null != sd_file && !sd_file.exists()) {
            sd_file.mkdirs();
        }
        return SD_PATH;
    }

    public static String getOtaFilePath(Context context) {
        return createTestOtaFiles(context) + File.separator + "update.ufw";

    }

    public static String getPath(Context context) {
        File dir = null;
        boolean state = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (state) {
            if (Build.VERSION.SDK_INT >= 29) {
                //Android10之后
                //dir = context.getExternalFilesDir(null);
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                        || !Environment.isExternalStorageRemovable()) {
                    //外部存储可用
                    dir = context.getExternalCacheDir();
                    return dir.getPath();
                } else {
                    //外部存储不可用
                    dir = context.getCacheDir();
                    return dir.getPath();
                }
            } else {
                dir = Environment.getExternalStorageDirectory();
            }
        } else {
            dir = Environment.getRootDirectory();
        }
        return dir.toString();
    }


    /**
     * 创建文件路径
     *
     * @param context  上下文
     * @param dirNames 文件夹名
     * @return 路径
     */
    public static String createFilePath(Context context, String... dirNames) {
        if (context == null || dirNames == null || dirNames.length == 0) return null;
        File file = context.getExternalFilesDir(null);
        if (file == null || !file.exists()) return null;
        StringBuilder filePath = new StringBuilder(file.getPath());
        if (filePath.toString().endsWith("/")) {
            filePath = new StringBuilder(filePath.substring(0, filePath.lastIndexOf("/")));
        }
        for (String dirName : dirNames) {
            filePath.append("/").append(dirName);
            file = new File(filePath.toString());
            if (!file.exists() || file.isFile()) {//文件不存在
                if (!file.mkdir()) {
                    Log.w("jieli", "create dir failed. filePath = " + filePath);
                    break;
                }
            }
        }
        return filePath.toString();
    }

    /**
     * 获取指定文件类型的路径
     *
     * @param dirPath 目录路径
     * @param suffix  文件后续
     * @return 文件路径
     */
    public static String obtainUpdateFilePath(String dirPath, String suffix) {
        if (null == dirPath) return null;
        File dir = new File(dirPath);
        if (!dir.exists()) return null;
        if (dir.isFile()) {
            if (dirPath.endsWith(suffix)) {
                return dirPath;
            } else {
                return null;
            }
        } else if (dir.isDirectory()) {
            String filePath = null;
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    filePath = obtainUpdateFilePath(file.getPath(), suffix);
                    if (filePath != null) {
                        break;
                    }
                }
            }
            return filePath;
        }
        return null;
    }

    /**
     * 获取设备名称
     *
     * @param device 蓝牙设备
     * @return 设备名
     */
    public static String getDeviceName(BluetoothDevice device) {
        if (null == device) return null;
        String name = device.getName();
        if (null == name) {
            name = device.getAddress();
        }
        return name;
    }

    /**
     * 转换成手表的连接状态
     *
     * @param status 系统连接状态
     * @return 手表连接状态
     */
    public static int convertWatchConnectStatus(int status) {
        int newStatus;
        switch (status) {
            case BluetoothConstant.CONNECT_STATE_CONNECTING:
                newStatus = StateCode.CONNECTION_CONNECTING;
                break;
            case BluetoothConstant.CONNECT_STATE_CONNECTED:
                newStatus = StateCode.CONNECTION_OK;
                break;
            default:
                newStatus = StateCode.CONNECTION_DISCONNECT;
                break;
        }
        return newStatus;
    }

    /**
     * 转换成OTA的连接状态
     *
     * @param status 系统连接状态
     * @return OTA连接状态
     */
    public static int convertOtaConnectStatus(int status) {
        int newStatus;
        switch (status) {
            case BluetoothConstant.CONNECT_STATE_CONNECTING:
                newStatus = StateCode.CONNECTION_CONNECTING;
                break;
            case BluetoothConstant.CONNECT_STATE_CONNECTED:
                newStatus = StateCode.CONNECTION_OK;
                break;
            default:
                newStatus = StateCode.CONNECTION_DISCONNECT;
                break;
        }
        return newStatus;
    }

    /**
     * 获取文件名
     *
     * @param filePath 文件路径
     * @return 文件名
     */
    public static String getFileNameByPath(String filePath) {
        if (filePath == null) return null;
        int index = filePath.lastIndexOf("/");
        if (index > -1) {
            return filePath.substring(index + 1);
        } else {
            return filePath;
        }
    }

    /**
     * 创建指定尺寸图像
     *
     * @param path         图像路径
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return 图像
     */
    public static Bitmap createScaleBitmap(String path, int targetWidth, int targetHeight) {
        Bitmap bmpSrc = BitmapFactory.decodeFile(path);
        if (bmpSrc == null) return null;
        int srcWidth = bmpSrc.getWidth();
        int srcHeight = bmpSrc.getHeight();
        if (srcWidth == targetWidth && srcHeight == targetHeight) {
            return bmpSrc;
        }
        float widthScale = targetWidth * 1.0f / srcWidth;
        float heightScale = targetHeight * 1.0f / srcHeight;
        float scale = Math.min(widthScale, heightScale);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale, 0, 0);
        Bitmap bmpRet = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmpRet);
        // 如需要可自行设置 Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG 等等
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        canvas.drawBitmap(bmpSrc, matrix, paint);
        return bmpRet;
    }

    /**
     * 保存缩放图像
     *
     * @param path         源图像路径
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @param quality      压缩比例
     * @return 输出图像路径
     */
    public static String saveScaleBitmap(String path, int targetWidth, int targetHeight, @IntRange(from = 0, to = 100) int quality) {
        Bitmap bitmap = createScaleBitmap(path, targetWidth, targetHeight);
        if (null == bitmap) return null;
        boolean ret = bitmapToFile(bitmap, path, quality);
        return ret ? path : null;
    }

    public static boolean bitmapToFile(Bitmap bitmap, String outputPath, int quality) {
        FileOutputStream outStream = null;
        boolean result = false;
        if (bitmap != null && !TextUtils.isEmpty(outputPath)) {
            try {
                outStream = new FileOutputStream(outputPath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
                result = true;
            } catch (IOException var14) {
                var14.printStackTrace();
            } finally {
                if (outStream != null) {
                    try {
                        outStream.flush();
                        outStream.close();
                    } catch (IOException var13) {
                        var13.printStackTrace();
                    }
                }

            }
        }

        return result;
    }
}

