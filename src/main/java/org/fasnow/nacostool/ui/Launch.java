package org.fasnow.nacostool.ui;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Launch {
    public static void main(String[] args){
//        Application.main(args);
        File directory = new File("D:\\Tools\\code\\nacostool\\nacostool1");
        try {
            FileUtils.forceDelete(directory);
            System.out.println("文件夹删除成功。");
        } catch (IOException e) {
            System.err.println("无法删除文件夹：" + e.getMessage());
        }
    }
}
