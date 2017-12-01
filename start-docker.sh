#!/bin/sh
SCRIPT=$(find . -type f -name tapi-developer)
exec $SCRIPT -Dhttp.port=8000
