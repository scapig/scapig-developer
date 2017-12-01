## tapi-developer

## Building
``
sbt clean test it:test component:test
``

## Packaging
``
sbt universal:package-zip-tarball
docker build -t tapi-developer .
``

## Running
``
docker run -p8000:8000 -i -a stdin -a stdout -a stderr tapi-developer sh start-docker.sh
``