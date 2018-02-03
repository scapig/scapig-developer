## scapig-developer

This is the microservice responsible of storing and retrieving the developers registered on the Scapig Developer Hub (http://www.scapig.com).

## Building
``
sbt clean test it:test component:test
``

## Packaging
``
sbt universal:package-zip-tarball
docker build -t scapig-developer .
``

## Publishing
``
docker tag scapig-developer scapig/scapig-developer
docker login
docker push scapig/scapig-developer
``

## Running
``
docker run -p9016:9016 -d scapig/scapig-developer
``
