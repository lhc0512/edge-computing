FROM lhc-ubuntu-20:v1.1
LABEL maintainer=hongcai
ENV PATH /root/lhc_dev/jdk-17.0.5/bin:${PATH}
COPY target/edge-node-1.0-SNAPSHOT.jar   /root/app.jar
WORKDIR /root
ENTRYPOINT ["sh","-c","java -jar app.jar --spring_application_name=$spring_application_name"]