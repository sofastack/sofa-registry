proto:
	protoc-3.5.1 --proto_path=./server/common/model/src/main/resources/proto --java_out=server/common/model/src/main/java  server/common/model/src/main/resources/proto/*.proto

image:
	docker build . -f docker/Dockerfile -t dzdx/sofa-registry:latest

image_run:
	docker run --rm --net=host --name sofa-registry -e REGISTRY_APP_NAME=integration_dev dzdx/sofa-registry:latest