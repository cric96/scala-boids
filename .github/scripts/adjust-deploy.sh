#!/usr/bin/env bash
mkdir ./public
mkdir ./public/target
mkdir ./public/target/scala-3.1.3

cp ./boids/.js/target/scala-3.1.3/scala-boids-fastopt.js ./public/target/scala-3.1.3/scala-boids-fastopt.js
cp ./boids/.js/index.html ./public/index.html