package com.use.demo.util;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    public static void packageZip(String filesPath, String zipPath) throws Exception {
        // 要被压缩的文件夹
        File file = new File(filesPath);   //需要压缩的文件夹
        File zipFile = new File(zipPath);  //放于和需要压缩的文件夹同级目录
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
        isDirectory(file, zipOut, "", true);   //判断是否为文件夹
        zipOut.close();
    }

    public static void isDirectory(File file, ZipOutputStream zipOutputStream, String filePath, boolean flag) throws IOException {
        //判断是否为问加减
        if (file.isDirectory()) {
            File[] files = file.listFiles();  //获取该文件夹下所有文件(包含文件夹)
            filePath = flag == true ? file.getName() : filePath + File.separator + file.getName();   //首次为选中的文件夹，即根目录，之后递归实现拼接目录
            for (int i = 0; i < files.length; ++i) {
                //判断子文件是否为文件夹
                if (files[i].isDirectory()) {
                    //进入递归,flag置false 即当前文件夹下仍包含文件夹
                    isDirectory(files[i], zipOutputStream, filePath, false);
                } else {
                    //不为文件夹则进行压缩
                    InputStream input = new FileInputStream(files[i]);
                    zipOutputStream.putNextEntry(new ZipEntry(filePath + File.separator + files[i].getName()));
                    int temp = 0;
                    while ((temp = input.read()) != -1) {
                        zipOutputStream.write(temp);
                    }
                    input.close();
                }
            }
        } else {
            //将子文件夹下的文件进行压缩
            InputStream input = new FileInputStream(file);
            zipOutputStream.putNextEntry(new ZipEntry(file.getPath()));
            int temp = 0;
            while ((temp = input.read()) != -1) {
                zipOutputStream.write(temp);
            }
            input.close();
        }
    }

    /**
     * zip解压
     *
     * @param srcFile     zip源文件
     * @param destDirPath 解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static List<String> unZip(File srcFile, String destDirPath) throws RuntimeException {

        //记录解压出来的所有文件名
        List<String> filesName = new ArrayList<>();
        long start = System.currentTimeMillis();
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "所指文件不存在");
        }
        // 开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(srcFile, Charset.forName("GBK"));
            Enumeration<?> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                //    System.out.println("解压文件:" + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + "/" + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    //添加进filesName
                    filesName.add(entry.getName());
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(destDirPath + "/" + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("解压完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filesName;
    }

    public static boolean isModel(MultipartFile file) {
        if (!file.getOriginalFilename().equals("infer_model.zip")) {
            return false;
        }
        try {
            FileInputStream input = (FileInputStream) file.getInputStream();
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(input), Charset.forName("GBK"));
            ZipEntry ze = zipInputStream.getNextEntry();
            try {
                if (!(ze.isDirectory() && ze.getName().equals("infer_model/"))) {
                    return false;
                }
            }finally {
                zipInputStream.closeEntry();
                input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    public static String getSize(long size){
        if (size/1024<1){
            return size+"B";
        }else if (size/1024/1024<1){
            return String.format("%.1fKB",(float)size/1024);
        }else if (size/1024/1024/1024<1){
            return String.format("%.1fMB",(float)size/1024/1024);
        }else{
            return String.format("%.1fGB",(float)size/1024/1024/1024);
        }
    }
}