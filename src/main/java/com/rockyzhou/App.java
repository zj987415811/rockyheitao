package com.rockyzhou;

import com.alibaba.fastjson.JSON;
import com.aliyuncs.exceptions.ClientException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 * https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic 普通接口
 * https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic 高精度接口
 * https://aip.baidubce.com/rest/2.0/ocr/v1/webimage 网络图片接口
 * https://aip.baidubce.com/rest/2.0/solution/v1/form_ocr/get_request_result 表格文字获取
 * https://aip.baidubce.com/rest/2.0/ocr/v1/numbers 数字识别
 * bbkey:24.8996fe59be0f5a79e390f1a5d202735c.2592000.1545573307.282335-14926692
 * mykey:24.786f8c8b8452108eccc2d3fa32da37c4.2592000.1545382893.282335-14894979
 */
public class App {
    private static int zhuangWinCount = 0;
    private static int xianWinCount = 0;
    private static int heNotComeCount = 0;
    private static int zhuangNotTianWangCount = 0;
    private static int xianNotTianWangCount = 0;
    private static int lianyingtrigeCount = 10;
    private static int tianwangtrigeCount = 13;
    private static int hetrigeCount = 50;
    private static String mykey = "24.786f8c8b8452108eccc2d3fa32da37c4.2592000.1545382893.282335-14894979";
    private static String bbKey = "24.8996fe59be0f5a79e390f1a5d202735c.2592000.1545573307.282335-14926692";
    private static String ypkey = "24.4f34ec42da5ad64c43aaad498e8dc015.2592000.1545909502.282335-14961594";

    private static JmsService jmsService = new JmsService();
    private static int getCount = 0;
    private static int bossTrigeWinOrFailCount = 4;
    private static int bossWinCount = 0;
    private static int bossfailCount = 0;

    public static void main(String[] args) {

        while (true) {
            try {
                String filesPath = "F:\\work\\image";
                File file = new File(filesPath);
                File[] listFiles = file.listFiles();
                if (listFiles.length > 0) {
                    File file1 = new File("F:\\work\\imageWork\\" + listFiles[0].getName());
                    copyFile(listFiles[0], file1);
                    print(file1.getAbsolutePath());
                    file1.delete();
                    File file111 = new File("F:\\work\\imageSave\\" + listFiles[0].getName());
                    copyFile(listFiles[0], file111);
                    listFiles[0].delete();
                    //Thread.sleep(25000);
                }

            } catch (IOException e) {
                System.out.println("Exception");
                continue;
            }
        }

    }

