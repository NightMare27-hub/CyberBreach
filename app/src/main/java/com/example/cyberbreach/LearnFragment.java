package com.example.cyberbreach;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cyberbreach.data.LessonParser;
import com.example.cyberbreach.data.LessonRepository;
import com.example.cyberbreach.databinding.FragmentLearnBinding;

import java.util.ArrayList;
import java.util.List;

public class LearnFragment extends Fragment {

    private FragmentLearnBinding binding;
    private LessonRepository repository;
    private List<LessonParser.LessonData> currentLessons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLearnBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new LessonRepository(requireContext());

        binding.webLesson.getSettings().setJavaScriptEnabled(false);
        binding.webLesson.setBackgroundColor(0); // Transparent background
        binding.webLesson.setWebViewClient(new WebViewClient());

        setupDefaultSpinner();
        loadRemoteLessons();
    }

    private void setupDefaultSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.lesson_titles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spLesson.setAdapter(adapter);

        final String[] files = getResources().getStringArray(R.array.lesson_files);
        binding.spLesson.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (currentLessons.isEmpty()) {
                    binding.webLesson.loadUrl("file:///android_asset/lessons/" + files[position]);
                } else if (position < currentLessons.size()) {
                    LessonParser.LessonData data = currentLessons.get(position);
                    if (data.contentHtml != null && !data.contentHtml.isEmpty()) {
                        binding.webLesson.loadDataWithBaseURL(null, data.contentHtml, "text/html", "utf-8", null);
                    } else {
                        binding.webLesson.loadUrl("file:///android_asset/lessons/" + data.filename);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void loadRemoteLessons() {
        repository.refresh(new LessonRepository.Callback() {
            @Override
            public void onLoaded(List<LessonParser.LessonData> lessons, LessonRepository.Source source) {
                if (binding == null || lessons == null || lessons.isEmpty()) return;
                currentLessons = lessons;
                List<String> titles = new ArrayList<>();
                for (LessonParser.LessonData l : lessons) {
                    titles.add(l.title);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, titles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spLesson.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {
                // Fall back to default bundled lessons
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.webLesson.destroy();
        }
        binding = null;
    }
}
