#!/bin/bash

BASE_DIR=`cd $(dirname $0)/../..; pwd`
REGISTRY_APP_NAME=meta ${BASE_DIR}/bin/base/shutdown_base.sh
