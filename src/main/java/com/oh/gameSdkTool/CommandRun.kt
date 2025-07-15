package com.oh.gameSdkTool

abstract class CommandRun {

    protected abstract fun isRun(commandArgs: CommandArgs):Boolean

    protected open fun isIntercept(commandArgs: CommandArgs):Boolean=true

    fun runCommandArgs(commandArgs: CommandArgs):Boolean{
        if (isRun(commandArgs)){
            run(commandArgs)
            return isIntercept(commandArgs)
        }
        return false
    }



    protected abstract fun run(commandArgs: CommandArgs)

}