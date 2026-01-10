
MODULES=rest-villains rest-heroes rest-fights rest-narration event-statistics grpc-locations ui-super-heroes

.PHONY: build $(MODULES)

build: $(MODULES)

rest-villains:
#	cd $@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
#	cd $@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd $@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-villain -Dquarkus.container-image.tag=obiagent -Dquarkus.container-image.push=true

rest-heroes:
#	cd $@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
#	cd $@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd $@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-hero -Dquarkus.container-image.tag=obiagent -Dquarkus.container-image.push=true

rest-fights:
#	cd $@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
#	cd $@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd $@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-fight -Dquarkus.container-image.tag=obiagent -Dquarkus.container-image.push=true

rest-narration:
#	cd $@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
#	cd $@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd $@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-narration -Dquarkus.container-image.tag=obiagent -Dquarkus.container-image.push=true

event-statistics:
#	cd $@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
#	cd $@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd $@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-statistic -Dquarkus.container-image.tag=obiagent -Dquarkus.container-image.push=true

grpc-locations:
#	cd $@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
#	cd $@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd $@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-grpc-locations -Dquarkus.container-image.tag=obiagent -Dquarkus.container-image.push=true

ui-super-heroes:
#	cd $@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
#	cd $@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd $@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-ui -Dquarkus.container-image.tag=obiagent -Dquarkus.container-image.push=true
