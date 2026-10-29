#コンパイル
FROM eclipse-temurin:26-jdk AS compile
RUN apt-get update  \
    && apt-get upgrade -y  \
    && apt-get install -y --no-install-recommends curl unzip  \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN chmod +x ./kotlin && ./kotlin package

#Botを起動
FROM eclipse-temurin:26-jre-alpine AS bot
RUN apk update && apk upgrade

WORKDIR /app
COPY --from=compile /app/build/tasks/_app_executableJarJvm/app-jvm-executable.jar .

CMD ["java", "-jar", "app-jvm-executable.jar"]