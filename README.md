# Pixel
![pixel](https://miro.medium.com/max/875/0*iAMn0EvUMF__xp3F)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.mmobin789.pixel/pixel.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.mmobin789.pixel%22%20AND%20a:%22pixel%22)
[![CircleCI](https://circleci.com/gh/mmobin789/pixel/tree/develop.svg?style=svg)](https://circleci.com/gh/mmobin789/pixel/tree/develop)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/99e37f923d05499e9554019ebb4ac291)](https://www.codacy.com/gh/mmobin789/pixel/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mmobin789/pixel&amp;utm_campaign=Badge_Grade)

[![Kotlin Version](https://img.shields.io/badge/kotlin-1.4.31-orange.svg)](http://kotlinlang.org/)

A lightweight image loading library for Android backed by Kotlin Coroutines.

**Optimal**: Pixel performs optimizations with memory & disk caching, downsampling the image in memory by image view size (pixel by pixel), re-using Bitmaps, automatically pause/cancel requests (Signature requests), and more.

**Light**: Pixel adds less than ~100 methods for now to your APK, which is considerably less than Glide,Fresco,Picasso and Coil.

**Easy to use**: Pixel's API uses Kotlin's language features and classic design for simplicity and minimal boilerplate.

**Modern**: Pixel is Kotlin-first and interoperable with Java.

## Features
 - Signature Loads
 - Image Loading (For now image loading from network is supported only)
 - Modern (Kotlin Co-routines for structured concurrency and low latency)
 - Reliable (No 3rd party library used)
 - Supports JAVA
 
 
 ## Why to use ?
   
   - It only downloads the image per width and height of image view per **pixel** hence the name and pauses all loads when UI is not          available.
   - Signature Download (Same image download with same requested width and height will cancel previous such download in progress)
   - Synchronous load cancellation.
   - UI Responsive.
  
 
 
 ## Download
 
 **Gradle**
 ```
 implementation 'io.github.mmobin789.pixel:pixel:1.0.0-alpha'
 ```
 
  **Gradle Kotlin DSL**
 ```
implementation("io.github.mmobin789.pixel:pixel:1.0.0-alpha")
 ```
 
 **Maven**
  
```
<dependency>
  <groupId>io.github.mmobin789.pixel</groupId>
  <artifactId>pixel</artifactId>
  <version>1.0.0-alpha</version>
  <type>aar</type>
</dependency>
```
 

## Documentation
 
For complete usage in Kotlin and Java clone the project in Android Studio and run the sample app included.

 
 **Load an Image**
 
 ```
 Pixel.load("https://images.unsplash.com/photo-1492684223066-81342ee5ff30?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80", iv)
 ```
 **Load an Image with options**
 
 ```
  //Loads an image with a placeholder resource.
 Pixel.load("image path", iv3, PixelOptions.Builder().setPlaceholderResource(R.drawable.ic_loading_android).build())
 
 /**Loads an image of 30x30 pixels with a placeholder resource.
 For best result, know the size of image from source and don't provide size less than size of image view
 **/
 Pixel.load("image path", holder.iv, PixelOptions.Builder().setPlaceholderResource(R.drawable.ic_loading_android)
 .setImageSize(30, 30).build())

 ```
 
 **Configure memory and disk cache**
 
 ```
 // Set image memory cache to 48MBS
  PixelConfiguration.setImageMemoryCacheSize(48)
  
 // Set Disk cache to 512MBS
  PixelConfiguration.setDiskCacheSize(512)
  
 // Clear image cache
  PixelConfiguration.clearImageCache()
  
   
   ```
   
 **Logging**
 
   ```
 // Enable logging behavior
   PixelConfiguration.setLoggingEnabled(true)
   
 // Disable logging behavior (Default)
   PixelConfiguration.setLoggingEnabled(false)
   ```
   ## Requirements
   - Min SDK 23
   - Compile SDK 30+
   
   ## R8 / Proguard
   Pixel is completely compatible with R8 and doesn't require adding any extra rules.

   If you use Proguard, you may need to add rules for [Coroutines](https://github.com/Kotlin/kotlinx.coroutines/blob/master/kotlinx-coroutines-core/jvm/resources/META-INF/proguard/coroutines.pro)
   
   ## Details
   
   Read more on [medium](https://medium.com/better-programming/introducing-pixel-a-new-kotlin-image-loading-library-for-android-a37a7a192f73).
   
   ## Releases
   See release notes [here](https://github.com/mmobin789/pixel/releases).
 
 ### In-Development
 This library is maintained and under development as new features are periodically added.


## Issues
Issues can be reported [here](https://github.com/mmobin789/pixel/issues).

## Inspiration
The idea of employing Kotlin Coroutines that drive the working of this library partly came from [Coil](https://github.com/coil-kt).

## Co-Creator
  [Malik Dawar](https://github.com/malikdawar)

## License
Copyright 2020 Pixel Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
