#!/bin/bash

BASE_DIR=`cd $(dirname $0)/../..; pwd`
REGISTRY_APP_NAME=session ${BASE_DIR}/bin/base/shutdown_base.sh
