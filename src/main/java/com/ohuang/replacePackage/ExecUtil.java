package com.ohuang.replacePackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class ExecUtil {

    public static String exec(String[] cmd, int timeOut, boolean isLog) throws IOException, InterruptedException {
        return exec(cmd, timeOut, isLog, true);
    }
    public  interface OnProgress {
        void onProgress(String line);
    }

    public static String exec(String[] cmd, int timeOut, OnProgress log, OnProgress logError) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);

        // 异步读取标准输出
        Thread outThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (log!=null) {
                        log.onProgress(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 异步读取错误输出
        Thread errThread = new Thread(() -> {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    if (logError!=null) {
                        logError.onProgress(errorLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outThread.start();
        errThread.start();

        // 设置超时等待
        boolean exited = p.waitFor(timeOut, TimeUnit.SECONDS);
        if (!exited) {
            System.err.println("Process timed out.");
            p.destroyForcibly();
        }

        // 等待输出线程完成
        outThread.join();
        errThread.join();

        try {
            p.destroy();
        } catch (Throwable e) {
            // Ignore
        }

        return "Process end";

    }

    public static String exec(String[] cmd, int timeOut, boolean isLog, boolean logError) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);

        // 异步读取标准输出
        Thread outThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isLog) {
                        System.out.println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 异步读取错误输出
        Thread errThread = new Thread(() -> {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    if (logError) {
                        System.out.println("Error: " + errorLine);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outThread.start();
        errThread.start();

        // 设置超时等待
        boolean exited = p.waitFor(timeOut, TimeUnit.SECONDS);
        if (!exited) {
            System.err.println("Process timed out.");
            p.destroyForcibly();
        }

        // 等待输出线程完成
        outThread.join();
        errThread.join();

        try {
            p.destroy();
        } catch (Throwable e) {
            // Ignore
        }

        return "Process end";

    }

    public static String execWaitStringStop(String[] cmd, int timeOut, String readStringStop, boolean isLog) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);

        // 异步读取标准输出
        Thread outThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isLog) {
                        System.out.println(line);
                    }
                    if (line.startsWith(readStringStop)) {
                        p.destroy();
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 异步读取错误输出
        Thread errThread = new Thread(() -> {
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    System.out.println("Error: " + errorLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        outThread.start();
        errThread.start();

        // 设置超时等待
        boolean exited = p.waitFor(timeOut, TimeUnit.SECONDS);
        if (!exited) {
            System.err.println("Process timed out.");
            p.destroyForcibly();
        }

        // 等待输出线程完成
        outThread.join();
        errThread.join();

        try {
            p.destroy();
        } catch (Throwable e) {
            // Ignore
        }


        return "Process end";

    }


}
