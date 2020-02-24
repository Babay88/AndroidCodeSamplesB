package ru.babay.codesamples.sharezip;

import android.net.Uri;

import java.io.File;

public interface MultiZipsUriInfo {
    String KEY_TYPE = "t";

    Uri toUri(FileProvider.PathStrategy pathStrategy);

    long filesLengthSum();

    File[] getFiles();

    String getZipFileName();
}
