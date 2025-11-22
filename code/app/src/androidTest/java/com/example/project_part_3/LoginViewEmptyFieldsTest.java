package com.example.project_part_3;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginViewEmptyFieldsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testEmptyLoginFields() {
        // Navigate to the login screen first
        Espresso.onView(ViewMatchers.withId(R.id.Login)).perform(ViewActions.click());

        // Now on the login screen, click the submit button without entering text
        Espresso.onView(ViewMatchers.withId(R.id.Login_submit)).perform(ViewActions.click());

        // Check for the toast message
        activityRule.getScenario().onActivity(activity -> {
            Espresso.onView(withText("Please fill in all fields"))
                    .inRoot(withDecorView(not(activity.getWindow().getDecorView())))
                    .check(matches(isDisplayed()));
        });
    }
}
