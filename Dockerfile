FROM openjdk:8

COPY target/universal/scapig-developer-*.tgz .
COPY start-docker.sh .
RUN chmod +x start-docker.sh
RUN tar xvf scapig-developer-*.tgz
EXPOSE 9016

CMD ["sh", "start-docker.sh"]