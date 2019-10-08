package com.mirage.live.upgrade;

/**
 * Created by zwj on 6/4/18.
 */

public interface DownloadProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}
