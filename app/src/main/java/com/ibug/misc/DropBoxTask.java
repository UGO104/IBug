package com.ibug.misc;

import android.content.Context;
import java.io.File;

public interface DropBoxTask {
    void download(File... localFileDirectory)throws Exception;
    void upload(File... localFileDirectory)throws Exception;
}
