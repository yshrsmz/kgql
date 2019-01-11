#!/usr/bin/env sh

./gradlew -Pnative.deploy=true core:publish
./gradlew kgql-compiler:publish
./gradlew kgql-gradle-plugin:publish
