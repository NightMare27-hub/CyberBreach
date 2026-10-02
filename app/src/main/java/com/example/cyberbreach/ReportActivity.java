package com.example.cyberbreach;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.databinding.ActivityReportBinding;
import com.example.cyberbreach.export.ReportExporter;
import com.example.cyberbreach.export.ReportRenderer;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LevelStore;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportActivity extends AppCompatActivity {

    private ActivityReportBinding binding;
    private ReportRenderer.Data data;
    private Uri exportedUri;
    private String fileName;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    // Must be registered before the activity reaches STARTED, so it is a field.
    private final ActivityResultLauncher<String> storagePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    export();
                } else {
                    Toast.makeText(this, R.string.perm_storage_denied, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent in = getIntent();
        Level level = LevelStore.findById(in.getStringExtra(IntentKeys.LEVEL_ID));
        if (level == null) {
            finish();
            return;
        }

        data = new ReportRenderer.Data();
        data.level = level;
        data.mode = "LEARN".equals(in.getStringExtra(IntentKeys.MODE)) ? "Learn" : "Challenge";
        data.stars = in.getIntExtra(IntentKeys.STARS, 0);
        data.timeSec = in.getIntExtra(IntentKeys.TIME_SEC, 0);
        data.hints = in.getIntExtra(IntentKeys.HINTS, 0);
        data.wrong = in.getIntExtra(IntentKeys.WRONG, 0);
        data.rank = DbHelper.rankForStars(DbHelper.get(this).getTotalStars());
        data.date = new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());
        fileName = "IncidentReport_" + level.id + "_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".pdf";

        Bitmap preview = Bitmap.createBitmap(
                ReportRenderer.WIDTH, ReportRenderer.HEIGHT, Bitmap.Config.ARGB_8888);
        ReportRenderer.draw(new Canvas(preview), data);
        binding.ivPreview.setImageBitmap(preview);

        binding.btnExport.setOnClickListener(v -> onExportClicked());
        binding.btnShareReport.setOnClickListener(v -> shareReport());
    }

    private void onExportClicked() {
        boolean needsPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

        if (!needsPermission) {
            export();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.perm_storage_title)
                    .setMessage(R.string.perm_storage_message)
                    .setPositiveButton(R.string.perm_continue,
                            (d, w) -> storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        } else {
            storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void export() {
        binding.btnExport.setEnabled(false);
        io.execute(() -> {
            try {
                Uri uri = ReportExporter.exportPdf(this, data, fileName);
                runOnUiThread(() -> {
                    exportedUri = uri;
                    binding.tvExportStatus.setText(getString(R.string.report_exported, fileName));
                    binding.btnShareReport.setEnabled(true);
                    binding.btnExport.setEnabled(true);
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.toast_export_failed, Toast.LENGTH_LONG).show();
                    binding.btnExport.setEnabled(true);
                });
            }
        });
    }

    /** Implicit intent with a temporary read grant on the exported file. */
    private void shareReport() {
        if (exportedUri == null) return;
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("application/pdf");
        send.putExtra(Intent.EXTRA_STREAM, exportedUri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(send, getString(R.string.share_report_chooser)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }
}
