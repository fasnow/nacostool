package org.fasnow.nacostool;


import org.fasnow.nacostool.Result;

public interface Vuls {
    Result CNVD_2020_67618() throws Exception;
    Result CVE_2021_29441() throws Exception;
    Result CNVD_2021_24491() throws Exception;
    Result QVD_2023_6271() throws Exception;
//    Result CNVD_2023_45001(String targetUrl) throws Exception; //没复现成功
}
