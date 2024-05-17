package emilyl9154.pinreminders;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import emilyl9154.pinreminders.databinding.FragmentFirstBinding;

import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.TimePicker;
import android.widget.DatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.AlarmManager;

// Other
import android.app.NotificationManager; // manager
import android.app.Notification; // builds notifications


public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private String notifContent;
    private String channelId = "main"; // can be anything
    Context context;
    NotificationManager notificationManager;

    // create notification channel to send notifications
    private void createTheNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= 33) {
            CharSequence name = "Notifications";
//          String description = "";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
//            channel.setDescription(description);
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendNotification(String bodyText) {
        Log.d("i","Start function"); // i = info tag
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setContentTitle("Reminder(s)")
                .setContentText(bodyText)
                .setOngoing(true) // make it pinned (will not work on android 14+)
                .build();

        Log.d("i","Made notification");
        notificationManager.notify(1, notification);
        Log.d("i","Notification sent");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createTheNotificationChannel();

        class NotificationBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                sendNotification(notifContent);
            }
        }

        // Variables for scheduling pinreminders
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        PendingIntent pendingIntent =
                PendingIntent.getService(context, 1, intent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }


        binding.pin.setOnClickListener(new View.OnClickListener() {
            // !!! call method sendNotification to send
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) getActivity().findViewById(R.id.textbox);
                notifContent = editText.getText().toString(); // body text of notification
                sendNotification(notifContent);

            }
        });


        final long[] timeInMilliseconds = {0};
        binding.schedule.setOnClickListener(new View.OnClickListener() {
            // !!! call method sendNotification to send
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) getActivity().findViewById(R.id.textbox);
                notifContent = editText.getText().toString(); // body text of notification

                // Pick date
                final Calendar c = Calendar.getInstance();
                int cYear = c.get(Calendar.YEAR);
                int cMonth = c.get(Calendar.MONTH);
                int cDay = c.get(Calendar.DAY_OF_MONTH);

                // Pick time
                int cHour = c.get(Calendar.HOUR_OF_DAY);
                int cMin = c.get(Calendar.MINUTE);

                final int[] selectedDay = {0};
                final int[] selectedMonth = {0};
                final int[] selectedYear = {0};

                TextView selectedDateTimeTxt = (TextView) getActivity().findViewById(R.id.reminder_sent_at_txt);
                TextView selectedDateTime = (TextView) getActivity().findViewById(R.id.selected_date_time);
                Button confirmBtn = (Button) getActivity().findViewById(R.id.confirm);


                TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hour, int min) {
                                // 1. Using Calendar
                                Calendar targetCal = Calendar.getInstance();
                                targetCal.set(Calendar.YEAR, selectedYear[0]);
                                targetCal.set(Calendar.MONTH, selectedMonth[0]);
                                targetCal.set(Calendar.DAY_OF_MONTH, selectedDay[0]);
                                targetCal.set(Calendar.HOUR_OF_DAY, hour);
                                targetCal.set(Calendar.MINUTE, min);

                                // 2. Get milliseconds
                                timeInMilliseconds[0] = targetCal.getTimeInMillis();

                                // Update UI (optional)
                                SimpleDateFormat format = new SimpleDateFormat("hh:mm a"); // hh:mm am/pm
                                String formatted12h = format.format(targetCal.getTime());
                                selectedDateTime.setText(selectedMonth[0] + "/" + selectedDay[0] + "/" + selectedYear[0] + " " + formatted12h);

                                selectedDateTimeTxt.setVisibility(View.VISIBLE);
                                selectedDateTime.setVisibility(View.VISIBLE);
                                confirmBtn.setVisibility(View.VISIBLE);
                            }
                        }, cHour, cMin, false); // false = will be 12h format (not working?)

                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                    new DatePickerDialog.OnDateSetListener(){
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int day){
                            selectedYear[0] = year;
                            selectedMonth[0] = month;
                            selectedDay[0] = day;
                            timePickerDialog.show();
                        }
                    }, cYear, cMonth, cDay);
                datePickerDialog.show();
            }
        });

        binding.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMilliseconds[0], pendingIntent);
                sendNotification(notifContent);
            }
        });






        //        ------------ Tinkering code ------------
//        binding.button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final TextView theText = (TextView) getActivity().findViewById(R.id.create_notif_title);
//
//                EditText editText = (EditText) getActivity().findViewById(R.id.textbox);
//                String newText = editText.getText().toString();
//
//                theText.setText(newText);
//
////               NavHostFragment.findNavController(FirstFragment.this)
////                       .navigate(R.id.action_FirstFragment_to_SecondFragment);
//            }
//        });
//        ------------ Tinkering code ------------
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
