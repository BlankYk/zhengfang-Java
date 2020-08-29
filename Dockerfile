FROM openjdk:8-jdk-alpine

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk add --no-cache maven

VOLUME /app

ADD ./build /app
WORKDIR /app
RUN ls /app
CMD java -jar /app/flea-1.0.0.jar

EXPOSE 5000