#!/bin/bash

set -o pipefail

if [[ -z "$REGISTRY_APP_NAME" ]]; then
    echo "Must provide REGISTRY_APP_NAME in environment" 1>&2
    exit 1
fi

BASE_DIR=/registry-distribution/registry-all
if [ ${REGISTRY_APP_NAME} == "meta" ];then
    exec ${BASE_DIR}/bin/meta/start.sh
elif [ ${REGISTRY_APP_NAME} == "data" ];then
    exec ${BASE_DIR}/bin/data/start.sh
elif [ ${REGISTRY_APP_NAME} == "session" ];then
    exec ${BASE_DIR}/bin/session/start.sh
elif [ ${REGISTRY_APP_NAME} == "integration" ];then
    exec ${BASE_DIR}/bin/integration/start.sh
else
  echo "REGISTRY_APP_NAME ${REGISTRY_APP_NAME} unexpected" 1>&2
  exit 1
fi