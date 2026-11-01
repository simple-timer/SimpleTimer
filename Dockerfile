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
FROM eclipse-temurin:26-jre
RUN apt-get update  \
    && apt-get upgrade -y  \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=compile /app/build/tasks/_app_executableJarJvm/app-jvm-executable.jar .

CMD ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app-jvm-executable.jar"]