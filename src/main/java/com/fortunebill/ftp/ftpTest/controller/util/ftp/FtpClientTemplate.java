package com.fortunebill.ftp.ftpTest.controller.util.ftp;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @description:
 * @title: 实现文件上传下载
 * @author: lrxc
 * @date: 2019/11/19 14:09
 */
@Component
public class FtpClientTemplate {

    private final static Logger log = Logger.getLogger(FtpClientTemplate.class.getName());

    @Autowired
    private FtpClientPool ftpClientPool;

    /***
     * @Author lmj
     * @Description
     * @Date 14:23 2021/5/31
     * @Param [file, newFileName, folder]
     * @return boolean
    **/
    public boolean uploadFile(File localFile, String newFileName, String remotePath){

        log.info("上传开始");
        FTPClient ftpClient = null;
        InputStream inStream = null;
        try {
            //从池中获取对象
            ftpClient = ftpClientPool.borrowObject();

            if(ftpClient == null){
                log.info("获取ftp链接失败");
                return false;
            }
            // 验证FTP服务器是否登录成功
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.warn("FTP服务器校验失败, 上传replyCode:{}" + replyCode+"   "+localFile);
                return false;
            }
            log.info("获取ftp连接成功");
            //切换到上传目录
            if (!ftpClient.changeWorkingDirectory(new String(remotePath.getBytes("ISO-8859-1")))) {
                //如果目录不存在创建目录
                String[] dirs = remotePath.split("/");
                String tempPath = "";

                for (String dir : dirs) {

                    if (null == dir || "".equals(dir)){
                        continue;
                    }
                    tempPath += "/" + dir;

                    if (!ftpClient.changeWorkingDirectory(new String(tempPath.getBytes("ISO-8859-1")))) {
                        if (!ftpClient.makeDirectory(new String(tempPath.getBytes("ISO-8859-1")))) {
                            return false;
                        } else {
                            ftpClient.changeWorkingDirectory(new String(tempPath.getBytes("ISO-8859-1")));
                        }
                    }
                }
            }
            log.info("ftp切换目录成功");
            inStream = new FileInputStream(localFile);
            //设置上传文件的类型为二进制类型

            //尝试上传三次
            for (int j = 0; j < 3; j++) {
                //避免进度回调过于频繁
                final int[] temp = {0};
                //上传进度监控
               /* ftpClient.setCopyStreamListener(new CopyStreamAdapter() {
                    @Override
                    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                        int percent = (int) (totalBytesTransferred * 100 / localFile.length());
                        if (temp[0] < percent) {
                            temp[0] = percent;
                            log.info("↑↑   上传进度    " + percent + "     " + localFile.getAbsolutePath());
                        }
                    }
                });*/

                boolean success = ftpClient.storeFile(new String(newFileName.getBytes("iso-8859-1")), inStream);
                if (success) {
                    log.info("文件上传成功! " + localFile.getName());
                    return true;
                }
                log.info("文件上传失败" + localFile.getName() + "  重试 " + j);
            }
            log.info("文件上传多次仍失败" + localFile.getName());
        } catch (Exception e) {
            try {
                e.printStackTrace();
                log.error("文件上传错误! " + localFile.getName(), e);
                ftpClient.disconnect();
            } catch (IOException ex) {
                log.error("ftp disconnect error");
                ex.printStackTrace();
            }
        } finally {
            IOUtils.closeQuietly(inStream);
            ftpClientPool.returnObject(ftpClient);
        }
        return false;
    }

    /***
     * 上传Ftp文件
     *
     * @param localFile 本地文件路径
     * @param remotePath 上传服务器路径
     * @return true or false
     */
    public boolean uploadFile(File localFile, String remotePath) {

        return uploadFile(localFile, localFile.getName(), remotePath);

    }

    /**
     * 下载文件
     *
     * @param remotePath FTP服务器文件目录
     * @param fileName   需要下载的文件名称
     * @param localPath  下载后的文件路径
     * @return true or false
     */
    public boolean downloadFile(String remotePath, String fileName, String localPath) {
        FTPClient ftpClient = null;
        OutputStream outputStream = null;
        try {
            ftpClient = ftpClientPool.borrowObject();
            // 验证FTP服务器是否登录成功
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.warn("FTP服务器校验失败, 下载replyCode:{}" + replyCode + "  " + localPath + "/" + fileName);
                return false;
            }

            // 切换FTP目录
            ftpClient.changeWorkingDirectory(remotePath);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile file : ftpFiles) {
                if (fileName.equalsIgnoreCase(file.getName())) {
                    //保存至本地路径
                    File localFile = new File(localPath + "/" + file.getName());
                    //创建父级目录
                    if (!localFile.getParentFile().exists()) {
                        localFile.getParentFile().mkdirs();
                    }

                    //尝试下载三次
                    for (int i = 0; i < 3; i++) {
                        //避免进度回调过于频繁
                        final int[] temp = {0};
                        //下载进度监控
                       /* ftpClient.setCopyStreamListener(new CopyStreamAdapter() {
                            @Override
                            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                                int percent = (int) (totalBytesTransferred * 100 / file.getSize());
                                if (temp[0] < percent) {
                                    temp[0] = percent;
                                    log.info("  ↓↓ 下载进度    " + percent + "     " + localFile.getAbsolutePath());
                                }
                            }
                        });*/

                        outputStream = new FileOutputStream(localFile);
                        boolean success = ftpClient.retrieveFile(file.getName(), outputStream);
                        outputStream.flush();
                        if (success) {
                            log.info("文件下载成功! " + localFile.getName());
                            return true;
                        }
                        log.info("文件下载失败" + localFile.getName() + "  重试 " + i);
                    }
                    log.info("文件下载多次仍失败" + localFile.getName());
                }
            }
        } catch (Exception e) {
            log.error("文件下载错误! " + remotePath + "/" + fileName, e);
        } finally {
            IOUtils.closeQuietly(outputStream);
            ftpClientPool.returnObject(ftpClient);
        }
        return false;
    }
    /***
     * @Author lmj
     * @Description
     * @Date 15:59 2021/5/31
     * @Param [remotePath, fileName]
     * @return java.io.InputStream
     **/
    public void downloadFile(String remotePath, String fileName){

        FTPClient ftpClient = null;
        try {
            ftpClient = ftpClientPool.borrowObject();
            // 验证FTP服务器是否登录成功
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.warn("FTP服务器校验失败, 下载replyCode:{}" + replyCode + "  " + fileName);
                return;
            }
            ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
            ftpClient.setBufferSize(100000);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(remotePath);

            InputStream inputStream = ftpClient.retrieveFileStream(remotePath + "/" + fileName);
            File file = new File("E:\\" + fileName );
            FileOutputStream outputStream = new FileOutputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] bytes = new byte[1024];
            int i = 0;

            while((i = bufferedInputStream.read(bytes, 0, 1024)) != -1){

                outputStream.write(bytes,0,i);
            }
            inputStream.close();

            outputStream.flush();
            outputStream.close();
            ftpClient.completePendingCommand();


        } catch (Exception e) {
            log.error("文件下载错误! " + remotePath + "/" + fileName, e);
        } finally {

            ftpClientPool.returnObject(ftpClient);
        }
    }
    private static boolean mkDir(String ftpPath,FTPClient ftpClient) {
        if (ftpClient ==null || !ftpClient.isConnected()) {
            return false;
        }
        try {
            // 将路径中的斜杠统一
            char[] chars = ftpPath.toCharArray();
            StringBuffer sbStr = new StringBuffer(256);
            for (int i = 0; i < chars.length; i++) {
                if ('\\' == chars[i]) {
                    sbStr.append('/');
                } else {
                    sbStr.append(chars[i]);
                }
            }
            ftpPath = sbStr.toString();
            if (ftpPath.indexOf('/') == -1) {
                // 只有一层目录
                ftpClient.makeDirectory(new String(ftpPath.getBytes(), "iso-8859-1"));
                ftpClient.changeWorkingDirectory(new String(ftpPath.getBytes(), "iso-8859-1"));
            } else {
                // 多层目录循环创建
                String[] paths = ftpPath.split("/");
                for (int i = 0; i < paths.length; i++) {
                    ftpClient.makeDirectory(new String(paths[i].getBytes(), "iso-8859-1"));
                    ftpClient.changeWorkingDirectory(new String(paths[i].getBytes(), "iso-8859-1"));
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
