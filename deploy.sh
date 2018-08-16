#!/usr/bin/env bash
./gradlew clean build bintrayUpload -PbintrayUser=bslience -PbintrayKey=f9123be83afc879730ef570ffdc7abdd9f3e3830 -PdryRun=false
