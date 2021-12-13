proto:
	protoc-3.5.1 --proto_path=./server/common/model/src/main/resources/proto --java_out=server/common/model/src/main/java  server/common/model/src/main/resources/proto/*.proto

image_build:
	docker build . -f docker/Dockerfile -t  sofastack/sofaregistry:6.1.4

image_push:
	docker push  sofastack/sofaregistry:6.1.4
