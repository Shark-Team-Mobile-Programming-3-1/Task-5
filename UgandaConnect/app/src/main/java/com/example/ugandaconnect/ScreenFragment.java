package com.example.ugandaconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// One fragment is used for all screens. It shows a long list so the top bar can collapse.
public class ScreenFragment extends Fragment {

    private static final String ARG_KEY = "screen_key";

    private String screenKey = MainActivity.HOME;
    private ItemAdapter adapter;

    // Create the fragment and give it the name of the screen
    public static ScreenFragment newInstance(String key) {
        ScreenFragment fragment = new ScreenFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            screenKey = getArguments().getString(ARG_KEY, MainActivity.HOME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ItemAdapter(buildRows(), row -> {
            if (row.target != null && isAdded()) {
                ((MainActivity) requireActivity()).openScreen(row.target);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    // Called by MainActivity when the user types in the search box
    public void filter(String text) {
        if (adapter != null) {
            adapter.filter(text);
        }
    }

    // Make the fake data for each screen
    private List<ItemAdapter.Row> buildRows() {
        List<ItemAdapter.Row> rows = new ArrayList<>();

        switch (screenKey) {
            case MainActivity.RESULTS: {
                // 60 fake UNEB results
                String[] names = {"Akello Grace", "Okello Peter", "Nakato Sarah", "Mugisha John",
                        "Namukasa Ruth", "Ssemakula David", "Atim Joy", "Opio Samuel",
                        "Kiiza Brian", "Nambi Esther"};
                for (int resultsIndex = 0; resultsIndex < 60; resultsIndex++) {
                    String name = names[resultsIndex % names.length] + " #" + (resultsIndex + 1);
                    String index = String.format(Locale.US, "U%04d/%03d", 1000 + resultsIndex * 7, 100 + resultsIndex);
                    String subtitle = "Index: " + index + " - Division " + (1 + resultsIndex % 4);
                    rows.add(new ItemAdapter.Row(name, subtitle, null));
                }
                break;
            }
            case MainActivity.HOME: {
                rows.add(new ItemAdapter.Row("Solar Subsidies",
                        "Apply via the Rural Electrification Agency", MainActivity.SOLAR));
                rows.add(new ItemAdapter.Row("Healthcare",
                        "Book appointments, see lab results", MainActivity.HEALTH));
                rows.add(new ItemAdapter.Row("My Results",
                        "UCE / UACE results and school term dates", MainActivity.RESULTS));
                rows.add(new ItemAdapter.Row("Bill Payments",
                        "Pay UMEME and NWSC, report issues to KCCA", MainActivity.BILLS));
                for (int homeIndex = 1; homeIndex <= 25; homeIndex++) {
                    rows.add(new ItemAdapter.Row("Announcement #" + homeIndex,
                            "Sample public notice for scrolling", null));
                }
                break;
            }
            case MainActivity.SERVICES: {
                rows.add(new ItemAdapter.Row("Solar Subsidies",
                        "Apply via the Rural Electrification Agency", MainActivity.SOLAR));
                rows.add(new ItemAdapter.Row("Healthcare",
                        "Book appointments, see lab results", MainActivity.HEALTH));
                rows.add(new ItemAdapter.Row("My Results",
                        "UCE / UACE results and school term dates", MainActivity.RESULTS));
                rows.add(new ItemAdapter.Row("Bill Payments",
                        "Pay UMEME and NWSC, report issues to KCCA", MainActivity.BILLS));
                break;
            }
            default: {
                // Bookmarks, Profile, Solar, Health, Bills: simple sample rows
                String title = MainActivity.getTitleFor(screenKey);
                for (int itemIndex = 1; itemIndex <= 30; itemIndex++) {
                    rows.add(new ItemAdapter.Row(title + " item " + itemIndex,
                            "Sample text for scrolling", null));
                }
                break;
            }
        }
        return rows;
    }
}
