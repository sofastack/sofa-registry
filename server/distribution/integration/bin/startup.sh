#!/bin/sh

# Copyright 1999-2018 Alibaba Group Holding Ltd.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# constants
APP_NAME="integration"
BASE_DIR=`cd $(dirname $0)/..; pwd`
APP_JAR="${BASE_DIR}/registry-${APP_NAME}.jar"

# app conf
JAVA_OPTS="$JAVA_OPTS -Dregistry.${APP_NAME}.home=${BASE_DIR}"
JAVA_OPTS="$JAVA_OPTS -Dspring.config.location=${BASE_DIR}/conf/application.properties"

# set user.home
JAVA_OPTS="$JAVA_OPTS -Duser.home=${BASE_DIR}"

# springboot conf
SPRINGBOOT_OPTS="${SPRINGBOOT_OPTS} --logging.config=${BASE_DIR}/conf/logback-spring.xml"

# heap size
HEAP_MAX=512
HEAP_NEW=256
JAVA_OPTS="$JAVA_OPTS -server -Xms${HEAP_MAX}m -Xmx${HEAP_MAX}m -Xmn${HEAP_NEW}m -Xss256k"

# gc option
mkdir -p "${BASE_DIR}/logs"
JAVA_OPTS="$JAVA_OPTS -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:${BASE_DIR}/logs/registry-${APP_NAME}-gc.log -verbose:gc"
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${BASE_DIR}/logs -XX:ErrorFile=${BASE_DIR}/logs/registry-${APP_NAME}-hs_err_pid%p.log"
JAVA_OPTS="$JAVA_OPTS -XX:-OmitStackTraceInFastThrow -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4"
JAVA_OPTS="$JAVA_OPTS -XX:+CMSClassUnloadingEnabled -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70"

# rm raftData
rm -rf ${BASE_DIR}/raftData/

# start
STD_OUT="${BASE_DIR}/logs/registry-${APP_NAME}-std.out"
rm -f $STD_OUT
echo "Command: java ${JAVA_OPTS} -jar ${APP_JAR} ${SPRINGBOOT_OPTS}" > $STD_OUT
java ${JAVA_OPTS} -jar ${APP_JAR} ${SPRINGBOOT_OPTS} >> "$STD_OUT" 2>&1 &
PID=$!
echo "registry-${APP_NAME}(pid is $PID) is starting, you can check the $STD_OUT for more details"