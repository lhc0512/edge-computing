FROM lhc-ubuntu-20:v1.1
LABEL maintainer=hongcai
ENV PATH /root/lhc_dev/jdk-17.0.5/bin:${PATH}
COPY target/edge-experiment-1.0-SNAPSHOT.jar   /root/app.jar
WORKDIR /root
ENTRYPOINT ["sh","-c","java -jar app.jar"]