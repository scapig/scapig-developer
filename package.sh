#!/bin/sh
sbt universal:package-zip-tarball
docker build -t scapig-developer .
docker tag scapig-developer scapig/scapig-developer
docker push scapig/scapig-developer
