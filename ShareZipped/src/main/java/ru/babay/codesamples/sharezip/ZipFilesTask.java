package ru.babay.codesamples.sharezip;

import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static ru.babay.codesamples.sharezip.ZipableFileProvider.TAG;

/**
 * zips and sends multiple files to a ParcelFileDescriptor writeFd
 * <p>
 * Note that some apps (like Telegram) receives the file at once.
 * Other apps (like Gmail) open the file you share, read some kb and close it,
 * and reopen it later (when you really send the email).
 * So, it's OK if "Broken pipe" exception thrown.
 */
public class ZipFilesTask {
    private final ParcelFileDescriptor writeFd;
    private final File[] files;
    private int writtenSize;

    /**
     * @param writeFd - file descriptor to write to
     * @param files   - source input files
     */
    public ZipFilesTask(ParcelFileDescriptor writeFd, File[] files) {
        this.writeFd = writeFd;
        this.files = files;
    }

    public void start() {
        new Thread(this::doZipFiles).start();
    }

    private void doZipFiles() {
        long start = System.currentTimeMillis();
        byte[] buf = new byte[8];

        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(writeFd.getFileDescriptor()))) {

            for (File file : files) {
                copyFileToStream(buf, file, zipStream);
            }

            zipStream.close();

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

    private void copyFileToStream(byte[] buf, File file, ZipOutputStream zipStream) throws IOException {
        FileInputStream iStream = new FileInputStream(file);

        zipStream.putNextEntry(new ZipEntry(file.getName()));
        int amount;
        while (0 <= (amount = iStream.read(buf))) {
            zipStream.write(buf, 0, amount);
            writtenSize += amount;
        }
        iStream.close();
        zipStream.closeEntry();
    }
}
