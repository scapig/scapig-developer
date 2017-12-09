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

## Running
``
docker run -p8000:8000 -i -a stdin -a stdout -a stderr scapig-developer sh start-docker.sh
``