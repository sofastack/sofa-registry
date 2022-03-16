# docker-compose 方式运行 sofa-registry

启动：

````shell
docker-compose -f sofa-registry-in-docker-compose.yml up -d
````

清理：

````shell
docker-compose -f sofa-registry-in-docker-compose.yml down
````

检查：

````shell
# 查看meta角色的健康检测接口：(3台机器，有1台是Leader，其他2台是Follower)
$ curl http://localhost:9615/health/check
{"success":true,"message":"..."}

# 查看data角色的健康检测接口：
$ curl http://localhost:9622/health/check
{"success":true,"message":"..."}

# 查看session角色的健康检测接口：
$ curl http://localhost:9603/health/check
{"success":true,"message":"..."}
````