FROM ubuntu:18.04
LABEL maintainer="dzidaxie@gmail.com"

RUN sed -i s/archive.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list \
        && sed -i s/security.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list \
        && apt-get update
RUN apt-get -y install openjdk-8-jre supervisor less net-tools vim curl iputils-ping


COPY server/distribution/all/target/registry-all.tgz /registry-distribution/registry-all.tgz
RUN tar -xzf /registry-distribution/registry-all.tgz -C /registry-distribution \
    && rm -rf /registry-distribution/registry-all.tgz \
    && cp /registry-distribution/registry-all/conf/supervisord/registry.supervisord.conf /etc/supervisor/conf.d/

CMD ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisor/supervisord.conf"]