FROM openjdk:8-jdk-alpine

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add --no-cache maven

VOLUME /app
RUN mvn package
ADD ./ /app
WORKDIR /app

RUN ls /app
RUN ls /app/target
CMD java -jar /app/target/flea-0.0.2.jar

EXPOSE 5000