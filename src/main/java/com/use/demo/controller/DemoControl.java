package com.use.demo.controller;

import com.use.demo.data.IndifyResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Controller
public class DemoControl {
    @PostMapping("/getResult")
    @ResponseBody
    public List<IndifyResult> getPicRes(@RequestParam("pics") MultipartFile image) throws IOException {
        List<IndifyResult> result=new ArrayList<>();
        String imgName = UUID.randomUUID().toString().replace("_", "") + "_" + image.getOriginalFilename().replaceAll(" ", "").replaceAll("：","");
        //保存图片
        String imgFilePath="./static/pics/"+imgName;
        OutputStream out = new FileOutputStream(imgFilePath);
        out.write(image.getBytes());
        out.flush();
        out.close();
        Process proc;
        try {
            System.out.println("start");
            System.out.println(imgFilePath);
            proc = Runtime.getRuntime().exec("python3 ./static/use.py --path="+imgFilePath);// 执行py文件

            //用输入输出流来截取结果
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                IndifyResult indifyResult=new IndifyResult();

                indifyResult.setAccuracy(Float.parseFloat(line.split("：",3)[2]));
                indifyResult.setTag(Integer.parseInt(line.split("：",3)[0]));
                indifyResult.setDisease(line.split("：",3)[1]);
                result.add(indifyResult);
            }
            in.close();
            proc.waitFor();
            System.out.println("end");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
    @GetMapping("/test")
    @ResponseBody
    public String test(){
        return "success";
    }
}
