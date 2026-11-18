package com.example.ugandaconnect;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Names of all the screens
    public static final String HOME = "home";
    public static final String SERVICES = "services";
    public static final String BOOKMARKS = "bookmarks";
    public static final String PROFILE = "profile";
    public static final String SOLAR = "solar";
    public static final String HEALTH = "health";
    public static final String RESULTS = "results";
    public static final String BILLS = "bills";

    // Views
    private DrawerLayout drawerLayout;
    private AppBarLayout appBarLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private FloatingActionButton fab;
    private BottomAppBar bottomAppBar;              // null on tablets
    private BottomNavigationView bottomNavigation;  // null on tablets
    private NavigationRailView navigationRail;      // null on phones
    private SearchView searchView;
    private ImageView bellIcon;
    private TextView badgeText;

    // Other variables
    private String currentKey = HOME;
    private int unreadCount = 3;            // fake number of unread notifications
    private boolean keyboardVisible = false;
    private boolean updatingUi = false;     // stops the listeners from running while we update the UI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Android picks layout/activity_main.xml (phone) or layout-w600dp/activity_main.xml (tablet)
        setContentView(R.layout.activity_main);

        findViews();
        setupToolbar();
        setupDrawer();
        setupBottomNavigation();
        setupNavigationRail();
        setupFab();
        setupKeyboardListener();
        setupBackButton();

        if (savedInstanceState == null) {
            openScreen(HOME);
        } else {
            // the phone was rotated, so the fragment is restored by Android
            currentKey = savedInstanceState.getString("current_key", HOME);
            updateScreenUi(currentKey);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("current_key", currentKey);
    }

    // ---------------------------------------------------------------
    // Setup methods
    // ---------------------------------------------------------------

    private void findViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        appBarLayout = findViewById(R.id.app_bar_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        fab = findViewById(R.id.fab);

        // These exist in only one of the two layouts, so they can be null
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        bottomNavigation = findViewById(R.id.bottom_nav);
        navigationRail = findViewById(R.id.nav_rail);
    }

    private void setupToolbar() {
        toolbar.inflateMenu(R.menu.top_app_bar_menu);

        // hamburger icon opens the drawer
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        Menu menu = toolbar.getMenu();

        // SEARCH
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setQueryHint("Search...");
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterCurrentScreen(newText);
                        return true;
                    }
                });
            }
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                    filterCurrentScreen(""); // show the full list again
                    return true;
                }
            });
        }

        // NOTIFICATION BELL with badge
        MenuItem bellItem = menu.findItem(R.id.action_notifications);
        if (bellItem != null) {
            View bellView = bellItem.getActionView();
            if (bellView != null) {
                bellIcon = bellView.findViewById(R.id.bell_icon);
                badgeText = bellView.findViewById(R.id.badge_text);
                bellView.setOnClickListener(v -> {
                    unreadCount = 0;   // user has read the notifications
                    updateBadge();
                    Toast.makeText(MainActivity.this, "No new notifications", Toast.LENGTH_SHORT).show();
                });
            }
        }
        updateBadge();
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            String key;
            if (id == R.id.drawer_solar) {
                key = SOLAR;
            } else if (id == R.id.drawer_health) {
                key = HEALTH;
            } else if (id == R.id.drawer_results) {
                key = RESULTS;
            } else if (id == R.id.drawer_bills) {
                key = BILLS;
            } else {
                key = HOME;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            if (!Objects.equals(key, currentKey)) {
                openScreen(key);
            }
            return true;
        });
    }

    private void setupBottomNavigation() {
        if (bottomNavigation == null) {
            return; // tablet layout has no bottom bar
        }
        bottomNavigation.setOnItemSelectedListener(item -> {
            if (updatingUi) {
                return true;
            }
            String key = getKeyFromNavItem(item.getItemId());
            if (!Objects.equals(key, currentKey)) {
                openScreen(key);
            }
            return true;
        });
    }

    private void setupNavigationRail() {
        if (navigationRail == null) {
            return; // phone layout has no rail
        }
        navigationRail.setOnItemSelectedListener(item -> {
            if (updatingUi) {
                return true;
            }
            String key = getKeyFromNavItem(item.getItemId());
            if (!Objects.equals(key, currentKey)) {
                openScreen(key);
            }
            return true;
        });
    }

    private void setupFab() {
        // The FAB is the primary action. Its text depends on the screen.
        fab.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, getFabText(currentKey), Toast.LENGTH_SHORT).show());
    }

    // Hide the bottom bar when the keyboard is open, show it again when it closes
    private void setupKeyboardListener() {
        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect visibleFrame = new Rect();
            rootView.getWindowVisibleDisplayFrame(visibleFrame);
            int screenHeight = rootView.getRootView().getHeight();
            int keyboardHeight = screenHeight - visibleFrame.bottom;

            // if more than 15% of the screen is covered, the keyboard is open
            boolean nowVisible = keyboardHeight > screenHeight * 0.15;
            if (nowVisible != keyboardVisible) {
                keyboardVisible = nowVisible;
                updateBottomBarVisibility();
            }
        });
    }

    private void setupBackButton() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (!HOME.equals(currentKey)) {
                    openScreen(HOME);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // ---------------------------------------------------------------
    // Opening screens
    // ---------------------------------------------------------------

    // Show a screen (also called from ScreenFragment when a row is clicked)
    public void openScreen(String key) {
        currentKey = key;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, ScreenFragment.newInstance(key))
                .commit();
        updateScreenUi(key);
    }

    // Change title, colors and selected items to match the screen
    private void updateScreenUi(String key) {
        updatingUi = true;

        toolbar.setTitle(getTitleFor(key));
        applyColors(key);

        // close the search box if it is open
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        if (searchItem != null && searchItem.isActionViewExpanded()) {
            searchItem.collapseActionView();
        }

        // highlight the right item in the bottom bar / rail
        int navId = getNavItemId(key);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(navId);
        }
        if (navigationRail != null) {
            navigationRail.setSelectedItemId(navId);
        }

        // highlight the right item in the drawer
        int drawerId = getDrawerItemId(key);
        Menu drawerMenu = navigationView.getMenu();
        for (int drawerIndex = 0; drawerIndex < drawerMenu.size(); drawerIndex++) {
            MenuItem item = drawerMenu.getItem(drawerIndex);
            item.setChecked(item.getItemId() == drawerId);
        }

        updatingUi = false;
    }

    // Brand identity: black/yellow normally, and a different color for each service
    private void applyColors(String key) {
        int barColor;
        int contentColor;

        switch (key) {
            case SOLAR:
                barColor = ContextCompat.getColor(this, R.color.green_rea);       // green for REA
                contentColor = ContextCompat.getColor(this, R.color.white);
                break;
            case HEALTH:
                barColor = ContextCompat.getColor(this, R.color.blue_health);     // blue for health
                contentColor = ContextCompat.getColor(this, R.color.white);
                break;
            case RESULTS:
                barColor = ContextCompat.getColor(this, R.color.uganda_yellow);   // yellow for education
                contentColor = ContextCompat.getColor(this, R.color.uganda_black);
                break;
            case BILLS:
                barColor = ContextCompat.getColor(this, R.color.uganda_red);      // red for public agencies
                contentColor = ContextCompat.getColor(this, R.color.white);
                break;
            default:
                barColor = ContextCompat.getColor(this, R.color.uganda_black);    // Uganda colors
                contentColor = ContextCompat.getColor(this, R.color.uganda_yellow);
                break;
        }

        appBarLayout.setBackgroundColor(barColor);
        toolbar.setTitleTextColor(contentColor);
        toolbar.setNavigationIconTint(contentColor);

        // color the icons in the toolbar menu (the search icon)
        Menu menu = toolbar.getMenu();
        for (int toolbarIndex = 0; toolbarIndex < menu.size(); toolbarIndex++) {
            MenuItem item = menu.getItem(toolbarIndex);
            Drawable icon = item.getIcon();
            if (icon != null) {
                Drawable wrapped = DrawableCompat.wrap(icon.mutate());
                DrawableCompat.setTint(wrapped, contentColor);
                item.setIcon(wrapped);
            }
        }

        // color the bell and the text inside the search box
        if (bellIcon != null) {
            bellIcon.setColorFilter(contentColor);
        }
        if (searchView != null) {
            EditText searchText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            if (searchText != null) {
                searchText.setTextColor(contentColor);
                searchText.setHintTextColor(contentColor);
            }
            ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
            if (closeButton != null) {
                closeButton.setColorFilter(contentColor);
            }
        }

        // color the FAB
        fab.setBackgroundTintList(ColorStateList.valueOf(barColor));
        fab.setImageTintList(ColorStateList.valueOf(contentColor));
        fab.setContentDescription(getFabText(key));
    }

    // ---------------------------------------------------------------
    // Small helper methods
    // ---------------------------------------------------------------

    private void updateBadge() {
        if (badgeText == null) {
            return;
        }
        if (unreadCount > 0) {
            badgeText.setVisibility(View.VISIBLE);
            if (unreadCount > 9) {
                badgeText.setText("9+");
            } else {
                badgeText.setText(String.valueOf(unreadCount));
            }
        } else {
            badgeText.setVisibility(View.GONE);
        }
    }

    private void updateBottomBarVisibility() {
        if (bottomAppBar == null) {
            return; // tablets have no bottom bar
        }
        if (keyboardVisible) {
            bottomAppBar.setVisibility(View.GONE);
            fab.hide();
        } else {
            bottomAppBar.setVisibility(View.VISIBLE);
            fab.show();
        }
    }

    private void filterCurrentScreen(String text) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof ScreenFragment) {
            ((ScreenFragment) fragment).filter(text);
        }
    }

    public static String getTitleFor(String key) {
        switch (key) {
            case SERVICES:
                return "Services";
            case BOOKMARKS:
                return "Bookmarks";
            case PROFILE:
                return "Profile";
            case SOLAR:
                return "Solar Subsidies";
            case HEALTH:
                return "Healthcare";
            case RESULTS:
                return "My Results";
            case BILLS:
                return "Bill Payments";
            case HOME:
            default:
                return "UgandaConnect";
        }
    }

    private String getFabText(String key) {
        switch (key) {
            case SOLAR:
            case HEALTH:
                return "Start New Application";
            default:
                return "Report Issue";
        }
    }

    // bottom bar / rail item id -> screen name
    private String getKeyFromNavItem(int id) {
        if (id == R.id.nav_services) {
            return SERVICES;
        } else if (id == R.id.nav_bookmarks) {
            return BOOKMARKS;
        } else if (id == R.id.nav_profile) {
            return PROFILE;
        }
        return HOME;
    }

    // screen name -> bottom bar / rail item id
    private int getNavItemId(String key) {
        switch (key) {
            case HOME:
                return R.id.nav_home;
            case BOOKMARKS:
                return R.id.nav_bookmarks;
            case PROFILE:
                return R.id.nav_profile;
            default:
                return R.id.nav_services; // Services and all four service screens
        }
    }

    // screen name -> drawer item id (-1 means no drawer item is selected)
    private int getDrawerItemId(String key) {
        switch (key) {
            case HOME:
                return R.id.drawer_home;
            case SOLAR:
                return R.id.drawer_solar;
            case HEALTH:
                return R.id.drawer_health;
            case RESULTS:
                return R.id.drawer_results;
            case BILLS:
                return R.id.drawer_bills;
            default:
                return -1;
        }
    }
}
