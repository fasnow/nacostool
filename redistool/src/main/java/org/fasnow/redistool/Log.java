package org.fasnow.redistool;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    static String filename = "log.txt";

    public static void info(String content){
        try {
            // 使用FileWriter追加模式写入内容
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter writer = new BufferedWriter(fw);
            if(!content.endsWith("\n")){
                // 写入换行符
                content+="\n";
            }
            writer.write(content);
            writer.close();
            System.out.print(content);
        } catch (IOException e) {
            System.out.printf(e.getMessage());
        }
    }

    public static String getFormatDate(){
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");
        return currentTime.format(formatter);
    }

    public static String formatStdout(boolean plus, String targetUrl, String vulName, String featureField){
        String s = String.format("[%s] [%s] %s%s\n",
                getFormatDate(),
                plus?"+":"-",
                targetUrl+(plus?" 存在 ":" 不存在 ")+vulName,
                plus && !"".equals(featureField)?"\n\t"+featureField:""
        );
        info(s);
        return s;
    }

    public static String formatStdout(boolean plus,String msg){
        if(msg.split("\n").length==1){
            String s = String.format("[%s] [-] %s\n",getFormatDate(),msg);
            info(s);
            return s;
        }
        msg = msg.replaceAll("\n","\n\t");
        String breakSep = "";
        if(msg.length()>2 && msg.substring(0,msg.length()-2).contains("\n")){
            breakSep="\n\t";
        }
        String s = String.format("[%s] [%s] %s\n\n",plus?"+":"-",getFormatDate(),breakSep+msg);
        info(s);
        return s;
    }

    public static String formatStderr(Exception e){
        String msg = "";
        msg=e.getMessage();
        if(!"".equals(msg)){
            String s =  String.format("[%s] [-] %s\n",getFormatDate(),msg);
            info(s);
            return s;
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        String tmp = stringWriter.toString();
        String out = tmp.substring(0,tmp.length()-1);
        String s = String.format("[%s] [-]\n\t%s\n",getFormatDate(),out.replace("\n", "\n\t"));
        info(s);
        return s;
    }
}
