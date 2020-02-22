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

import ru.babay.codesamples.sharezip.ZipableFileProvider;

public class MainActivity extends AppCompatActivity {

    private final String FILE_NAME = "example.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View btn = findViewById(R.id.doShareZip);
        btn.setOnClickListener(this::doShareZip);
    }

    private void doShareZip(View view) {
        File file = getFile();
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

    private File getFile() {
        File dir = getExternalCacheDir();
        File file = new File(dir, FILE_NAME);
        if (!file.exists()) {
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
