
MODULES=rest-villains rest-heroes rest-fights event-statistics ui-super-heroes

build: $(MODULES)

rest-villains:
	cd super-heroes/$@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
#	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.native -t esara/quarkus-workshop-villain .
	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.native-distroless -t esara/quarkus-workshop-villain .

rest-heroes:
	cd super-heroes/$@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
#	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.native -t esara/quarkus-workshop-hero .
	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.native-distroless -t esara/quarkus-workshop-hero .

rest-fights:
	cd super-heroes/$@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
#	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.native -t esara/quarkus-workshop-fight .
	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.native-distroless -t esara/quarkus-workshop-fight .

event-statistics:
	cd super-heroes/$@; mvn package -Dnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
#	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.native -t esara/quarkus-workshop-statistic .
	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.native-distroless -t esara/quarkus-workshop-statistic .

ui-super-heroes:
	cd super-heroes/$@; mvn install -Pbuild-ui
	cd super-heroes/$@; ./node_modules/.bin/ng build --configuration production --base-href "."
	export DEST=src/main/resources/META-INF/resources; cd super-heroes/$@; rm -Rf $${DEST}; cp -R dist/* $${DEST}
	cd super-heroes/$@; mvn package
	cd super-heroes/$@; docker build -f src/main/docker/Dockerfile.jvm -t esara/quarkus-workshop-ui .

load-super-heroes:
	kubectl port-forward svc/quarkus-workshop-fight 8082:8080 &
	kubectl port-forward svc/quarkus-workshop-hero 8083:8080 &
	kubectl port-forward svc/quarkus-workshop-villain 8084:8080 &
	cd super-heroes/$@; mvn compile; mvn exec:java
