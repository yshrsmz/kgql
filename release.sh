#!/usr/bin/env sh

./gradlew clean
./gradlew build
./gradlew :kgql-core:publish
./gradlew :kgql-compiler:uploadArchives
./gradlew :kgql-gradle-plugin:uploadArchives
