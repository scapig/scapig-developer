#!/bin/sh
SCRIPT=$(find . -type f -name scapig-developer)
rm -f scapig-developer*/RUNNING_PID
exec $SCRIPT -Dhttp.port=9016 -J-Xms16M -J-Xmx64m
