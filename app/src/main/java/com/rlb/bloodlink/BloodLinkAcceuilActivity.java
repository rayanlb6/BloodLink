package com.rlb.bloodlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BloodLinkAcceuilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_link_acceuil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }
    public void startSelectRole(View view){
        Intent intent=new Intent(BloodLinkAcceuilActivity.this,BloodLinkRoleActivity.class);
        startActivity(intent);
    }
    public void acceuil(View view){

        View view1=findViewById(R.id.view1);
        View view2=findViewById(R.id.view2);
        View view3=findViewById(R.id.view3);
        TextView tv1=findViewById(R.id.textview1);
        TextView tv2=findViewById(R.id.textview2);
        TextView tv3=findViewById(R.id.textview3);
        if(tv1.getVisibility()==View.VISIBLE){
            tv1.setVisibility(View.GONE);
            view1.setBackgroundResource(R.drawable.btn_gray2);
            tv2.setVisibility(View.VISIBLE);
            view2.setBackgroundResource(R.drawable.btn_red2);

        }
        else if(tv2.getVisibility()==View.VISIBLE){
            tv2.setVisibility(View.GONE);
            view2.setBackgroundResource(R.drawable.btn_gray2);
            tv3.setVisibility(View.VISIBLE);
            view3.setBackgroundResource(R.drawable.btn_red2);
        }
        else if(tv3.getVisibility()==View.VISIBLE){
            Intent intent=new Intent(BloodLinkAcceuilActivity.this,BloodLinkRoleActivity.class);
            startActivity(intent);
            finish();
        }


    }

}