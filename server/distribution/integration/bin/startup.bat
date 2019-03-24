@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem constants
set APP_NAME=integration
set BASE_DIR=%~dp0
set BASE_DIR=%BASE_DIR:~0,-5%
set APP_JAR=%BASE_DIR%\registry-%APP_NAME%.jar

rem app conf
set "JAVA_OPT=%JAVA_OPT% -Dregistry.%APP_NAME%.home=%BASE_DIR%"
set "JAVA_OPT=%JAVA_OPT% -Dspring.config.location=%BASE_DIR%\conf\application.properties"

rem set user.home
set "JAVA_OPT=%JAVA_OPT% -Duser.home=%BASE_DIR%"

rem springboot conf
set "SPRINGBOOT_OPTS=%SPRINGBOOT_OPTS% --logging.config=%BASE_DIR%\conf\logback-spring.xml"

rem heap size
set HEAP_MAX=512
set HEAP_NEW=256
set "JAVA_OPT=%JAVA_OPT% -server -Xms%HEAP_MAX%m -Xmx%HEAP_MAX%m -Xmn%HEAP_NEW%m -Xss256k"

rem gc option
if not exist %BASE_DIR%\logs md %BASE_DIR%\logs
set "JAVA_OPTS=%JAVA_OPT% -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:%BASE_DIR%\logs\registry-%APP_NAME%-gc.log -verbose:gc"
set "JAVA_OPTS=%JAVA_OPT% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%BASE_DIR%\logs -XX:ErrorFile=%BASE_DIR%\logs\registry-%APP_NAME%-hs_err_pid%p.log"
set "JAVA_OPTS=%JAVA_OPT% -XX:-OmitStackTraceInFastThrow -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4"
set "JAVA_OPTS=%JAVA_OPT% -XX:+CMSClassUnloadingEnabled -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70"

rem rm raftData
if exist %BASE_DIR%\raftData rd /s /Q %BASE_DIR%\raftData

rem start
set STD_OUT=%BASE_DIR%\logs\registry-%APP_NAME%-std.out
if exist %STD_OUT% del %STD_OUT%
echo registry-%APP_NAME% is starting, you can check the %STD_OUT% for more details
echo "Command: java %JAVA_OPTS% -jar %APP_JAR% %SPRINGBOOT_OPTS%" > %STD_OUT%
java %JAVA_OPTS% -jar %APP_JAR% %SPRINGBOOT_OPTS% >> %STD_OUT% 2>&1
