FROM ubuntu:18.04
LABEL maintainer="dzidaxie@gmail.com"

RUN sed -i s/archive.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list \
        && sed -i s/security.ubuntu.com/mirrors.aliyun.com/g /etc/apt/sources.list \
        && apt-get update
RUN apt-get -y install openjdk-8-jdk supervisor less net-tools vim curl iputils-ping telnet unzip

COPY server/distribution/all/target/registry-all.tgz /registry-distribution/registry-all.tgz
RUN tar -xzf /registry-distribution/registry-all.tgz -C /registry-distribution \
    && rm -rf /registry-distribution/registry-all.tgz

RUN useradd -s /bin/bash admin && usermod -a -G admin admin && mkdir -p /home/admin/logs && chown -R admin:admin  /home/admin/logs && chmod -R 755 /home/admin/logs
USER admin
CMD ["/registry-distribution/registry-all/bin/registry-run.sh"]