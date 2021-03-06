package th.ac.kmitl.groupedapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

import th.ac.kmitl.groupedapplication.adapter.ClassroomItemClickListener;
import th.ac.kmitl.groupedapplication.adapter.ClassroomListAdapter;
import th.ac.kmitl.groupedapplication.model.Classroom;
import th.ac.kmitl.groupedapplication.controller.setNavHeader;

public class ClassroomActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener, ClassroomItemClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ChildEventListener childEventListener_class;
    private Query QuerybyUID;

    private TextView tvEmail;
    private TextView tvFullName;
    private RecyclerView recyclerView;

    private String uid;
    private String ustatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classroom);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //--------------mthu---------------------
        mAuth = FirebaseAuth.getInstance();
        //------------setVisibility---------------
        findViewById(R.id.inc_class).setVisibility(View.VISIBLE);
        //----------------decrea recyclerview------------------------
        recyclerView = findViewById(R.id.recyclerViewClassroom);
        recyclerView.setHasFixedSize(true);
        //----------------set layout-----------------------
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        //--------------get Intent----------------------
        Intent getI = getIntent(); //getFrom Login
        uid = getI.getStringExtra("uid");
        ustatus = getI.getStringExtra("ustatus");
        //---------------drawer menu bar---------------------
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //-------------create nav--------------------------------
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_classroom);
        Log.e("ustatusClassroom",ustatus);
        if(ustatus.equals("0")) {
            Menu menubar = navigationView.getMenu();
            menubar.findItem(R.id.nav_classcreate).setVisible(false);
        }
        //-------------class data set to recycleview------------
        Log.e("test","test");
        getDataResults(uid, ustatus);
    }

    public void getDataResults(final String uID, final String uSTATUS){
        final ArrayList<Classroom> classroomArrayList = new ArrayList<Classroom>();
        //------------Firebase-----------------

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("classrooms");

        if(uSTATUS.equals("0")) {
            QuerybyUID = myRef;
        } else {
            QuerybyUID = myRef.orderByChild("p_uid").equalTo(uid);
        }

        QuerybyUID.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String classID;
                String classSubject;
                String puid;
                if(uSTATUS.equals("0")) {

                    for (DataSnapshot findUID: dataSnapshot.child("member_list").getChildren()) {
                        if(findUID.getValue().equals(uID)) {
                            Log.w("memberUID",findUID.toString());
                            Log.e("getData_std",dataSnapshot.toString());
                            classID = dataSnapshot.getKey();
                            classSubject = dataSnapshot.child("subj_name").getValue().toString();
                            puid = dataSnapshot.child("p_uid").getValue().toString();
                            Classroom  classroom  = new Classroom(classID, classSubject, puid);
                            classroomArrayList.add(classroom);
                        } else {
                            Log.e("*not*std_uid","cur_std_id: "+uid+" | not: "+findUID.toString());
                        }
                    }

                } else {
                    Log.w("professerUID",uID);
                    Log.e("getData_prof",dataSnapshot.toString());
                    classID = dataSnapshot.getKey();
                    puid = dataSnapshot.child("p_uid").getValue().toString();
                    classSubject = dataSnapshot.child("subj_name").getValue().toString();
                    Classroom  classroom  = new Classroom(classID, classSubject, puid);
                    classroomArrayList.add(classroom);
                }
                getAllClassroom(classroomArrayList);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("cancell","ok");
            }
        });

    }

    public void getAllClassroom(ArrayList<Classroom> classroomlist) {
        ArrayList<Classroom> classrooms = classroomlist;
        ClassroomListAdapter classroomAdapter = new ClassroomListAdapter(classrooms, ClassroomActivity.this);
        Log.d("cList",String.valueOf(classrooms.toArray()));
        recyclerView.setAdapter(classroomAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (childEventListener_class != null) {
            myRef.removeEventListener(childEventListener_class);
        }
    }

    //--------for RecycleView-------------------
    @Override
    public void onClassroomItemClick(String classID,String className,String puID) {

        //-------------intent ไป ClassMenuActivity----------
        Intent i = new Intent(this, ClassMenuActivity.class);
        Intent getI = getIntent();
        uid = getI.getStringExtra("uid");
        i.putExtra("classid", classID);
        i.putExtra("classname", className);
        i.putExtra("uid", uid);
        i.putExtra("puid", puID);
        i.putExtra("ustatus", ustatus); // is 0 or 1
        startActivity(i);
        Toast.makeText(this, "Class Id: "+ classID +" PUID: "+puID+" ustatus: "+ ustatus, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!ustatus.equals("0")) {
            getMenuInflater().inflate(R.menu.main, menu);
        } else {
            getMenuInflater().inflate(R.menu.main, menu);
            MenuItem cleateClass = menu.findItem(R.id.action_createClassroom);
            cleateClass.setVisible(false);
            MenuItem joinClass = menu.findItem(R.id.action_signupClassroom);
            joinClass.setVisible(true);
            Log.e("xxxx",ustatus);
        }
        //---------nav head-----------------
        tvEmail = findViewById(R.id.textEmail);
        tvFullName = findViewById(R.id.textFullName);
        new setNavHeader(uid,tvEmail,tvFullName);

        Log.e("CreateOpMenu","ok");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_signupClassroom) {
            Intent i = new Intent(ClassroomActivity.this, ClassroomJoinActivity.class);
            i.putExtra("uid", uid);
            i.putExtra("ustatus", ustatus);
            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            //finish();
            //findViewById(R.id.inc_class).setVisibility(View.GONE);
            return true;
        }

        if (id == R.id.action_createClassroom) {
            Intent i = new Intent(ClassroomActivity.this, ClassroomCreateActivity.class);
            i.putExtra("uid", uid);
            startActivity(i);
            //findViewById(R.id.inc_profile).setVisibility(View.GONE);
            //finish();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent getI = getIntent();
        uid = getI.getStringExtra("uid");

        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent i = new Intent(ClassroomActivity.this, ProfileActivity.class);
            i.putExtra("uid", uid);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            findViewById(R.id.inc_class).setVisibility(View.GONE);
            finish();
        } else if (id == R.id.nav_classroom) {

        } else if (id == R.id.nav_classcreate) {
            Intent i = new Intent(ClassroomActivity.this, ClassroomCreateActivity.class);
            i.putExtra("uid", uid);
            startActivity(i);
           // findViewById(R.id.inc_class).setVisibility(View.GONE);
            //finish();
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent i = new Intent(ClassroomActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            Toast.makeText(ClassroomActivity.this, "ออกจากระบบแล้ว!", Toast.LENGTH_SHORT).show();
            findViewById(R.id.inc_class).setVisibility(View.GONE);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
