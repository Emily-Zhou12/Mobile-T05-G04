package com.example.g04_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Create_joinGame_page extends AppCompatActivity {
    private Button backButton;
    private Button filterButton;
    private CheckBox locationCheckBox;
    private CheckBox gameModeCheckBox;
    private LinearLayout locationOptions;
    private LinearLayout gameModeOptions;
    private CheckBox location1CheckBox;
    private CheckBox location2CheckBox;
    private CheckBox location3CheckBox;
    private CheckBox mode1CheckBox;
    private CheckBox mode2CheckBox;
    private TextView emptyMessageTextView;
    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;
    private ConcurrentHashMap<String, RoomInformation> rooms;
    private List<RoomInformation> targetRooms;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_joingame_page);

        backButton = findViewById(R.id.backButton);
        filterButton = findViewById(R.id.filterButton);
        locationCheckBox = findViewById(R.id.locationCheckBox);
        gameModeCheckBox = findViewById(R.id.gameModeCheckBox);
        locationOptions = findViewById(R.id.locationOptions);
        gameModeOptions = findViewById(R.id.gameModeOptions);
        location1CheckBox = findViewById(R.id.location1);
        location2CheckBox = findViewById(R.id.location2);
        location3CheckBox = findViewById(R.id.location3);
        mode1CheckBox = findViewById(R.id.mode1);
        mode2CheckBox = findViewById(R.id.mode2);
        emptyMessageTextView = findViewById(R.id.emptyMessageTextView);
        roomsRecyclerView = findViewById(R.id.roomsRecyclerview);
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchView);

        rooms = new ConcurrentHashMap<>();
        targetRooms = new ArrayList<>(rooms.values());
        //TODO: get rooms

        if (!rooms.isEmpty()) {
            roomsRecyclerView.setVisibility(View.VISIBLE);
            emptyMessageTextView.setVisibility(View.GONE);
        }

        roomAdapter = new RoomAdapter(targetRooms);
        roomsRecyclerView.setAdapter(roomAdapter);

        // Actions clicking on back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Actions clicking on filter button
        filterButton.setOnClickListener(v -> {
            if (locationCheckBox.getVisibility() == View.GONE) {
                locationCheckBox.setVisibility(View.VISIBLE);
                gameModeCheckBox.setVisibility(View.VISIBLE);
            }else {
                locationCheckBox.setChecked(false);
                gameModeCheckBox.setChecked(false);
                locationCheckBox.setVisibility(View.GONE);
                gameModeCheckBox.setVisibility(View.GONE);
            }
        });

        // Actions clicking on checkBoxes
        locationCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                locationOptions.setVisibility(View.VISIBLE);
            } else {
                location1CheckBox.setChecked(false);
                location2CheckBox.setChecked(false);
                location3CheckBox.setChecked(false);
                locationOptions.setVisibility(View.GONE);
            }
        });

        gameModeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                gameModeOptions.setVisibility(View.VISIBLE);
            } else {
                mode1CheckBox.setChecked(false);
                mode1CheckBox.setChecked(false);
                gameModeOptions.setVisibility(View.GONE);
            }
        });

        // Action clicking on option buttons
        CompoundButton.OnCheckedChangeListener filterListener = (buttonView, isChecked) -> {
            applyFilters();
        };
        location1CheckBox.setOnCheckedChangeListener(filterListener);
        location2CheckBox.setOnCheckedChangeListener(filterListener);
        location3CheckBox.setOnCheckedChangeListener(filterListener);
        mode1CheckBox.setOnCheckedChangeListener(filterListener);
        mode2CheckBox.setOnCheckedChangeListener(filterListener);

        // Actions entering and submitting id in searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String id) {
                searchRoom(id);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }

    // Apply filters
    private void applyFilters() {
        targetRooms.clear();

        // Check for each button
        boolean isLocation1Checked = location1CheckBox.isChecked();
        boolean isLocation2Checked = location2CheckBox.isChecked();
        boolean isLocation3Checked = location3CheckBox.isChecked();
        boolean isMode1Checked = mode1CheckBox.isChecked();
        boolean isMode2Checked = mode2CheckBox.isChecked();

        // Check if any option button is checked
        boolean isAnyLocationChecked = isLocation1Checked
                || isLocation2Checked
                || isLocation3Checked;
        boolean isAnyModeChecked = isMode1Checked || isMode2Checked;

        // Display all rooms if no option is checked
        if (!isAnyLocationChecked && !isAnyModeChecked) {
            targetRooms.addAll(rooms.values());
            roomAdapter.updateDisplayedRooms(targetRooms);
            return;
        }

        // Check if each room meets the conditions
        for (RoomInformation room : rooms.values()) {

            // Ignore the category if not checked
            boolean matchesLocation = !isAnyLocationChecked;
            boolean matchesMode = !isAnyModeChecked;

            // Check location
            if (isAnyLocationChecked) {
                matchesLocation = (room.getLocationName().equals("Location 1") && isLocation1Checked)
                        || (room.getLocationName().equals("Location 2") && isLocation2Checked)
                        || (room.getLocationName().equals("Location 3") && isLocation3Checked);
            }

            // Check game mode
            if (isAnyModeChecked) {
                matchesMode = (room.getModeName().equals("Mode 1") && isMode1Checked)
                        || (room.getModeName().equals("Mode 2") && isMode2Checked);
            }

            // Add the satisfied room to the displayed list
            if (matchesLocation && matchesMode) {
                targetRooms.add(room);
            }

        }
        roomAdapter.updateDisplayedRooms(targetRooms);
    }

    // Search and display the room
    private void searchRoom(String id) {
        targetRooms.clear();
        boolean roomFound = rooms.computeIfPresent(id, (key, room) -> {
            targetRooms.add(room);
            roomAdapter.updateDisplayedRooms(targetRooms);
            return room;
        }) != null;

        if (!roomFound) {
            Toast.makeText(this, "Room not found", Toast.LENGTH_SHORT).show();
        }
    }

}