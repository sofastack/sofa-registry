#!/bin/bash

BASE_DIR=`cd $(dirname $0)/../..; pwd`
export SPRING_PROFILES_ACTIVE="dev"
REGISTRY_APP_NAME=integration exec ${BASE_DIR}/bin/base/start_base.sh