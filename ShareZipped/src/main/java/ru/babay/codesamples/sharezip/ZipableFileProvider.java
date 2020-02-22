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
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File provider intended to zip files on-the-fly.
 * It can send files (just like FileProvider) and zip files.
 * <p>
 * Use {@link ZipableFileProvider#getUriForFile(Context, String, File, boolean)}
 * to create an URI.
 */
//@SuppressWarnings("ALL")
public class ZipableFileProvider extends FileProvider {

    static final String TAG = "ZipableFileProvider";

    /**
     * Just like {@link FileProvider#getUriForFile}, but will create an URI for zipping wile while sending
     *
     * @param context - see {@link FileProvider#getUriForFile}
     * @param authority - see {@link FileProvider#getUriForFile}
     * @param file - see {@link FileProvider#getUriForFile}
     * @param zipFile - true to make zipped file uri, false for regular file uri
     * @return - see {@link FileProvider#getUriForFile}
     */

    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority,
                                    @NonNull File file, boolean zipFile) {
        Uri uri = getUriForFile(context, authority, file);
        if (zipFile) {
            return new Uri.Builder()
                    .scheme(uri.getScheme())
                    .authority(uri.getAuthority())
                    .encodedPath(uri.getPath())
                    .encodedQuery("zip").build();
        }
        return uri;
    }

    public static ParcelFileDescriptor startZippedPipe(File file) throws IOException {
        ParcelFileDescriptor[] pipes = Build.VERSION.SDK_INT >= 19 ?
                ParcelFileDescriptor.createReliablePipe() :
                ParcelFileDescriptor.createPipe();
        new Thread(() -> doZipFile(pipes[1], file)).start();
        return pipes[0];
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static ParcelFileDescriptor startZippedSocketPair(File file) throws IOException {
        ParcelFileDescriptor[] pipes = ParcelFileDescriptor.createReliableSocketPair();
        new Thread(() -> doZipFile(pipes[1], file)).start();
        return pipes[0];
    }

    /**
     * zips and sends a file to a ParcelFileDescriptor writeFd
     * <p>
     * Note that some apps (like Telegram) receives the file at once.
     * Other apps (like Gmail) open the file you share, read some kb and close it,
     * and reopen it later (when you really send the email).
     * So, it's OK if "Broken pipe" exception thrown.
     *
     * @param writeFd - file descriptor to write to
     * @param inputFile - source input file
     */
    private static void doZipFile(ParcelFileDescriptor writeFd, File inputFile) {
        long start = System.currentTimeMillis();
        byte[] buf = new byte[1024];
        int writtenSize = 0;
        try (FileInputStream iStream = new FileInputStream(inputFile);
             ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(writeFd.getFileDescriptor()))) {

            zipStream.putNextEntry(new ZipEntry(inputFile.getName()));
            int amount;
            while (0 <= (amount = iStream.read(buf))) {
                zipStream.write(buf, 0, amount);
                writtenSize += amount;
            }

            zipStream.closeEntry();
            zipStream.close();
            iStream.close();
            writeFd.close();

            if (BuildConfig.DEBUG)
                Log.d(TAG, "doZipFile: done. it took ms: " + (System.currentTimeMillis() - start));
        } catch (IOException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    writeFd.closeWithError(e.getMessage());
                } catch (IOException e1) {
                    Log.e(TAG, "doZipFile: ", e1);
                }
            }
            if (BuildConfig.DEBUG)
                Log.d(TAG, "doZipFile: written: " + writtenSize, e);
        }
    }

    public static String getExportFileName(File file, boolean isZip) {
        if (isZip) {
            return removeExt(file.getName()) + ".zip";
        } else {
            return file.getName();
        }
    }

    private static String removeExt(String fileName) {
        int pos = fileName.indexOf('.');
        if (pos < 0)
            return fileName;
        return fileName.substring(0, pos);
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        File file = getFileForUri(uri);
        // if file does not exist -- let parent class handle that
        if (file.exists() && isZip(uri)) {
            if (file.exists()) {
                try {
                    return startZippedPipe(file);
                } catch (IOException e) {
                    Log.e(TAG, "openFile: ", e);
                }
            }
        }
        return super.openFile(uri, mode);
    }

    private boolean isZip(@NonNull Uri uri) {
        return "zip".equals(uri.getQuery());
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        // ContentProvider has already checked granted permissions
        File file = mStrategy.getFileForUri(uri);

        if (projection == null) {
            projection = COLUMNS;
        }

        String[] cols = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i = 0;
        for (String col : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                cols[i] = OpenableColumns.DISPLAY_NAME;
                values[i++] = file.getName() + (isZip(uri) ? ".zip" : "");
            } else if (OpenableColumns.SIZE.equals(col)) {
                // return size of original file; zip-file might differ
                cols[i] = OpenableColumns.SIZE;
                values[i++] = file.length();
            }
        }

        cols = copyOf(cols, i);
        values = copyOf(values, i);

        final MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(values);
        return cursor;
    }
}
