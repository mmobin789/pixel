# Pixel

[![CircleCI](https://circleci.com/gh/mmobin789/pixel/tree/master.svg?style=svg)](https://circleci.com/gh/mmobin789/pixel/tree/master)
[![Kotlin Version](https://img.shields.io/badge/kotlin-1.3.70-green.svg)](http://kotlinlang.org/)

An image loading and networking library for Android backed by Kotlin Coroutines.

**Optimal**: Pixel performs optimizations with memory caching, downsampling the image in memory by image view size (pixel by pixel), re-using Bitmaps, automatically pause/cancel requests (Signature requests), and more.

**Light**: Pixel adds less than ~250 methods for now to your APK (for apps that already use coroutines), which is considerably less than Glide,Fresco,Picasso and Coil.

**Easy to use**: Pixel's API uses Kotlin's language features and classic design for simplicity and minimal boilerplate.

**Modern**: Pixel is Kotlin-first and interoperable with Java.

## Features
 - Image Loading (For now image loading from network is supported only.)
 - Networking (JSON Objects/Arrays can be loaded/cached from GET urls.)
 - Fast (Only uses Kotlin Co-routines for structured concurrency with minimal thread pool.)
 - Reliable (No 3rd party library used.)


### In-Development
 The library is maintained and under development as new features are being continuosly added.


### Issues
Issues can be reported [here](https://github.com/mmobin789/pixel/issues).
