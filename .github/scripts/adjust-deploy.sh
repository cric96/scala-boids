#!/usr/bin/env bash
mkdir ./public
mkdir ./public/target
mkdir ./public/target/scala-3.9.0/
mkdir ./public/target/scala-3.9.0/scalajs-bundler
mkdir ./public/target/scala-3.9.0/scalajs-bundler/main


cp ./boids/js/target/scala-3.9.0/scalajs-bundler/main/scala-boids-fastopt-bundle.js ./public/target/scala-3.9.0/scalajs-bundler/main
cp ./boids/js/index.html ./public/index.html
cp ./boids/js/style.css ./public/style.css
