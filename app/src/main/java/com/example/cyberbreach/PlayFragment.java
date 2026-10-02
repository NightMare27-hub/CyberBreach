package com.example.cyberbreach;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cyberbreach.data.DbHelper;
import com.example.cyberbreach.data.LevelRepository;
import com.example.cyberbreach.databinding.FragmentPlayBinding;
import com.example.cyberbreach.model.Level;
import com.example.cyberbreach.model.LevelStore;

import java.util.List;

public class PlayFragment extends Fragment {

    private FragmentPlayBinding binding;
    private LevelAdapter adapter;
    private LevelRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = new LevelRepository(requireContext());

        ArrayAdapter<CharSequence> modeAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.mode_array, android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spMode.setAdapter(modeAdapter);

        adapter = new LevelAdapter(LevelStore.getLevels(), this::openLevel);
        binding.rvLevels.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLevels.setAdapter(adapter);

        loadLevels();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setProgress(DbHelper.get(requireContext()).getAllProgress());
        }
    }

    private void loadLevels() {
        binding.progressLoading.setVisibility(View.VISIBLE);
        repository.refresh(new LevelRepository.Callback() {
            @Override
            public void onLoaded(List<Level> levels, LevelRepository.Source source) {
                if (binding == null) return;   // the view was destroyed while loading
                LevelStore.set(levels);
                adapter.setLevels(levels);
                binding.progressLoading.setVisibility(View.GONE);
                binding.tvSource.setText(sourceLabel(source));
            }

            @Override
            public void onError(String message) {
                if (binding == null) return;
                binding.progressLoading.setVisibility(View.GONE);
                binding.tvSource.setText(R.string.source_error);
            }
        });
    }

    private int sourceLabel(LevelRepository.Source source) {
        switch (source) {
            case REMOTE:
                return R.string.source_remote;
            case CACHE:
                return R.string.source_cache;
            default:
                return R.string.source_bundled;
        }
    }

    private void openLevel(Level level) {
        Intent i = new Intent(requireContext(), BriefingActivity.class);
        i.putExtra(IntentKeys.LEVEL_ID, level.id);
        i.putExtra(IntentKeys.MODE,
                binding.spMode.getSelectedItemPosition() == 0 ? "LEARN" : "CHALLENGE");
        startActivity(i);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
