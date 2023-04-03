package com.use.demo.controller;

import com.use.demo.data.DataSet;
import com.use.demo.data.ZipData;
import com.use.demo.util.Result;
import com.use.demo.util.ZipUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
    静态文件存放在项目根目录
    格式如resources所示
    静态文件夹 static与src同级目录
*/
@Controller
public class PageControl {
    //静态文件存放网络地址，方便进行由客户端主动发起的文件下载事件
    final static String WEBURL = "https://fireinsect.top/test/static/";
    /*
        静态文件存放本地地址，方便进行本地文件操作
        文件操作为本地化操作，导致静态文件不能存放在项目资源文件内，本地地址需要另外表示
    * */
    final static String LocalURL = "static/";
    @GetMapping("/wenye/data/list")
    @ResponseBody
    public Result<List<DataSet>> getDataList() {
        Result result = new Result();
        List<DataSet> dataSets = new ArrayList<>();
        String baseUrl = LocalURL + "dataset/";
        File dir = new File(baseUrl);
        System.out.println(dir.getAbsolutePath());
        String[] dirClass1 = dir.list();
        System.out.println(dirClass1.length);
        for (String filestr : dirClass1) {
            File file=new File(dir.getAbsolutePath()+"/"+filestr);
            String[] dirClass2 = file.list();
            String category = file.getName();
            System.out.println(file.getName());
            if (dirClass2 != null) {
                for (String fstr : dirClass2) {
                    File f=new File(file.getAbsolutePath()+"/"+fstr);
                    System.out.println(f.getName());
                    if (new File(LocalURL + "zip/" + f.getName() + ".zip") != null) {
                        DataSet dataSet = new DataSet();
                        dataSet.setCategory(category);
                        dataSet.setName(f.getName());
                        dataSet.setUrl(WEBURL + "zip/"  + f.getName() + ".zip");
                        dataSet.setNums(f.list().length);
                        System.out.println("=========");
                        dataSets.add(dataSet);
                    } else {
                        File file1 = new File(LocalURL+"zip/" + f.getName() + ".zip");
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    file1.createNewFile();
                                    System.out.println("压缩" + file1.getName());
                                    ZipUtil.packageZip(f.getAbsolutePath(), file1.getAbsolutePath());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                super.run();
                            }
                        }.start();
                    }
                }
            }
        }
        DataSet noClass = new DataSet();
        noClass.setUrl(WEBURL + "zip/Noclass.zip");
        noClass.setCategory("Noclass");
        noClass.setName("Noclass");
        noClass.setNums(new File(LocalURL+"pics").list().length);
        dataSets.add(noClass);
        result.setData(dataSets);
        result.setCode(20000);
        result.setMessage("success");
        result.setSuccess(true);
        return result;
    }
