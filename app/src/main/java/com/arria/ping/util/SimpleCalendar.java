package com.arria.ping.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;


import com.arria.ping.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;



public class SimpleCalendar extends LinearLayout {

    private static final String CUSTOM_GREY = "#000000";
    private static final String CUSTOM_WHITE = "#FFFFFF";
    private static final String CUSTOM_RED = "#ff0000";
    private static final String LIGHT_GRAY = "#F5F5F5";
    private static final String PINK = "#FF4081";
    private static final String MONTH_DATE_COLOR = "#000000";
    private static final String[] ENG_MONTH_NAMES = {"January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"};
    List<String> daysArr = Arrays.asList("M", "T", "W", "T", "F", "S", "S", "M", "T", "W", "T", "F", "S", "S");
    private TextView currentDate;
    private TextView currentMonth;
    private Button selectedDayButton;
    // private Button[] days;
    private TextView[] days;
    //private Button[] daysName;
    private TextView[] daysName;
    LinearLayout weekOneLayout;
    LinearLayout weekTwoLayout;
    LinearLayout weekThreeLayout;
    LinearLayout weekFourLayout;
    LinearLayout weekFiveLayout;
    LinearLayout weekSixLayout;
    LinearLayout daysArray;
    private LinearLayout[] weeks;

    private int currentDateDay, chosenDateDay, currentDateMonth,
            chosenDateMonth, currentDateYear, chosenDateYear,
            pickedDateDay, pickedDateMonth, pickedDateYear;
    int userMonth, userYear;
    private DayClickListener mListener;
    private Drawable userDrawable;

    private Calendar calendar;
    LayoutParams defaultButtonParams;
    private LayoutParams userButtonParams;

    public SimpleCalendar(Context context) {
        super(context);
        init(context);
    }

    public SimpleCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SimpleCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SimpleCalendar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        isWeekend();

        // getAllWeekEndsOfTheYear();
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        View view = LayoutInflater.from(context).inflate(R.layout.simple_calendar, this, true);
        calendar = Calendar.getInstance();

        weekOneLayout = (LinearLayout) view.findViewById(R.id.calendar_week_1);
        weekTwoLayout = (LinearLayout) view.findViewById(R.id.calendar_week_2);
        weekThreeLayout = (LinearLayout) view.findViewById(R.id.calendar_week_3);
        weekFourLayout = (LinearLayout) view.findViewById(R.id.calendar_week_4);
        weekFiveLayout = (LinearLayout) view.findViewById(R.id.calendar_week_5);
        weekSixLayout = (LinearLayout) view.findViewById(R.id.calendar_week_6);
        daysArray = (LinearLayout) view.findViewById(R.id.days_array);
        currentDate = (TextView) view.findViewById(R.id.current_date);
        currentMonth = (TextView) view.findViewById(R.id.current_month);

        currentDateDay = chosenDateDay = calendar.get(Calendar.DAY_OF_MONTH);

        if (userMonth != 0 && userYear != 0) {
            currentDateMonth = chosenDateMonth = userMonth;
            currentDateYear = chosenDateYear = userYear;
        } else {
            currentDateMonth = chosenDateMonth = calendar.get(Calendar.MONTH);
            currentDateYear = chosenDateYear = calendar.get(Calendar.YEAR);
        }

        currentDate.setText("" + currentDateDay);
        currentMonth.setText(ENG_MONTH_NAMES[currentDateMonth]);

        initializeDaysWeeks();
        if (userButtonParams != null) {
            defaultButtonParams = userButtonParams;
        } else {
            defaultButtonParams = getdaysLayoutParams();
        }
        addDaysNameInCalendar(defaultButtonParams, context, metrics);
        addDaysinCalendar(defaultButtonParams, context, metrics);

