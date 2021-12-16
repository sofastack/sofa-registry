VERSION := $(shell cat VERSION)

proto:
	protoc --proto_path=./server/common/model/src/main/resources/proto --java_out=server/common/model/src/main/java  server/common/model/src/main/resources/proto/*.proto  # protoc-3.5.1

image_build:
	docker build . -f docker/Dockerfile -t  sofaregistry/sofaregistry:$(VERSION)

image_push:
	docker push  sofaregistry/sofaregistry:$(VERSION)
