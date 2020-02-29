package ru.babay.sharedzipexample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.babay.codesamples.sharezip.ZipFilesProvider;
import ru.babay.codesamples.sharezip.ZipableFileProvider;

public class MainActivity extends AppCompatActivity {

    private final String FILE_NAME = "example.jpg";
    private final String FILE2_NAME = "../files/example2.jpg";
    private final String FILE3_NAME = "example2.jpg";

    private int method = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.doShareZip).setOnClickListener(this::doShareZip);
        findViewById(R.id.doShare2Zip).setOnClickListener(this::doShare2Zip);

    }

    private void doShareZip(View view) {
        File file = getFile(FILE_NAME);
        Uri uri = ZipableFileProvider.getUriForFile(this, getPackageName() + ".provider", file, true);

        String name = ZipableFileProvider.getExportFileName(file, true);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("application/zip");

        sendIntent.putExtra(Intent.EXTRA_SUBJECT, name);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Example share zip on the fly");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooserIntent = Intent.createChooser(sendIntent, "Example share");
        startActivity(chooserIntent);
    }

    private void doShare2Zip(View view) {
        File f1 = getFile(FILE_NAME);
        File f2 = getFile(FILE2_NAME);
        File f3 = getFile(FILE3_NAME);
        String zipName = "someFiles3.zip";

        Uri uri;
        switch (method) {
            case 0:
                String[] names = new String[]{f1.getName(), f3.getName()};
                uri = ZipFilesProvider.getUriForFile(this, getPackageName() + ".provider2", f1.getParentFile(), zipName, names);
                break;

            case 1:
                uri = ZipFilesProvider.getUriForFile(this, getPackageName() + ".provider2", zipName, new File[]{f1, f2});
                break;

            case 2:
                uri = ZipFilesProvider.getUriForFile2(this, getPackageName() + ".provider2", zipName, new File[]{f1, f2});
                break;

            default:
                throw new RuntimeException("Not implemented for method: " + method);
        }

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("application/zip");

        sendIntent.putExtra(Intent.EXTRA_SUBJECT, zipName);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Example share zip on the fly2");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooserIntent = Intent.createChooser(sendIntent, "Example share");
        startActivity(chooserIntent);
    }

    private File getFile(String name) {
        File dir = getExternalCacheDir();
        File file = new File(dir, name);
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            copyFromAssets(file);
        }
        return file;
    }

    private void copyFromAssets(File target) {
        byte[] buf = new byte[1024];
        int amount;
        try (InputStream istream = getAssets().open("example.jpeg");
             OutputStream ostream = new FileOutputStream(target)) {
            while ((amount = istream.read(buf)) > 0) {
                ostream.write(buf, 0, amount);
            }
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "copyFromAssets: ", e);
        }
    }
}
