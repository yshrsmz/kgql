#!/usr/bin/env sh

./gradlew clean
./gradlew build
./gradlew -Pnative.deploy=true :kgql-core:bintrayUpload
./gradlew kgql-compiler:bintrayUpload
#./gradlew kgql-gradle-plugin:bintrayUpload
./gradlew kgql-gradle-plugin:publishPlugins