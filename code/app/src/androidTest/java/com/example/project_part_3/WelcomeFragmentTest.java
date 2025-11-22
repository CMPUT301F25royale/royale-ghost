package com.example.project_part_3;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.project_part_3.Enter.WelcomeFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class WelcomeFragmentTest {

    @Test
    public void testNavigationToLoginFragment() {
        // Create a TestNavHostController
        TestNavHostController navController = new TestNavHostController(
                ApplicationProvider.getApplicationContext());

        // Create a graphical FragmentScenario for the WelcomeFragment
        FragmentScenario<WelcomeFragment> welcomeFragmentScenario = FragmentScenario.launchInContainer(WelcomeFragment.class);

        welcomeFragmentScenario.onFragment(fragment -> {
            // Set the graph on the TestNavHostController
            navController.setGraph(R.navigation.nav_graph);

            // Make the NavController available via the findNavController() APIs
            Navigation.setViewNavController(fragment.requireView(), navController);
        });

        // Verify that performing a click changes the NavController's state
        Espresso.onView(ViewMatchers.withId(R.id.Login)).perform(ViewActions.click());
        assertEquals(navController.getCurrentDestination().getId(), R.id.loginFragment);
    }
}
