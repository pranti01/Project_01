package com.xyz.moneytrail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.xyz.moneytrail.R;

import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;


    //Fragment

    private DashBoardFragment dashBoardFragment;
    private IncomeFragment incomeFragment;
    private ExpenseFragment expenseFragment;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle("MoneyTrail");
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();




        bottomNavigationView=findViewById(R.id.bottomNavigationbar);
        frameLayout=findViewById(R.id.main_frame);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);


        dashBoardFragment=new DashBoardFragment();
        incomeFragment=new IncomeFragment();
        expenseFragment=new ExpenseFragment();

        setFragment(dashBoardFragment);


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId()==R.id.dashboard) {
                    setFragment(dashBoardFragment);
                    bottomNavigationView.setItemBackgroundResource(R.color.blue);
                    return true;
                }
                else if (item.getItemId()==R.id.income) {
                    setFragment(incomeFragment);
                    bottomNavigationView.setItemBackgroundResource(R.color.btwn_blue_mint);
                    return true;
                } else if (item.getItemId()==R.id.expense) {
                    setFragment(expenseFragment);
                    bottomNavigationView.setItemBackgroundResource(R.color.mint);
                    return true;
                }


                else {
                    return false;
                }
            }
        });
    }

    private void setFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }


    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }


    public void displaySelectedListener(int itemId) {


        Fragment fragment = null;

        if(itemId==R.id.dashboard) {
            setFragment(dashBoardFragment);
        }
        else if (itemId==R.id.income) {
            setFragment(incomeFragment);
        }
        else if (itemId==R.id.expense) {
            setFragment(expenseFragment);
        }
        else if (itemId==R.id.logout) {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }



        if (fragment!=null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_frame, fragment);
            ft.commit();

        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        displaySelectedListener(item.getItemId());
        return true;
    }
}