        initCalendarWithDate(chosenDateYear, chosenDateMonth, chosenDateDay);

    }

    private void addDaysNameInCalendar(LayoutParams defaultButtonParams, Context context, DisplayMetrics metrics) {
        int engDaysArrayCounter = 0;


        for (int i = 0; i < daysArr.size(); ++i) {
            final TextView day = new TextView(context);
            day.setTextColor(Color.parseColor(CUSTOM_GREY));

            day.setLayoutParams(defaultButtonParams);

            //day.setTextSize((int) metrics.density * 6);
            day.setTextSize(14);
            day.setPadding(0, 20, 0, 20);
            day.setGravity(Gravity.CENTER);
            day.setSingleLine();
            if (daysArr.get(i).equals("S")) {
                day.setBackgroundColor(Color.parseColor(LIGHT_GRAY));


            } else {
                day.setBackgroundColor(Color.TRANSPARENT);
            }
            day.setText(daysArr.get(i));
            day.setTypeface(null, Typeface.BOLD);
            daysName[engDaysArrayCounter] = day;
            daysArray.addView(day);
            ++engDaysArrayCounter;
        }



    }

    private void initializeDaysWeeks() {



        weeks = new LinearLayout[3];
        days = new TextView[3 * 14];
        daysName = new TextView[14];
        weeks[0] = weekOneLayout;
        weeks[1] = weekTwoLayout;
        weeks[2] = weekThreeLayout;

    }

    List<Integer> disable = new ArrayList<>();
    List<Integer> nextMonthDisable = new ArrayList<>();
    List<Integer> previousMonthDisabled = new ArrayList<>();

    void getAllWeekEndsOfTheYear() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Calendar cal = new GregorianCalendar(year, Calendar.JANUARY, 1);

        for (int i = 0, inc = 1; i < 366 && cal.get(Calendar.YEAR) == year; i += inc) {
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                // this is a sunday
                disable.add(cal.getTime().getDate());

                cal.add(Calendar.DAY_OF_MONTH, 7);

                inc = 7;
            } else {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

    }


    void isWeekend() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int month = cal.get(Calendar.MONTH);
        Log.e(" MONTH", month + "");
        do {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                Log.e("CHECKINGG", dayOfWeek + "");
                //disable.add(cal.getTime());
                disable.add(cal.getTime().getDate());
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        } while (cal.get(Calendar.MONTH) == month);
        nextMonthWeekends();


    }


    void previousMonthWeekend() {

        Calendar calendar = Calendar.getInstance();

        // Note that month is 0-based in calendar, bizarrely.
        Log.e("Previos MONTH", calendar.get(Calendar.MONTH) + "");
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        if (month == 11) {
            calendar.set(year + 1, 0, 1);
        } else if (month > 0) {
            calendar.set(year, month - 1, 1);
        }

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int count = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            if (month == 11) {
                calendar.set(year + 1, 0, day);
            } else if (month > 0) {

                Log.e("HMM", month + "");
                calendar.set(year, month - 1, day);
            }
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
                previousMonthDisabled.add(calendar.getTime().getDate());
                count++;
            }
        }

        for (int i = 0; i < previousMonthDisabled.size() - 2; i++) {
            previousMonthDisabled.remove(i);
        }
    }


    void nextMonthWeekends() {

        Calendar calendar = Calendar.getInstance();

        Log.e("NEXT MONTH", calendar.get(Calendar.MONTH) + "");
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        if (month == 11) {
            calendar.set(year + 1, 0, 1);
        } else {
            calendar.set(year, month + 1, 1);
        }

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int count = 0;

        for (int day = 1; day <= daysInMonth; day++) {
            /*calendar.set(2021, 11, day);*/
            if (month == 11) {
                calendar.set(year + 1, 0, day);
            } else {

                Log.e("HMM", month + "");
                calendar.set(year, month + 1, day);
            }
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
                nextMonthDisable.add(calendar.getTime().getDate());
                count++;
                // Or do whatever you need to with the result.
            }
        }
        previousMonthWeekend();


    }

    float dpToPx(int margin) {
        return margin * getResources().getDisplayMetrics().density;
    }

    private void initCalendarWithDate(int year, int month, int day) {
        if (calendar == null)
            calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        int daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); // total Days in the month
        Log.e("daysInCurrentMonth", daysInCurrentMonth + "");
        chosenDateYear = year; // Current year
        chosenDateMonth = month; // Month
        chosenDateDay = day; //  current date
        Log.e("chosenDateYear", chosenDateYear + "");
        Log.e("chosenDateMonth", chosenDateMonth + "");
        Log.e("chosenDateDay", chosenDateDay + "");
        calendar.set(year, month, 1);
        int firstDayOfCurrentMonth = calendar.get(Calendar.DAY_OF_WEEK) - 2; // S, M ,Tue ,wed
        if(firstDayOfCurrentMonth == -1){
            firstDayOfCurrentMonth = 1;
        }
        Log.e("firstDayOfCurrentMonth", firstDayOfCurrentMonth + "");
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        int dayNumber = 1;
        int daysLeftInFirstWeek = 0;
        int indexOfDayAfterLastDayOfMonth = 0;

        Calendar c1 = Calendar.getInstance();
        if (firstDayOfCurrentMonth != 1) {  // September  = 4
            daysLeftInFirstWeek = firstDayOfCurrentMonth; // 4
            indexOfDayAfterLastDayOfMonth = daysLeftInFirstWeek + daysInCurrentMonth; // 34 in september
            Log.e("indexOfDayLastDayMonth", indexOfDayAfterLastDayOfMonth + "");
            for (int i = firstDayOfCurrentMonth; i < firstDayOfCurrentMonth + daysInCurrentMonth; ++i) { // i = 4 ;i<34 ;I++
                if (currentDateMonth == chosenDateMonth
                        && currentDateYear == chosenDateYear
                        && dayNumber == currentDateDay) {



                    days[i].setBackgroundResource(R.drawable.round_background);
                    days[i].setTypeface(null, Typeface.BOLD);
                    days[i].setTextColor(Color.WHITE);


                } else {
                    if (disable.contains(dayNumber)) {
                        Log.e("DAY NUMBER", dayNumber + "");
                        days[i].setBackgroundColor(Color.parseColor(LIGHT_GRAY));

                    } else {
                        days[i].setBackgroundColor(Color.TRANSPARENT);
                    }
                    days[i].setTextColor(Color.parseColor(MONTH_DATE_COLOR));

                }

                int[] dateArr = new int[3];
                dateArr[0] = dayNumber;
                dateArr[1] = chosenDateMonth;
                dateArr[2] = chosenDateYear;
                days[i].setTag(dateArr);
                days[i].setText(String.valueOf(dayNumber));

              /*  days[i].setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDayClick(v);
                    }
                });*/
                ++dayNumber;
            }
        } else {
            daysLeftInFirstWeek = 6;
            indexOfDayAfterLastDayOfMonth = daysLeftInFirstWeek + daysInCurrentMonth;
            for (int i = 6; i < 6 + daysInCurrentMonth; ++i) {
                if (currentDateMonth == chosenDateMonth
                        && currentDateYear == chosenDateYear
                        && dayNumber == currentDateDay) {
                    days[i].setBackgroundResource(R.drawable.round_background);
                    days[i].setTextColor(Color.WHITE);


                } else {
                    if (disable.contains(dayNumber)) {
                        days[i].setBackgroundColor(Color.parseColor(LIGHT_GRAY));
                        Log.e("TRUE IS", "TRUE");
                    } else {
                        days[i].setBackgroundColor(Color.TRANSPARENT);
                    }
                    days[i].setTextColor(Color.BLACK);
                }

                int[] dateArr = new int[3];
                dateArr[0] = dayNumber;
                dateArr[1] = chosenDateMonth;
                dateArr[2] = chosenDateYear;
                days[i].setTag(dateArr);
                days[i].setText(String.valueOf(dayNumber));

               /* days[i].setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDayClick(v);
                    }
                });*/
                ++dayNumber;
            }
        }

        if (month > 0)
            calendar.set(year, month - 1, 1);
        else
            calendar.set(year - 1, 11, 1);
        int daysInPreviousMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = daysLeftInFirstWeek - 1; i >= 0; --i) {
            int[] dateArr = new int[3];

            if (chosenDateMonth > 0) {
                if (currentDateMonth == chosenDateMonth - 1
                        && currentDateYear == chosenDateYear
                        && daysInPreviousMonth == currentDateDay) {
                } else {
                    //days[i].setBackgroundColor(Color.TRANSPARENT);
                    if (previousMonthDisabled.contains(daysInPreviousMonth)) {
                        days[i].setBackgroundColor(Color.parseColor(LIGHT_GRAY));
                        days[i].setTextColor(Color.TRANSPARENT);
                    } else {
                        days[i].setBackgroundColor(Color.TRANSPARENT);
                        days[i].setTextColor(Color.parseColor(CUSTOM_WHITE));

                    }

                }

                dateArr[0] = daysInPreviousMonth;
                dateArr[1] = chosenDateMonth - 1;
                dateArr[2] = chosenDateYear;
            } else {
                if (currentDateMonth == 11
                        && currentDateYear == chosenDateYear - 1
                        && daysInPreviousMonth == currentDateDay) {
                } else {
                    //days[i].setBackgroundColor(Color.TRANSPARENT);
                    days[i].setBackgroundColor(Color.TRANSPARENT);
                }

                dateArr[0] = daysInPreviousMonth;
                dateArr[1] = 11;
                dateArr[2] = chosenDateYear - 1;
            }

            days[i].setTag(dateArr);
            days[i].setText(String.valueOf(daysInPreviousMonth--));
           /* days[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDayClick(v);
                }
            });*/
        }

        int nextMonthDaysCounter = 1;
        for (int i = indexOfDayAfterLastDayOfMonth; i < days.length; ++i) {
            int[] dateArr = new int[3];

            if (chosenDateMonth < 11) {
                if (currentDateMonth == chosenDateMonth + 1
                        && currentDateYear == chosenDateYear
                        && nextMonthDaysCounter == currentDateDay) {
                    days[i].setBackgroundColor(getResources().getColor(R.color.pink));
                } else {
                    //   days[i].setBackgroundColor(Color.TRANSPARENT);
                    Log.e("DAY COUNTEr", nextMonthDaysCounter + "");
                    Log.e("nextMonthDisable", nextMonthDisable + "");

                    if (nextMonthDisable.contains(nextMonthDaysCounter)) {
                        days[i].setBackgroundColor(Color.parseColor(LIGHT_GRAY));
                        days[i].setTextColor(Color.TRANSPARENT);
                        Log.e("TRUE IS", "TRUE");

                    } else {
                        days[i].setBackgroundColor(Color.TRANSPARENT);
                        days[i].setTextColor(Color.parseColor(CUSTOM_WHITE));
                        Log.e("FALSE IS", "FALSE");
                    }
                }

                dateArr[0] = nextMonthDaysCounter;
                dateArr[1] = chosenDateMonth + 1;
                dateArr[2] = chosenDateYear;
            } else {
                if (currentDateMonth == 0
                        && currentDateYear == chosenDateYear + 1
                        && nextMonthDaysCounter == currentDateDay) {
                    days[i].setBackgroundColor(getResources().getColor(R.color.pink));
                    days[i].setTextColor(Color.parseColor(CUSTOM_WHITE));
                } else {
                    days[i].setBackgroundColor(Color.TRANSPARENT);
                    days[i].setBackgroundColor(Color.TRANSPARENT);
                    days[i].setTextColor(Color.parseColor(CUSTOM_WHITE));
                }

                dateArr[0] = nextMonthDaysCounter;
                dateArr[1] = 0;
                dateArr[2] = chosenDateYear + 1;
            }

            days[i].setTag(dateArr);


            days[i].setText(String.valueOf(nextMonthDaysCounter++));

        }

        calendar.set(chosenDateYear, chosenDateMonth, chosenDateDay);
    }

    public void onDayClick(View view) {
        mListener.onDayClick(view);

        if (selectedDayButton != null) {
            if (chosenDateYear == currentDateYear
                    && chosenDateMonth == currentDateMonth
                    && pickedDateDay == currentDateDay) {
                selectedDayButton.setBackgroundColor(getResources().getColor(R.color.pink));
                selectedDayButton.setTextColor(Color.WHITE);
            } else {
                selectedDayButton.setBackgroundColor(Color.TRANSPARENT);
                if (selectedDayButton.getCurrentTextColor() != Color.RED) {
                    selectedDayButton.setTextColor(getResources()
                            .getColor(R.color.calendar_number));
                }
            }
        }

        selectedDayButton = (Button) view;
        if (selectedDayButton.getTag() != null) {
            int[] dateArray = (int[]) selectedDayButton.getTag();
            pickedDateDay = dateArray[0];
            pickedDateMonth = dateArray[1];
            pickedDateYear = dateArray[2];
        }

        if (pickedDateYear == currentDateYear
                && pickedDateMonth == currentDateMonth
                && pickedDateDay == currentDateDay) {
            selectedDayButton.setBackgroundColor(getResources().getColor(R.color.pink));
            selectedDayButton.setTextColor(Color.WHITE);
        } else {
            selectedDayButton.setBackgroundColor(getResources().getColor(R.color.grey));
            if (selectedDayButton.getCurrentTextColor() != Color.RED) {
                selectedDayButton.setTextColor(Color.WHITE);
            }
        }
    }

    private void addDaysinCalendar(LayoutParams buttonParams, Context context,
                                   DisplayMetrics metrics) {
        int engDaysArrayCounter = 0;
        int padding8 = (int) metrics.density * 3;
        for (int weekNumber = 0; weekNumber < 3; ++weekNumber) {
            for (int dayInWeek = 0; dayInWeek < 14; ++dayInWeek) {
                //final Button day = new Button(context);
                final TextView day = new TextView(context);

                day.setTextColor(Color.parseColor(CUSTOM_WHITE));
                day.setBackgroundColor(Color.TRANSPARENT);

                day.setPadding(0, padding8, 0, padding8);
                day.setLayoutParams(buttonParams);
                day.setGravity(Gravity.CENTER);
                day.setTextSize(14);
                day.setSingleLine();

                days[engDaysArrayCounter] = day;
                weeks[weekNumber].addView(day);

                ++engDaysArrayCounter;
            }
        }


    }

    private LayoutParams getdaysLayoutParams() {
        LayoutParams buttonParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        buttonParams.weight = 1;

        return buttonParams;
    }

    public void setUserDaysLayoutParams(LayoutParams userButtonParams) {
        this.userButtonParams = userButtonParams;
    }

    public void setUserCurrentMonthYear(int userMonth, int userYear) {
        this.userMonth = userMonth;
        this.userYear = userYear;
    }

    public void setDayBackground(Drawable userDrawable) {
        this.userDrawable = userDrawable;
    }

    public interface DayClickListener {
        void onDayClick(View view);
    }

    public void setCallBack(DayClickListener mListener) {
        this.mListener = mListener;
    }

}
