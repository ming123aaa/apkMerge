package com.ohuang.replacePackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ExecUtil {


    public static String execAndPrint(String[] cmd, int timeOut, boolean isLog) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        String line = null;

        while ((line = reader.readLine()) != null) {
            if (isLog) {
                System.out.println(line);
            }
        }
        InputStream errorStream = p.getErrorStream();
        if (errorStream != null) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
            String errorLine = null;
            while ((errorLine = errorReader.readLine()) != null) {
                System.out.println("Error:" + errorLine);
            }
        }

        try {
            p.destroy();
        } catch (Throwable e) {

        }

        return "Process end";

    }

    public static String execAndPrint(String[] cmd, int timeOut, String readStringStop, boolean isLog) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (isLog) {
                System.out.println(line);
            }
            if (line.startsWith(readStringStop)) {
                p.destroy();
            }
        }
        InputStream errorStream = p.getErrorStream();
        if (errorStream != null) {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
            String errorLine = null;
            while ((errorLine = errorReader.readLine()) != null) {
                System.out.println("Error:" + errorLine);
            }
        }
        try {
            p.destroy();
        } catch (Throwable e) {

        }


        return "Process end";

    }


}
