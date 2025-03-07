# https://github.com/folio-org/folio-tools/tree/master/folio-java-docker/openjdk17
FROM folioci/alpine-jre-openjdk21:latest

# Install latest patch versions of packages: https://pythonspeed.com/articles/security-updates-in-docker/
USER root
RUN apk upgrade --no-cache
USER folio

# Copy your fat jar to the container; if multiple *.jar files exist the .dockerignore file excludes others
COPY target/*.jar ${JAVA_APP_DIR}

# Expose this port locally in the container.
EXPOSE 8081
