#!/usr/bin/env sh

./gradlew clean build publish :kgql-gradle-plugin:publishPlugins --no-daemon --no-parallel
