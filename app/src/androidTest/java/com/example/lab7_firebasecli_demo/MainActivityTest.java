package com.example.lab7_firebasecli_demo;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import android.util.Log;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    static int firestorePort = 8080; // Changed to match your previous setup
    static String androidLocalhost = "10.0.2.2";

    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        // Connect to local emulator
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, firestorePort);
    }

    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference citiesRef = db.collection("cities");
        City[] cityList = {
                new City("Calgary", "AB"),
                new City("Toronto", "ON"),
        };
        for (City city : cityList) {
            DocumentReference docRef = citiesRef.document(city.getCityName());
            try {
                Tasks.await(docRef.set(city));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void tearDown() {
        String projectId = "lab7-5a91b"; // Your Project ID
        try {
            URL url = new URL("http://10.0.2.2:" + firestorePort + "/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.getResponseCode();
            urlConnection.disconnect();
        } catch (IOException e) {
            Log.e("TearDown Error", e.getMessage());
        }
    }

    @Test
    public void testAddCity() {
        onView(withId(R.id.button_add)).perform(click());
        onView(withId(R.id.editText_name)).perform(typeText("Edmonton"), closeSoftKeyboard());
        onView(withId(R.id.button_confirm)).perform(click());
        onView(withText("Edmonton")).check(matches(isDisplayed()));
    }

    @Test
    public void testClearCity() {
        onView(withId(R.id.button_clear)).perform(click());
        onView(withText("Calgary")).check(doesNotExist());
        onView(withText("Toronto")).check(doesNotExist());
    }

    @Test
    public void testSeededCities() {
        onView(withText("Calgary")).check(matches(isDisplayed()));
        onView(withText("Toronto")).check(matches(isDisplayed()));

        onView(withText("Calgary")).perform(click());
        onView(withId(R.id.city_text)).check(matches(withText("Calgary")));
    }
    @Test
    public void testActivitySwitch() {
        onView(withText("Calgary")).perform(click());
        onView(withId(R.id.button_back)).check(matches(isDisplayed()));
    }

    @Test
    public void testCityNameConsistency() {
        onView(withText("Toronto")).perform(click());
        onView(withId(R.id.city_text)).check(matches(withText("Toronto")));
    }

    @Test
    public void testBackButton() {
        onView(withText("Calgary")).perform(click());
        onView(withId(R.id.button_back)).perform(click());
        onView(withId(R.id.city_list)).check(matches(isDisplayed()));
    }
}