package com.example.todolist.TaskOperations;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.todolist.Activity.MainActivity;
import com.example.todolist.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.example.todolist.Model.ToDoModel;
import com.example.todolist.Utils.DatabaseHandler;

import java.util.Calendar;
import java.util.Locale;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private EditText newTitleText;
    private EditText newCategory;
    private EditText newTaskText;
    private Button newTaskSaveButton;
    private Button newDate;
    private Button newTime;
    private Switch switchButton;
    private boolean switchChecked;
    private int year, month, day;
    private int hour, minute;
    private boolean pickedTime = false;
    private boolean pickedDate = false;

    private DatabaseHandler db;

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.new_task, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newTitleText = requireView().findViewById(R.id.newTitleText);
        newCategory = requireView().findViewById(R.id.newCategory);
        newTaskText = requireView().findViewById(R.id.newTaskText);
        newDate = requireView().findViewById(R.id.newDate);
        newTime = requireView().findViewById(R.id.newTime);
        switchButton = requireView().findViewById(R.id.switchButton);
        newTaskSaveButton = getView().findViewById(R.id.newTaskButton);

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if(bundle != null){
            isUpdate = true;
            String taskTitle = bundle.getString("taskTitle");
            String category = bundle.getString("category");
            String task = bundle.getString("task");
            String date = bundle.getString("date");
            String time = bundle.getString("time");
            newTaskText.setText(task);
            newTitleText.setText(taskTitle);
            newCategory.setText(category);
            newDate.setText(date);
            newTime.setText(time);
            assert task != null;
            if(task.length()>0)
                newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary_dark));
        }

        db = new DatabaseHandler(getActivity());
        db.openDatabase();

        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                }
                else {
                    if(!newTitleText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime) {
                        newTaskSaveButton.setEnabled(true);
                        newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary_dark));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        newTitleText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                }
                else {
                    if(!newCategory.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && pickedDate && pickedTime) {
                        newTaskSaveButton.setEnabled(true);
                        newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary_dark));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        newCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("")){
                    newTaskSaveButton.setEnabled(false);
                    newTaskSaveButton.setTextColor(Color.GRAY);
                }
                else {
                    if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && pickedDate && pickedTime) {
                        newTaskSaveButton.setEnabled(true);
                        newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary_dark));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = newTaskText.getText().toString();
                String textTitle = newTitleText.getText().toString();
                String category = newCategory.getText().toString();
                String date = newDate.getText().toString();
                String time = newTime.getText().toString();
                if(finalIsUpdate){
                    db.updateTask(bundle.getInt("id"), text);
                    db.updateTaskTitle(bundle.getInt("id"), textTitle);
                    db.updateCategory(bundle.getInt("id"), category);
                    db.updateDate(bundle.getInt("id"), date);
                    db.updateTime(bundle.getInt("id"), time);
                    if(switchChecked) {
                        ToDoModel task = new ToDoModel();
                        task.setTaskTitle(textTitle);
                        task.setCategory(category);
                        task.setTask(text);
                        task.setDate(date);
                        task.setTime(time);
                        task.setStatus(0);
                        ((MainActivity) requireActivity()).setAlarm(task);
                    }
                }
                else {
                    ToDoModel task = new ToDoModel();
                    task.setTaskTitle(textTitle);
                    task.setCategory(category);
                    task.setTask(text);
                    task.setDate(date);
                    task.setTime(time);
                    task.setStatus(0);
                    db.insertTask(task);
                    if(switchChecked) {
                        ((MainActivity) requireActivity()).setAlarm(task);
                    }
                }
                dismiss();
            }
        });

        newDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popDatePicker(view);
            }
        });

        newTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popTimePicker(view);
            }
        });

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    switchChecked = true;
                    if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime)
                        newTaskSaveButton.setEnabled(true);
                } else {
                    switchChecked = false;
                    if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime)
                        newTaskSaveButton.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog){
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if(activity instanceof DialogCloseListener)
            ((DialogCloseListener)activity).handleDialogClose(dialog);
    }

    public void popDatePicker(View view) {
        DatePickerDialog.OnDateSetListener onDateSetListener = (datePicker, selectedYear, selectedMonth, selectedDay) -> {
            year = selectedYear;
            month = selectedMonth + 1;
            day = selectedDay;
            newDate.setText(String.format(Locale.getDefault(), "%02d.%02d.%4d",day ,month ,year));
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), onDateSetListener, year, month, day);

        datePickerDialog.setTitle("Select date");
        datePickerDialog.show();

        pickedDate = true;
        if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime) {
            newTaskSaveButton.setEnabled(true);
            newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary_dark));
        }

    }

    public void popTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
            hour = selectedHour;
            minute = selectedMinute;
            newTime.setText(String.format(Locale.getDefault(),"%02d:%02d",hour ,minute));
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), onTimeSetListener, hour, minute, true);

        timePickerDialog.setTitle("Select time");
        timePickerDialog.show();

        pickedTime = true;
        if(!newTitleText.getText().toString().isEmpty() && !newTaskText.getText().toString().isEmpty() && !newCategory.getText().toString().isEmpty() && pickedDate && pickedTime) {
            newTaskSaveButton.setEnabled(true);
            newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary_dark));
        }
    }
}
