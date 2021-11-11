#!/bin/bash

BASE_DIR=`cd $(dirname $0)/../..; pwd`
export SPRING_CONFIG_LOCATION=${BASE_DIR}/conf/application-h2.properties
REGISTRY_APP_NAME=integration exec ${BASE_DIR}/bin/base/start_base.sh