package ru.babay.codesamples.sharezip;

import android.net.Uri;

import java.io.File;
import java.util.List;
import java.util.Map;

class ZipFilesUriInfo2 implements MultiZipsUriInfo {
    static final String TYPE = "mf2";
    private final String zipFileName;
    private final File[] files;

    ZipFilesUriInfo2(String zipFileName, File[] files) {
        this.zipFileName = zipFileName;
        this.files = files;
    }


    public ZipFilesUriInfo2(FileProvider.PathStrategy pathStrategy, Uri uri) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() == 0) {
            zipFileName = "";
            files = new File[0];
            return;
        }

        zipFileName = pathSegments.get(0);

        files = new File[pathSegments.size() / 2];

        for (int i = 2; i < pathSegments.size(); i += 2) {
            String rootName = pathSegments.get(i - 1);
            String path = pathSegments.get(i);
            files[(i - 1) / 2] = pathStrategy.getFile(rootName, path);
        }
    }

    @Override
    public Uri toUri(FileProvider.PathStrategy pathStrategy, String authority) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .appendPath(zipFileName)
                .appendQueryParameter(KEY_TYPE, TYPE);

        for (int i = 0; i < files.length; i++) {
            Map.Entry<String, File> root = pathStrategy.getMostSpecificPath(files[i]);
            String path = pathStrategy.shortenPathByRoot(files[i], root);
            builder.appendPath(root.getKey());
            builder.appendPath(path);
        }
        return builder.build();
    }

    @Override
    public long filesLengthSum() {
        long sum = 0;
        for (File file : files) {
            sum += file.length();
        }
        return sum;
    }

    @Override
    public File[] getFiles() {
        return files;
    }

    @Override
    public String getZipFileName() {
        return zipFileName;
    }
}
