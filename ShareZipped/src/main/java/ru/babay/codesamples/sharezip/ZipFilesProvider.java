package ru.babay.codesamples.sharezip;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * File provider intended to zip files on-the-fly.
 * It can send files (just like FileProvider) and zip files.
 * <p>
 * Use {@link ZipFilesProvider#getUriForFile(Context, String, File, String, String[])}
 * or  {@link ZipFilesProvider#getUriForFile(Context, String, String, File[])}
 * to create an URI.
 */
//@SuppressWarnings("ALL")
public class ZipFilesProvider extends FileProvider {

    static final String TAG = "ZipableFileProvider";

    /**
     * Just like {@link FileProvider#getUriForFile}, but will create an URI for zipping wile while sending
     * It will zip multiple files in one folder.
     *
     * @param context     - see {@link FileProvider#getUriForFile}
     * @param authority   - see {@link FileProvider#getUriForFile}
     * @param folder      - a folder where files reside
     * @param zipFileName - a zip file name
     * @param fileNames   - an array of file names
     * @return
     */
    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority,
                                    File folder, String zipFileName, String[] fileNames) {
        final PathStrategy strategy = getPathStrategy(context, authority);
        return new FilesInFolderZipUriInfo(folder, zipFileName, fileNames).toUri(strategy);
    }

    /**
     * Just like {@link FileProvider#getUriForFile}, but will create an URI for zipping wile while sending
     * It will zip multiple files in one folder.
     *
     * @param context     - see {@link FileProvider#getUriForFile}
     * @param authority   - see {@link FileProvider#getUriForFile}
     * @param zipFileName - a zip file name
     * @param files       - an array of files to zip
     * @return
     */
    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority,
                                    String zipFileName, File[] files) {
        final PathStrategy strategy = getPathStrategy(context, authority);
        return new ZipFilesUriInfo(zipFileName, files).toUri(strategy);
    }

    private static ParcelFileDescriptor startZippedPipe(MultiZipsUriInfo zipInfo) throws IOException {
        ParcelFileDescriptor[] pipes = Build.VERSION.SDK_INT >= 19 ?
                ParcelFileDescriptor.createReliablePipe() :
                ParcelFileDescriptor.createPipe();

        File[] files = zipInfo.getFiles();
        for (File file : files) {
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
            }
        }
        new ZipFilesTask(pipes[1], files).start();
        return pipes[0];
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        MultiZipsUriInfo zipInfo = parseUri(uri);

        try {
            return startZippedPipe(zipInfo);
        } catch (IOException e) {
            Log.e(TAG, "openFile: ", e);
        }
        // if some files do not exist -- let parent class handle that
        return super.openFile(uri, mode);
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        // ContentProvider has already checked granted permissions

        MultiZipsUriInfo info = parseUri(uri);

        if (projection == null) {
            projection = COLUMNS;
        }

        String[] cols = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i = 0;
        for (String col : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                cols[i] = OpenableColumns.DISPLAY_NAME;
                values[i++] = info.getZipFileName();
            } else if (OpenableColumns.SIZE.equals(col)) {
                // return size of original file; zip-file might differ
                cols[i] = OpenableColumns.SIZE;
                values[i++] = info.filesLengthSum();
            }
        }

        cols = copyOf(cols, i);
        values = copyOf(values, i);

        final MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(values);
        return cursor;
    }

    @Nullable
    private MultiZipsUriInfo parseUri(@NonNull Uri uri) {
        String type = uri.getQueryParameter(MultiZipsUriInfo.KEY_TYPE);
        if (ZipFilesUriInfo.TYPE.equals(type)) {
            return new ZipFilesUriInfo(mStrategy, uri);
        } else if (FilesInFolderZipUriInfo.TYPE.equals(type)) {
            return new FilesInFolderZipUriInfo(mStrategy, uri);
        } else
            return null;
    }
}
