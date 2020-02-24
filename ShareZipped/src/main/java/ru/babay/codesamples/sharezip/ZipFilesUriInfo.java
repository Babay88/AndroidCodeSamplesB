package ru.babay.codesamples.sharezip;

import android.net.Uri;
import android.util.Base64;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class ZipFilesUriInfo implements MultiZipsUriInfo {
    static final String TYPE = "multiFiles";
    private final String zipFileName;
    private final File[] files;

    ZipFilesUriInfo(String zipFileName, File[] files) {
        this.zipFileName = zipFileName;
        this.files = files;
    }


    public ZipFilesUriInfo(FileProvider.PathStrategy pathStrategy, Uri uri) {
        File file = pathStrategy.getFileForUri(uri);
        zipFileName = file.getName();

        Set<String> keys = uri.getQueryParameterNames();
        List<File> files = new ArrayList<>();
        for (String key : keys) {
            try {
                int n = Integer.parseInt(key);
                if (n >= 0 && n < keys.size()) {
                    String encoded = uri.getQueryParameter(key);
                    String path = new String(Base64.decode(encoded, 0));
                    files.add(new File(path));
                }
            } catch (Exception e) {
            }
        }
        this.files = files.toArray(new File[0]);
    }

    @Override
    public Uri toUri(FileProvider.PathStrategy pathStrategy) {
        File parentDir = files[0].getParentFile();
        Uri uri = pathStrategy.getUriForFile(new File(parentDir, zipFileName));

        Uri.Builder builder = new Uri.Builder()
                .scheme(uri.getScheme())
                .authority(uri.getAuthority())
                .encodedPath(uri.getPath())
                .appendQueryParameter(KEY_TYPE, TYPE);

        for (int i = 0; i < files.length; i++) {
            String key = Integer.toString(i);
            String path = Base64.encodeToString(files[i].getAbsolutePath().getBytes(), 0);
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
