#!/usr/bin/env bash
mkdir ./public
mkdir ./public/target
mkdir ./public/target/scala-3.1.3/scala-boids-fastopt

cp ./boids/js/target/scala-3.1.3/scala-boids-fastopt/main.js ./public/target/scala-3.1.3/scala-boids-fastopt/
cp ./boids/js/index.html ./public/index.html
cp ./boids/js/style.css ./public/style.css