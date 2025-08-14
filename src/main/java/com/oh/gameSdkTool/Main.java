package com.oh.gameSdkTool;

import com.beust.jcommander.JCommander;

import java.util.ServiceLoader;

public class Main {

    public static void main(String[] args) {
        CommandArgs mainCommandArgs = new CommandArgs();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(mainCommandArgs)
                .build();
        jCommander.parse(args);
        if (mainCommandArgs.help) {
            jCommander.usage();
        } else {
            ArgsRunTime.INSTANCE.run(mainCommandArgs,jCommander);
        }
    }
}

