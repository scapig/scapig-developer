FROM openjdk:8

COPY target/universal/tapi-developer-*.tgz .
COPY start-docker.sh .
RUN chmod +x start-docker.sh
RUN tar xvf tapi-developer-*.tgz

EXPOSE 8000