    // 复制文件
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }

            outBuff.flush();
        } finally {
            if (inBuff != null) {
                inBuff.close();
            }
            if (outBuff != null) {
                outBuff.close();
            }
        }
    }


    public static void print(String filePath) {
        try {
            //String filePath = "G:\\image\\Screenshot-000"+num+".jpg";
            String imageEncodeStr = ConvertImageToBase64.getImageStr(filePath);
            String keyWord = URLEncoder.encode(imageEncodeStr, "GBK");
            String keyurl = "24.786f8c8b8452108eccc2d3fa32da37c4.2592000.1545382893.282335-14894979";
//            if(getCount < 490 && !keyurl.equals(ypkey)) {
//                keyurl = ypkey;
//            } else if(getCount > 491 && getCount < 991 && !keyurl.equals(bbKey)) {
//                keyurl = bbKey;
//            } else if(getCount > 991 && getCount < 1491 && !keyurl.equals(mykey)) {
//                keyurl = mykey;
//            }

            String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/accurate_basic?access_token=" + keyurl;
            String param = "image=" + keyWord;
            String resultStr = sendPost(url, param);
            JSONObject jsonObject = new JSONObject(resultStr);
            System.out.println(jsonObject.toString());
            Object wordsLObject = jsonObject.get("words_result");
            ArrayList<Map<String, String>> wordsLList = JSON.parseObject(wordsLObject.toString(), ArrayList.class);
            //在不是和的情况下：第一个元素是闲的点数，第二个元素是庄的点数，第三个值是闲的输赢，第4个值是庄的输赢。
            //和的情况下:第一个元素是闲的点数，第二个元素是结果和，第三个元素是庄的点数，第四个元素的闲的情况，第五个元素是庄的情况。
            ArrayList<String> strings = new ArrayList<>();
            for (Map<String, String> word : wordsLList) {
                String wordStr = word.get("words");
                strings.add(wordStr);
            }
            resultParseBossWin(strings, filePath);
//            resultParseWin(strings, filePath);
//            resultParseNum(strings);
//            resultParsePease(strings);
            getCount++;
            System.out.println("今日调用次数：" + getCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void resultParseWinV2(int zhuangNum, int xianNum) throws ClientException {
        if (zhuangNum > xianNum) {
            zhuangWinCount++;
            xianWinCount = 0;
            if (zhuangWinCount >= 8 && zhuangWinCount % 8 == 0) {
                jmsService.jmsServiceWin(zhuangWinCount);
            }
        } else if (zhuangNum < xianNum) {
            zhuangWinCount = 0;
            xianWinCount++;
            if (xianWinCount >= 8 && xianWinCount % 8 == 0) {
                jmsService.jmsServiceWin(xianWinCount);
            }
        } else {
            zhuangWinCount++;
            xianWinCount++;
            if (xianWinCount > zhuangWinCount) {
                if (xianWinCount >= 8 && xianWinCount % 8 == 0) {
                    jmsService.jmsServiceWin(xianWinCount);
                }
            } else {
                if (zhuangWinCount >= 8 && xianWinCount % 8 == 0) {
                    jmsService.jmsServiceWin(zhuangWinCount);
                }
            }
        }
    }

    public static void resultParseBossWin(List<String> wordList, String filePath) throws ClientException {
        List<String> list = wordList;
        //获取最终下注情况
        double zhuangkexiafen = Double.parseDouble(list.get(0).substring(4, list.get(0).length()));
        double xiankexiafen = Double.parseDouble(list.get(1).substring(4, list.get(1).length()));
        //获取胜负情况
        int zhuangdianshu = 0;
        int xiandianshu = 0;
        if(list.size() ==5) {
            zhuangdianshu = Integer.parseInt(list.get(3).substring(list.get(3).length() - 1, list.get(3).length()));
            xiandianshu = Integer.parseInt(list.get(4).substring(list.get(4).length() - 1, list.get(4).length()));
        } else if(list.size() == 6 && !"庄闲".equals(list.get(3))) {
            zhuangdianshu = Integer.parseInt(list.get(3).substring(list.get(3).length() - 1, list.get(3).length()));
            xiandianshu = Integer.parseInt(list.get(5).substring(list.get(5).length() - 1, list.get(5).length()));
        } else if(list.size() == 6 && "庄闲".equals(list.get(3))) {
            zhuangdianshu = Integer.parseInt(list.get(4).substring(list.get(4).length() - 1, list.get(4).length()));
            xiandianshu = Integer.parseInt(list.get(5).substring(list.get(5).length() - 1, list.get(5).length()));

        }
        System.out.println(filePath);
        System.out.println("庄下注情况：" + zhuangkexiafen);
        System.out.println("闲下注情况：" + xiankexiafen);
        System.out.println("庄点数：" + zhuangdianshu + "," + "闲点数：" + xiandianshu);
        getWin(zhuangkexiafen, xiankexiafen, zhuangdianshu, xiandianshu);
    }


    public static void getWin(double zhuangkexiafen, double xiankexiafen, int zhuangdianshu, int xiandianshu) throws ClientException {
        if (zhuangkexiafen > xiankexiafen && zhuangdianshu > xiandianshu) {
            bossWinCount++;
            bossfailCount = 0;
            System.out.println("Boss赢:" + bossWinCount);
        } else if (zhuangkexiafen > xiankexiafen && zhuangdianshu < xiandianshu) {
            bossfailCount++;
            bossWinCount = 0;
            System.out.println("Boss输" + bossfailCount);
        } else if (zhuangkexiafen < xiankexiafen && zhuangdianshu < xiandianshu) {
            bossWinCount++;
            bossfailCount = 0;
            System.out.println("Boss赢:" + bossWinCount);
        } else if (zhuangkexiafen < xiankexiafen && zhuangdianshu > xiandianshu) {
            bossfailCount++;
            bossWinCount = 0;
            System.out.println("Boss输:" + bossfailCount);
        } else {
            bossfailCount++;
            bossWinCount++;
            System.out.println("Boss输和" + bossWinCount + "," + bossfailCount);
        }
        if (bossWinCount >= bossTrigeWinOrFailCount && bossWinCount % 5 == 0) {
            String result = bossWinCount + "点数为：" + zhuangdianshu + "," + xiandianshu;
            jmsService.jmsServiceBossWin(result);
        } else if (bossfailCount >= bossTrigeWinOrFailCount && bossfailCount % 5 == 0) {
            String result = bossfailCount + "点数为：" + zhuangdianshu + "," + xiandianshu;
            jmsService.jmsServiceBossFail(result);
        }
    }

    public static void resultParseWinV2(List<String> wordList, String filePath) throws ClientException {
        for (String s : wordList) {
            if (s.contains("庄赢")) {
                zhuangWinCount++;
                xianWinCount = 0;
                if (zhuangWinCount >= 8 && zhuangWinCount % 8 == 0) {
                    jmsService.jmsServiceWin(zhuangWinCount);
                }
            } else if (s.contains("闲赢")) {
                zhuangWinCount = 0;
                xianWinCount++;
                if (xianWinCount >= 8 && xianWinCount % 8 == 0) {
                    jmsService.jmsServiceWin(xianWinCount);
                }
            }
        }
    }

    public static void resultParseWin(List<String> wordsLList, String filePath) throws ClientException {

        String resultStrWord = wordsLList.toString();
        if (resultStrWord.contains("庄赢")) {
            zhuangWinCount++;
            xianWinCount = 0;
            if (zhuangWinCount >= lianyingtrigeCount && zhuangWinCount % 8 == 0) {
                jmsService.jmsServiceWin(zhuangWinCount);
            }
        } else if (resultStrWord.contains("闲赢")) {
            zhuangWinCount = 0;
            xianWinCount++;
            if (xianWinCount >= lianyingtrigeCount && xianWinCount % 8 == 0) {
                jmsService.jmsServiceWin(xianWinCount);
            }
        } else {
            zhuangWinCount++;
            xianWinCount++;
            if (xianWinCount > zhuangWinCount) {
                if (xianWinCount >= lianyingtrigeCount && xianWinCount % 8 == 0) {
                    jmsService.jmsServiceWin(xianWinCount);
                }
            } else {
                if (zhuangWinCount >= lianyingtrigeCount && xianWinCount % 8 == 0) {
                    jmsService.jmsServiceWin(zhuangWinCount);
                }
            }
        }
        System.out.println("文件名：" + filePath + "," + "庄：" + zhuangWinCount + "," + "闲：" + xianWinCount);
    }

    //用于统计天王情况,分2种庄天王和天王
    public static void resultParseNum(List<String> wordsLList) {
        String resultStrWord = wordsLList.toString();
        String xianNumStr = wordsLList.get(0);
        int xianNum = getIntFromString(xianNumStr);
        if (!resultStrWord.contains("赢")) {
            try {
                //获取闲的点数
                if (xianNum == 8 || xianNum == 9) {
                    xianNotTianWangCount = 0;
                    zhuangNotTianWangCount = 0;
                } else {
                    xianNotTianWangCount++;
                    zhuangNotTianWangCount++;
                }

                if (zhuangNotTianWangCount >= tianwangtrigeCount && zhuangNotTianWangCount % 7 == 0) {
                    try {
                        jmsService.jmsServiceTianWang(zhuangNotTianWangCount);
                    } catch (ClientException e) {
                        e.printStackTrace();
                    }
                }
                if (xianNotTianWangCount >= tianwangtrigeCount && xianNotTianWangCount % 7 == 0) {
                    try {
                        jmsService.jmsServiceTianWang(xianNotTianWangCount);
                    } catch (ClientException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("庄点数：" + xianNum + ",闲点数：" + xianNum);

            } catch (Exception e) {
                if (xianNum == 8 || xianNum == 9) {
                    xianNotTianWangCount = 0;
                    zhuangNotTianWangCount = 0;
                } else {
                    xianNotTianWangCount++;
                    zhuangNotTianWangCount++;
                }
            }
        } else {
            //获取闲的点数
            if (xianNum == 8 || xianNum == 9) {
                xianNotTianWangCount = 0;
            } else {
                xianNotTianWangCount++;
            }

            String zhuangNumStr = wordsLList.get(1).toString();
            int zhuangNum = getIntFromString(zhuangNumStr);
            if (zhuangNum == 8 || zhuangNum == 9) {
                zhuangNotTianWangCount = 0;
            } else {
                zhuangNotTianWangCount++;
            }
            if (zhuangNotTianWangCount >= tianwangtrigeCount && zhuangNotTianWangCount % 7 == 0) {
                try {
                    jmsService.jmsServiceTianWang(zhuangNotTianWangCount);
                } catch (ClientException e) {
                    e.printStackTrace();
                }
            }
            if (xianNotTianWangCount >= tianwangtrigeCount && xianNotTianWangCount % 7 == 0) {
                try {
                    jmsService.jmsServiceTianWang(xianNotTianWangCount);
                } catch (ClientException e) {
                    e.printStackTrace();
                }
            }
            heNotComeCount++;
            System.out.println("庄点数：" + zhuangNum + ",闲点数：" + xianNum);
        }
        System.out.println("庄" + zhuangNotTianWangCount + "局没有天王。");
        System.out.println("闲" + xianNotTianWangCount + "局没有天王。");
    }

    public static int getIntFromString(String input) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(input);
        int resultInt = Integer.parseInt(m.replaceAll("").trim());
        return resultInt;
    }

    //用于统计和情况
    public static void resultParsePease(List<String> wordsLList) throws ClientException {
        String resultStrWord = wordsLList.toString();
        if (!resultStrWord.contains("赢")) {
            heNotComeCount = 0;
        }
        if (heNotComeCount >= hetrigeCount && heNotComeCount % 10 == 0) {
            jmsService.jmsServiceHe(heNotComeCount);
        }
        System.out.println("连续" + heNotComeCount + "没有和。");
    }

    //用于统计输赢情况
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static String getURLContent(String urlStr) {
        //请求的url
        URL url = null;
        //请求的输入流
        BufferedReader in = null;
        //输入流的缓冲
        StringBuffer sb = new StringBuffer();
        try {
            url = new URL(urlStr);
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = null;
            //一行一行进行读入
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
        } catch (Exception ex) {

        } finally {
            try {
                if (in != null) {
                    in.close(); //关闭流
                }
            } catch (IOException ex) {

            }
        }
        String result = sb.toString();
        return result;
    }
}
