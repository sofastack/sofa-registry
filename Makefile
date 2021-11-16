proto:
	protoc-3.5.1 --proto_path=./server/common/model/src/main/resources/proto --java_out=server/common/model/src/main/java  server/common/model/src/main/resources/proto/*.proto

image_build:
	docker build . -f docker/Dockerfile -t dzdx/sofa-registry-test:latest

image_push:
	docker push dzdx/sofa-registry-test:latest

app_run:
	docker run -e REGISTRY_APP_NAME=integration -d  --net=host --name=sofa-registry --rm  -v /Users/dzdx/Desktop/registry-all/conf/application.properties:/registry-distribution/registry-all/conf/application.properties   dzdx/sofa-registry-test:latest
mysql_run:
	docker run --rm -e MARIADB_ROOT_PASSWORD=root -p 3306:3306 --name=mysql -v /Users/dzdx/Desktop/registry-all:/registry-distribution/registry-all -d  --net=host mariadb:10.7
