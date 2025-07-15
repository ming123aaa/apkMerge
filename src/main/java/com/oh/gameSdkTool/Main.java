package com.oh.gameSdkTool;

import com.beust.jcommander.JCommander;

import java.util.ServiceLoader;

public class Main {

    public static void main(String[] args) {
        CommandArgs main = new CommandArgs();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(main)
                .build();
        jCommander.parse(args);
        if (main.help) {
            jCommander.usage();
        } else {
            ArgsRunTime.INSTANCE.run(main);
        }
    }
}

