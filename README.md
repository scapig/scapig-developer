## scapig-developer

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
docker tag scapig-developer scapig/scapig-developer:VERSION
docker login
docker push scapig/scapig-developer:VERSION
``

## Running
``
docker run -p9016:9016 -d scapig/scapig-developer:VERSION
``
