package ru.babay.codesamples.sharezip;

import android.net.Uri;
import android.util.Base64;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class FilesInFolderZipUriInfo implements MultiZipsUriInfo {
    static final String TYPE = "od";
    private final File folder;
    private final String zipFileName;
    private final String[] fileNames;

    FilesInFolderZipUriInfo(File folder, String zipFileName, String[] fileNames) {
        this.folder = folder;
        this.zipFileName = zipFileName;
        this.fileNames = fileNames;
    }

    public FilesInFolderZipUriInfo(FileProvider.PathStrategy pathStrategy, Uri uri) {
        File file = pathStrategy.getFileForUri(uri);
        zipFileName = file.getName();
        folder = file.getParentFile();

        Set<String> keys = uri.getQueryParameterNames();
        List<String> fileNames = new ArrayList<>();
        for (String key : keys) {
            try {
                int n = Integer.parseInt(key);
                if (n >= 0 && n < keys.size()) {
                    String encoded = uri.getQueryParameter(key);
                    String name = new String(Base64.decode(encoded, 0));
                    fileNames.add(name);
                }
            } catch (Exception e) {
            }
        }
        this.fileNames = fileNames.toArray(new String[0]);
    }

    @Override
    public Uri toUri(FileProvider.PathStrategy pathStrategy, String authority) {
        Uri.Builder builder = new Uri.Builder()
                .scheme("content")
                .authority(authority)
                .encodedPath(pathStrategy.getPathForFile(new File(folder, zipFileName)))
                .appendQueryParameter(KEY_TYPE, TYPE);

        for (int i = 0; i < fileNames.length; i++) {
            String key = Integer.toString(i);
            String name = Base64.encodeToString(fileNames[i].getBytes(), 0);
            builder.appendQueryParameter(key, name);
        }
        return builder.build();
    }

    @Override
    public long filesLengthSum() {
        long sum = 0;
        for (String fileName : fileNames) {
            File file = new File(folder, fileName);
            sum += file.length();
        }
        return sum;
    }

    @Override
    public File[] getFiles() {
        File[] files = new File[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            files[i] = new File(folder, fileNames[i]);
        }
        return files;
    }

    @Override
    public String getZipFileName() {
        return zipFileName;
    }
}
