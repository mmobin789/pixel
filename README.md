# Pixel
![pixel](https://scontent.flhe3-1.fna.fbcdn.net/v/t1.15752-9/99293309_295343698156112_6153396621322223616_n.png?_nc_cat=105&_nc_sid=b96e70&_nc_eui2=AeF3NzTNx9KrpvA1NgheAod7z_B803KdsfHP8HzTcp2x8YmOcl8ROyn80k6Ee34fD4DXuqRRYQNo1Ir2o5FOHAn1&_nc_ohc=5FjqN-dgZbQAX9dlsbn&_nc_ht=scontent.flhe3-1.fna&oh=27c73a7595a7d7a5e2f6d3d8f8f1192b&oe=5EF40FB8&dl=1)

[![CircleCI](https://circleci.com/gh/mmobin789/pixel/tree/master.svg?style=svg)](https://circleci.com/gh/mmobin789/pixel/tree/master)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/863ea5386f204bd4807580d3ae231874)](https://www.codacy.com/manual/mmobin789/pixel?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mmobin789/pixel&amp;utm_campaign=Badge_Grade)

[![Kotlin Version](https://img.shields.io/badge/kotlin-1.3.72-green.svg)](http://kotlinlang.org/)

A lightweight image loading library for Android backed by Kotlin Coroutines.

**Optimal**: Pixel performs optimizations with memory caching, downsampling the image in memory by image view size (pixel by pixel), re-using Bitmaps, automatically pause/cancel requests (Signature requests), and more.

**Light**: Pixel adds less than ~250 methods for now to your APK (for apps that already use coroutines), which is considerably less than Glide,Fresco,Picasso and Coil.

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
 implementation 'io.pixel.android:pixel:0.0.2'
 ```
 
 **Maven**
  
```
 <dependency>
  <groupId>io.pixel.android</groupId>
  <artifactId>pixel</artifactId>
  <version>0.0.2</version>
  <type>pom</type>
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
 **Cancel an image load**
 
```
// cancels the load 
Pixel.load("https://images.unsplash.com/photo-1492684223066-81342ee5ff30?ixlib=rb-1.2.1&auto=format&fit=crop&w=1000&q=80", iv1).cancel()

```

**Load JSON Object (Till Version 0.0.2)**

```
 Pixel.loadJsonObject("https://jsonplaceholder.typicode.com/todos/1") {
        // do something with it here
        }     
```
**Load JSON Array (Till Version 0.0.2)**

```
Pixel.loadJsonArray("https://jsonplaceholder.typicode.com/users") {
       // do something with it here.
        }       
```
 
 **Configure and clear memory cache**
 
 ```
 // Set image memory cache to 48000 KBS which is 48MBS
  PixelConfiguration.setImageMemoryCacheSize(48000)
  
 // Set JSON memory cache to 16000 KBS which is 16MBS
  PixelConfiguration.setJSONMemoryCacheSize(16000)
  
 // Clear image cache
  PixelConfiguration.clearImageCache()
  
 // Clear JSON cache (Till Version 0.0.2)
  PixelConfiguration.clearDocumentCache()
  
 // Clear all caches
   PixelConfiguration.clearCaches()
   
   ```
   
 **Logging**
 
   ```
 // Enable logging behavior
   PixelConfiguration.setLoggingEnabled(true)
   
 // Disable logging behavior (Default)
   PixelConfiguration.setLoggingEnabled(false)
   ```
   ## Requirements
   - Min SDK 14
   - Compile SDK 29+
   
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
