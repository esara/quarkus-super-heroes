
MODULES=rest-villains rest-heroes rest-fights event-statistics

build: $(MODULES)

rest-villains:
# 	cd super-heroes/$@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
# 	cd super-heroes/$@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd super-heroes/$@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-villain -Dquarkus.container-image.tag=latest -Dquarkus.container-image.push=true

rest-heroes:
# 	cd super-heroes/$@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
# 	cd super-heroes/$@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd super-heroes/$@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-hero -Dquarkus.container-image.tag=latest -Dquarkus.container-image.push=true

rest-fights:
# 	cd super-heroes/$@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
# 	cd super-heroes/$@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd super-heroes/$@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-fight -Dquarkus.container-image.tag=latest -Dquarkus.container-image.push=true

event-statistics:
# 	cd super-heroes/$@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless
# 	cd super-heroes/$@; mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.native-distroless -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.push=true
	cd super-heroes/$@; mvn package -Dquarkus.native.container-build=false -Dquarkus.container-image.build=true -Dquarkus.docker.dockerfile-native-path=src/main/docker/Dockerfile.jvm -Dquarkus.docker.buildx.platform="linux/amd64,linux/arm64" -Dquarkus.container-image.name=quarkus-workshop-statistic -Dquarkus.container-image.tag=latest -Dquarkus.container-image.push=true

ui-super-heroes:
	cd super-heroes/$@; mvn install -Pbuild-ui
	cd super-heroes/$@; ./node_modules/.bin/ng build --configuration production --base-href "."
	export DEST=src/main/resources/META-INF/resources; cd super-heroes/$@; rm -Rf $${DEST}; cp -R dist/* $${DEST}
	cd super-heroes/$@; mvn package
#	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.jvm -t esara/quarkus-workshop-ui .
	cd super-heroes/$@; docker buildx build -f src/main/docker/Dockerfile.jvm -t esara/quarkus-workshop-ui --platform "linux/amd64,linux/arm64" --push .

load-super-heroes:
	kubectl port-forward svc/quarkus-workshop-fight 8082:8080 &
	kubectl port-forward svc/quarkus-workshop-hero 8083:8080 &
	kubectl port-forward svc/quarkus-workshop-villain 8084:8080 &
	cd super-heroes/$@; mvn compile; mvn exec:java
