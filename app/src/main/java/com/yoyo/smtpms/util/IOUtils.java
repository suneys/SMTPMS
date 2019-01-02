package com.yoyo.smtpms.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Administrator on 2016-06-25.
 */
public class IOUtils {

    /**
     * 向文件写入数据
     *
     * @param path
     * @param content
     * @throws IOException
     */
    public static void writeToFile(String path, String content,boolean append) throws IOException {
        File file = new File(path);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file,append);
        fw.write(content);
        fw.flush();
        fw.close();
    }

    /**
     * 读取小文件
     *
     * @param path
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    static public String readFormFile(String path, String code)
            throws FileNotFoundException, IOException {
        String result = "";
        File file = new File(path);
        if (file.exists()) {
            //FileReader fr = new FileReader(file);
            FileInputStream is = new FileInputStream(file);
            //char[] bb = new char[1024]; // 用来保存每次读取到的字符
            byte[] buffer = new byte[1024];
            int n;// 每次读取到的字符长度
            while ((n = is.read(buffer)) != -1) {
                result += new String(buffer, 0, n, code);
            }
            is.close();
        }
        return result;
    }

    /**
     * 实现文件的拷贝
     * @param srcPathStr
     *          源文件的地址信息
     * @param desPathStr
     *          目标文件的地址信息
     */
    public static void copyFile(String srcPathStr, String desPathStr) {

        try{
            //2.创建输入输出流对象
            FileInputStream fis = new FileInputStream(srcPathStr);
            FileOutputStream fos = new FileOutputStream(desPathStr);

            //创建搬运工具
            byte datas[] = new byte[1024*8];
            //创建长度
            int len = 0;
            //循环读取数据
            while((len = fis.read(datas))!=-1){
                fos.write(datas,0,len);
            }
            //3.释放资源
            fis.close();
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据指定路径判断文件是否存在
     *
     * @param path
     * @return
     */
    public static Boolean fileIsExist(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public static Bitmap decoderFileAsBitmap(File file){
        if(file == null) return null;

        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    /**
     * 删除指定路径下的文件
     *
     * @param path
     */
    public static boolean deleteFoder(String path) {
        File file = new File(path);
        boolean isSuccess = true;
        // 判断文件是否存在
        if (file.exists()) {
            // 判断是否是文件
            if (file.isFile()) {
                isSuccess = file.delete();
                // 否则如果它是一个目录
            } else if (file.isDirectory()) {
                // 声明目录下所有的文件 files[];
                File files[] = file.listFiles();
                if (files != null) {
                    if(files.length > 0) {
                        // 遍历目录下所有的文件
                        for (int i = 0; i < files.length; i++) {
                            // 把每个文件
                            // 用这个方法进行迭代
                            deleteFoder(files[i].getAbsolutePath());
                        }
                    }else{
                        file.delete();
                    }
                }
            }
            if(file.exists()) {
                isSuccess = file.delete();
            }
            if (!isSuccess) {
                return false;
            }
        }
        return true;
    }

    public static void saveBitmap(Bitmap bitmap,String fileName){
        File file = new File(fileName);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdir();
        }
        if (!file.exists()){
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
