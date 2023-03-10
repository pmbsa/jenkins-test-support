package com.iconsolutions.jenkins.test
import groovy.transform.Field

@Field
def currentLevel= "INFO"
@Field
List logList = []

@Field
OFFLIST = []

@Field
FATALLIST= ["FATAL"]

@Field
ERRORLIST = ["FATAL", "ERROR"]

@Field
WARNLIST= ["FATAL", "ERROR", "WARNING"]

@Field
INFOLIST= ["FATAL", "ERROR", "WARNING", "INFO"]

@Field
DEBUGLIST= ["FATAL", "ERROR", "WARNING", "INFO", "DEBUG"]

Field
ALLLIST = DEBUGLIST

@Field
TRACELIST = DEBUGLIST

// no debug setting is set to INFO Groovy.transform.Field
LIST = INFOLIST

def info(def loggerclass, def message) { Logger( level: "INFO", Loggerclass, message) }

def warn(def loggerclass, def message) { Logger( level: "WARNING", loggerclass, message) }

def error(def loggerclass, def message) { Logger( level: "ERROR", Loggerclass, message) }

def debug(def loggerclass, def message) { Logger( level: "DEBUG", Loggerclass, message) }


private logger (def level, def loggerclass, def message) {
    if (this."${currentLevel}LIST" && this."${currentLevel}LIST".contains(level)) {
        addToList(new String(level + ": " + loggerclass + " : " + message))
    }
}

def addToList(String listItem) { logList.add(listItem.trim()) }

def setCurrentLevel (def newLevel) { currentLevel = newLevel}