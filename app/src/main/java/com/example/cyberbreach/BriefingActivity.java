package com.example.cyberbreach;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cyberbreach.databinding.ActivityBriefingBinding;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LevelStore;

public class BriefingActivity extends AppCompatActivity {

    private ActivityBriefingBinding binding;
    private Level level;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBriefingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        level = LevelStore.findById(getIntent().getStringExtra(IntentKeys.LEVEL_ID));
        mode = getIntent().getStringExtra(IntentKeys.MODE);
        if (mode == null) mode = "CHALLENGE";
        if (level == null) {
            finish();
            return;
        }

        binding.tvTitle.setText(level.title);
        binding.tvOrg.setText(level.org);
        binding.tvMode.setText("LEARN".equals(mode) ? R.string.mode_label_learn : R.string.mode_label_challenge);

        binding.webConcept.getSettings().setJavaScriptEnabled(false);
        binding.webConcept.setWebViewClient(new WebViewClient());
        binding.webConcept.loadUrl("file:///android_asset/concepts/" + level.concept + ".html");

        setupVideo();

        binding.btnLearnMore.setOnClickListener(v -> openUrl("https://owasp.org/www-community/"));
        binding.btnStart.setOnClickListener(v -> {
            Intent i = new Intent(this, RoomActivity.class);
            i.putExtra(IntentKeys.LEVEL_ID, level.id);
            i.putExtra(IntentKeys.MODE, mode);
            startActivity(i);
            finish();
        });
    }

    private void setupVideo() {
        int resId = getResources().getIdentifier("brief_" + level.concept, "raw", getPackageName());
        if (resId == 0) return;

        binding.videoExplainer.setVisibility(View.VISIBLE);
        binding.videoExplainer.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + resId));
        MediaController controller = new MediaController(this);
        controller.setAnchorView(binding.videoExplainer);
        binding.videoExplainer.setMediaController(controller);
        binding.videoExplainer.seekTo(1);   // show the first frame as a thumbnail
    }

    /** Implicit intent: let any installed browser handle the link. */
    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.toast_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.videoExplainer.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.webConcept.destroy();
    }
}
