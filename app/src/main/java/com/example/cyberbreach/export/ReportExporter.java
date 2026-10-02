package com.example.cyberbreach.export;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class ReportExporter {
    private ReportExporter() { }

    private static final String FOLDER = "CyberBreach";

    /** Writes the report PDF to public Documents/CyberBreach and returns a shareable content Uri. */
    public static Uri exportPdf(Context context, ReportRenderer.Data data, String fileName)
            throws IOException {

        PdfDocument pdf = new PdfDocument();
        try {
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(
                    ReportRenderer.WIDTH, ReportRenderer.HEIGHT, 1).create();
            PdfDocument.Page page = pdf.startPage(info);
            ReportRenderer.draw(page.getCanvas(), data);
            pdf.finishPage(page);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return saveWithMediaStore(context, pdf, fileName);
            }
            return saveLegacy(context, pdf, fileName);
        } finally {
            pdf.close();
        }
    }

    // Android 10+ : scoped storage, no permission needed
    private static Uri saveWithMediaStore(Context context, PdfDocument pdf, String fileName)
            throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOCUMENTS + "/" + FOLDER);
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri uri = resolver.insert(collection, values);
        if (uri == null) throw new IOException("MediaStore insert failed");

        try (OutputStream out = resolver.openOutputStream(uri)) {
            if (out == null) throw new IOException("Cannot open output stream");
            pdf.writeTo(out);
        } catch (IOException e) {
            resolver.delete(uri, null, null);   // do not leave a broken pending file behind
            throw e;
        }

        ContentValues done = new ContentValues();
        done.put(MediaStore.MediaColumns.IS_PENDING, 0);
        resolver.update(uri, done, null, null);
        return uri;
    }

    // Android 9 and below : needs WRITE_EXTERNAL_STORAGE granted at runtime
    @SuppressWarnings("deprecation")
    private static Uri saveLegacy(Context context, PdfDocument pdf, String fileName)
            throws IOException {
        File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), FOLDER);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create " + dir);
        }
        File file = new File(dir, fileName);
        try (FileOutputStream out = new FileOutputStream(file)) {
            pdf.writeTo(out);
        }
        MediaScannerConnection.scanFile(context,
                new String[]{file.getAbsolutePath()}, new String[]{"application/pdf"}, null);
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }
}