//    @GetMapping("/wenye/data/list")
//    @ResponseBody
//    public Result<List<DataSet>> getDataList() {
//        Result result = new Result();
//        List<DataSet> dataSets = new ArrayList<>();
//        String baseUrl = LocalURL + "dataset/";
//        File dir = new File(baseUrl);
//        System.out.println(dir.getAbsolutePath());
//        File[] dirClass1 = dir.listFiles();
//        for (File file : dirClass1) {
//            File[] dirClass2 = file.listFiles();
//            String category = file.getName();
//            System.out.println(file.getName());
//            if (dirClass2 != null) {
//                for (File f : dirClass2) {
//                    if (new File(LocalURL + "zip/" + f.getName() + ".zip") != null) {
//                        DataSet dataSet = new DataSet();
//                        dataSet.setCategory(category);
//                        dataSet.setName(f.getName());
//                        dataSet.setUrl(WEBURL + category + "/" + f.getName() + "/");
//                        dataSet.setNums(f.list().length);
//                        dataSets.add(dataSet);
//                    } else {
//                        File file1 = new File(LocalURL+"zip/" + f.getName() + ".zip");
//                        new Thread() {
//                            @Override
//                            public void run() {
//                                try {
//                                    file1.createNewFile();
//                                    System.out.println("压缩" + file1.getName());
//                                    ZipUtil.packageZip(f.getAbsolutePath(), file1.getAbsolutePath());
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                                super.run();
//                            }
//                        }.start();
//                    }
//                }
//            }
//        }
//        DataSet noClass = new DataSet();
//        noClass.setUrl(WEBURL + "pics");
//        noClass.setCategory("Noclass");
//        noClass.setName("Noclass");
//        noClass.setNums(new File(noClass.getUrl()).list().length);
//        dataSets.add(noClass);
//        result.setData(dataSets);
//        result.setCode(20000);
//        result.setMessage("success");
//        result.setSuccess(true);
//        return result;
//    }

    @PostMapping("/wenye/zip/upload")
    @ResponseBody
    public Result zipUpload(MultipartFile file) {
        Result result = new Result();
        if (!ZipUtil.isModel(file)) {
            result.setMessage("不是模型文件");
            result.setSuccess(false);
            result.setCode(50009);
            return result;
        }
        try {
            File file1 = new File(LocalURL + "infer_model.zip");
            if (!file1.exists()) {
                file1.createNewFile();
            }
            OutputStream outputStream=new FileOutputStream(file1);
            InputStream inputStream=file.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,len);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            System.out.println(file1.length());
            //解压模型文件，暂时不要
//            ZipUtil.unZip(file1,LocalURL);
        } catch (IOException e) {
            e.printStackTrace();
            result.setMessage("服务器错误");
            result.setSuccess(false);
            result.setCode(40009);
            return result;
        }
        result.setMessage("success");
        result.setSuccess(true);
        result.setCode(20000);
        return result;
    }

    @GetMapping("/wenye/zip/get")
    @ResponseBody
    public Result<List<ZipData>> zipGet(){
        Result<List<ZipData>> result=new Result<>();
        List<ZipData> list=new ArrayList<>();
        ZipData zipData=new ZipData();
        File file=new File(LocalURL+"infer_model.zip");
        if (file.exists()){
            long time=file.lastModified();
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String newtime = sdf.format(date);
            zipData.setDate(newtime);
            long size=file.length();
            zipData.setSize(ZipUtil.getSize(size));
            list.add(zipData);
            result.setData(list);
        }
        result.setMessage("success");
        result.setCode(20000);
        return result;
    }
//    @PostMapping("/wenye/data/get")
//    @ResponseBody
//    public Result<List<String>> getDataZip(@RequestBody Map<String, List<DataSet>> data) {
//        System.out.println(data.get("query"));
//        List<DataSet> dataSets = data.get("query");
//        Result<List<String>> result=new Result<>();
//        List<String> datas=new ArrayList<>();
//        for (DataSet da : dataSets) {
//            OutputStream outputStream = null;
//
//            if (!file1.exists()) {
//
//            } else {
//                datas.add(file1.getName());
//            }
//        }
//        result.setData(datas);
//        result.setCode(20000);
//        result.setMessage("success");
//        result.setSuccess(true);
//        return result;
//    }
//
//    @RequestMapping("/wenye/data/download/{file}")
//    @ResponseBody
//    public Result downloadFile(@PathVariable("file") String fileName, HttpServletRequest request,HttpServletResponse response) throws Exception {
//        System.out.println(fileName);
//        String filePath="static/zip/"+fileName;
//        File file = new File(filePath);
//        Result result=new Result();
//        byte[] buffer = new byte[1024];
//        BufferedInputStream bis = null;
//        OutputStream os = null;
//        try {
//            //文件是否存在
//            if (file.exists()) {
//                //设置响应
//                response.setContentType("application/octet-stream;charset=UTF-8");
//                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
//                response.setHeader("Content-Disposition","attachment;filename="+fileName);
//                response.setCharacterEncoding("UTF-8");
//                os = response.getOutputStream();
//                bis = new BufferedInputStream(new FileInputStream(file));
//                while(bis.read(buffer) != -1){
//                    os.write(buffer);
//                }
//            }
//        }catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if(bis != null) {
//                    bis.close();
//                }
//                if(os != null) {
//                    os.flush();
//                    os.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        result.setMessage("success");
//        result.setCode(20000);
//        return result;
//    }
}
