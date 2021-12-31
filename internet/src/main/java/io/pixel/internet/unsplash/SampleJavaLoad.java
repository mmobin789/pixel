package io.pixel.internet.unsplash;

import android.widget.ImageView;

import io.pixel.Pixel;
import io.pixel.config.PixelOptions;

public final class SampleJavaLoad {

    public static void load(String path, ImageView imageView) {
        Pixel.load(path, null, imageView);
    }

    public static void load(String path, int placeholderId, ImageView imageView) {
        Pixel.load(path, new PixelOptions.Builder().setPlaceholderResource(placeholderId).build(), imageView);
    }
}
