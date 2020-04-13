#!/usr/bin/env sh

./gradlew clean
./gradlew build
./gradlew :kgql-core:publishAllPublicationsToMavenRepository
./gradlew :kgql-compiler:publishAllPublicationsToMavenRepository
./gradlew :kgql-gradle-plugin:publishAllPublicationsToMavenRepository :kgql-gradle-plugin:publishPlugins
