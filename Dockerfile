FROM openjdk:11.0.4 AS base
# 时区设置
ENV TZ=Asia/Shanghai
WORKDIR /app
EXPOSE 5123

# build
FROM openjdk:11.0.4 AS build
WORKDIR /src
COPY . .
RUN chmod +x gradlew
#RUN ls -l
# 注意: gradlew 在 Windows 中的 Docker 下，
# 请保证为 Unix(LF) 行尾结束符，而不是 Windows(CR LF)，不然会报错: /usr/bin/env: ‘sh\r’: No such file or directory
RUN ./gradlew jar
RUN ls Server/build/libs -l
RUN echo "build success"

# final
FROM base AS final
WORKDIR /app
# 使用 Server/build/libs 即可
COPY --from=build /src/Server/build/libs .

# 还需要有一个配置文件
#COPY --from=build /src/docker/Config.json .
# Config.json 放在jar同级目录data/下
#RUN mkdir data
#RUN mv Config.json data/Config.json
# 配置文件 不再直接放在容器内，改为 -v 挂载到容器内

RUN ls -l
ENTRYPOINT ["java", "-jar", "Server.jar"]

