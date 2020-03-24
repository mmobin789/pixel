# Pixel

[![CircleCI](https://circleci.com/gh/mmobin789/pixel/tree/master.svg?style=svg)](https://circleci.com/gh/mmobin789/pixel/tree/master)
[![Kotlin Version](https://img.shields.io/badge/kotlin-1.3.70-green.svg)](http://kotlinlang.org/)

An image loading and networking library for Android backed by Kotlin Coroutines.

**Optimal**: Pixel performs optimizations with memory caching, downsampling the image in memory by image view size (pixel by pixel), re-using Bitmaps, automatically pause/cancel requests (Signature requests), and more.

**Light**: Pixel adds less than ~250 methods for now to your APK (for apps that already use coroutines), which is considerably less than Glide,Fresco,Picasso and Coil.

**Easy to use**: Pixel's API uses Kotlin's language features and classic design for simplicity and minimal boilerplate.

**Modern**: Pixel is Kotlin-first and interoperable with Java.

## Features
 - Image Loading (For now image loading from network is supported only)
 - Networking (JSON Objects/Arrays can be loaded/cached from GET urls)
 - Fast (Kotlin Co-routines for structured concurrency with minimal thread pool)
 - Reliable (No 3rd party library used.)
 - Supports JAVA.
 
 
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
 

## QuickStart
 
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

**Load JSON Object**

```
 Pixel.loadJsonObject("https://jsonplaceholder.typicode.com/todos/1") {
        // do something with it here
        }     
```
**Load JSON Array**

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
  
 // Clear JSON cache
  PixelConfiguration.clearDocumentCache()
  
 // Clear all caches
   PixelConfiguration.clearCaches()
   
   ```
   
 **Logging**
 
   ```
 // Enable logging behavior.
   PixelConfiguration.setLoggingEnabled(true)
   
 // Disable logging behavior.
   PixelConfiguration.setLoggingEnabled(false)
   ```
 
 ### In-Development
 The library is maintained and under development as new features are being continuosly added.


### Issues
Issues can be reported [here](https://github.com/mmobin789/pixel/issues).
