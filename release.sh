#!/usr/bin/env sh

./gradlew clean build uploadArchives :kgql-gradle-plugin:publishPlugins --no-daemon --no-parallel
