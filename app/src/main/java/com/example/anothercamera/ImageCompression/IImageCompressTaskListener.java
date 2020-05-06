package com.example.anothercamera.ImageCompression;

import java.io.File;
import java.util.List;

/**
 * Author Jaba
 */

public interface IImageCompressTaskListener {

    void onComplete(List<File> compressed);

    void onError(Throwable error);
}
