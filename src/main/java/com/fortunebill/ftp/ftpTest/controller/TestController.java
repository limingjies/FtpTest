package com.fortunebill.ftp.ftpTest.controller;

import com.fortunebill.ftp.ftpTest.controller.util.ftp.FtpClientTemplate;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * @ClassName TestController
 * @Author lmj
 * @Date 2021/6/10
 * @Version 1.0
 */
@RestController
public class TestController {

    @Autowired
    private FtpClientTemplate ftpClientTemplate;

    @RequestMapping("/ftp.do")
    public String testFtp(){

        File file = new File("E:\\rishiqing_v3.2.4.zip");

        try {
            File destFile = new File("E:\\" + new Random().nextInt(999999) + ".zip");
            FileUtils.copyFile(file, destFile);
            ftpClientTemplate.uploadFile(destFile, "/home/pospmng/nas20/FB_TMP");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "hahahh";

    }
}
