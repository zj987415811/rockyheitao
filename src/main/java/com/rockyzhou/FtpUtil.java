package com.rockyzhou;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.Date;

/**
 * Created by leboop on 2018/8/28.
 */
public class FtpUtil {
    //服务器ip地址
    private static String url = "47.105.107.154";
    //FTP端口，默认是21（注意：SSH默认22）
    private static int port = 21;
    //登录用户名
    private static String username = "test";
    //登录密码
    private static String password = "12345678";

    public static void main(String[] args) {
        System.out.println(upload());
    }
    //将字符串写入文件上传至服务器
    public static boolean upload() {
        //写入文件的字符串
        String cityStr = "Hello World";

        boolean success = false;
        FTPClient ftp = new FTPClient();
        try {
            int reply;

            ftp.setControlEncoding("UTF-8");
            //连接FTP服务器
            ftp.connect(url, port);
            //如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
            ftp.login(username, password);
            ftp.login(username, password);
            //如果reply的值是230,表示连接成功，530可能是用户名或者密码错误
            //500可能是/home/test目录不存在
            reply = ftp.getReplyCode();
            System.out.println(reply);

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                return success;
            }
            long date = System.currentTimeMillis();
            String filename = date + "ftp.csv";
            byte[] cBytes= cityStr.getBytes();
            //字符串转换为字节数组
            InputStream input = new ByteArrayInputStream(cBytes);

            ftp.enterLocalPassiveMode();
            //需要添加这行代码，不然上传的文件为空
            ftp.setFileType(FTP.BINARY_FILE_TYPE);

            boolean g = ftp.storeFile(filename, input);

            System.out.println("上传服务器：" + g);
            input.close();
            ftp.logout();
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException ioe) {
                }
            }
        }
        return success;
    }

}
