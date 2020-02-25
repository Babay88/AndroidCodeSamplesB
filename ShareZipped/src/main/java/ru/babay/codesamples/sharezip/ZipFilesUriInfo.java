package ru.babay.codesamples.sharezip;

import android.net.Uri;
import android.util.Base64;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class ZipFilesUriInfo implements MultiZipsUriInfo {
    static final String TYPE = "mf";
    private final String zipFileName;
    private final File[] files;

    ZipFilesUriInfo(String zipFileName, File[] files) {
        this.zipFileName = zipFileName;
        this.files = files;
    }


    public ZipFilesUriInfo(FileProvider.PathStrategy pathStrategy, Uri uri) {
        String zipFileName = Uri.decode(uri.getEncodedPath());
        if (zipFileName.startsWith("/")) {
            zipFileName = zipFileName.substring(1);
        }
        this.zipFileName = zipFileName;

        Set<String> keys = uri.getQueryParameterNames();
        List<File> files = new ArrayList<>();
        for (String key : keys) {
            try {
                int n = Integer.parseInt(key);
                if (n >= 0 && n < keys.size()) {
                    String encoded = uri.getQueryParameter(key);
                    String path = new String(Base64.decode(encoded, 0));
                    files.add(pathStrategy.getFileForPath(path));
                }
            } catch (Exception e) {
            }
        }
        this.files = files.toArray(new File[0]);
    }

    @Override
    public Uri toUri(FileProvider.PathStrategy pathStrategy, String authority) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .path(zipFileName)
                .appendQueryParameter(KEY_TYPE, TYPE);

        for (int i = 0; i < files.length; i++) {
            String key = Integer.toString(i);
            String path = pathStrategy.getPathForFile(files[i]);
            path = Base64.encodeToString(path.getBytes(), 0);
            builder.appendQueryParameter(key, path);
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
