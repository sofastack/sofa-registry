#!/bin/bash

set -o pipefail

if [[ -z "$REGISTRY_APP_NAME" ]]; then
    echo "Must provide REGISTRY_APP_NAME in environment" 1>&2
    exit 1
fi
echo "killing registry-${REGISTRY_APP_NAME} server..."
pids=`ps aux | grep java | grep registry-${REGISTRY_APP_NAME} | awk '{print $2;}'`
for pid in "${pids[@]}"
do
    if [ -z "$pid" ]; then
        continue;
    fi
kill $pid
done
echo "Done!"