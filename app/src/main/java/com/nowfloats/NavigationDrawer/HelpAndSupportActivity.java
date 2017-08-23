package com.nowfloats.NavigationDrawer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.freshdesk.hotline.Hotline;
import com.nowfloats.Login.UserSessionManager;
import com.nowfloats.NavigationDrawer.API.RiaNetworkInterface;
import com.nowfloats.NavigationDrawer.model.RiaSupportModel;
import com.nowfloats.util.Constants;
import com.nowfloats.util.Methods;
import com.thinksity.R;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HelpAndSupportActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView headerText, tvConsultantName, tvConsultantNumber, tvEmail, tvTextHelp, tvTextRia,
            tvTextFaq1, tvTextFaq2, tvRia;
    Button btnSendEmail, btnCall, btnSchedule, btnChat;
    ImageView ivHelpAvatar;
    ProgressDialog pd;

    UserSessionManager sessionManager;
    RiaSupportModel mRiaSupportModel;

    private static final String CHAT_INTENT_URI = "com.biz2.nowfloats://com.riachat.start";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_and_support);

        /*toolbar = (Toolbar) findViewById(R.id.help_and_support_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        headerText = (TextView) toolbar.findViewById(R.id.titleTextView);
        headerText.setText(getResources().getString(R.string.help_and_support));*/

        tvConsultantName = (TextView) findViewById(R.id.tv_consultant_name);
        tvConsultantNumber = (TextView) findViewById(R.id.tv_contact_number);
        tvEmail = (TextView) findViewById(R.id.tv_consultant_email);
        tvRia = (TextView) findViewById(R.id.tvRia);
        tvTextHelp = (TextView) findViewById(R.id.tv_text_help);
        tvTextRia = (TextView) findViewById(R.id.tv_ria_text);
        tvTextFaq1 = (TextView) findViewById(R.id.tv_text_faq1);
        tvTextFaq2 = (TextView) findViewById(R.id.tv_text_faq2);

        ivHelpAvatar = (ImageView) findViewById(R.id.iv_help_avatar);

        btnSendEmail = (Button) findViewById(R.id.btn_send_mail);
        btnCall = (Button) findViewById(R.id.btn_call);
        btnSchedule = (Button) findViewById(R.id.btn_schedule);
        btnChat = (Button) findViewById(R.id.btnChat);

        sessionManager = new UserSessionManager(getApplicationContext(), this);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://ria.withfloats.com")
                .build();
        final RiaNetworkInterface riaNetworkInterface = restAdapter.create(RiaNetworkInterface.class);
        Map<String, String> param = new HashMap<>();
        param.put("clientId", Constants.clientId);
        param.put("fpTag", sessionManager.getFpTag());
        riaNetworkInterface.getMemberForFp(param, new Callback<RiaSupportModel>() {
            @Override
            public void success(final RiaSupportModel riaSupportModel, Response response) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                if (riaSupportModel != null) {
                    mRiaSupportModel = riaSupportModel;

                    if (riaSupportModel.getGender() == 1) {
                        tvConsultantName.setText("Ms. " + riaSupportModel.getName());
                        ivHelpAvatar.setImageDrawable(ContextCompat.getDrawable(HelpAndSupportActivity.this, R.drawable.help_female_avatar));
                        btnCall.setText("CALL HER");
                    } else {
                        tvConsultantName.setText("Mr. " + riaSupportModel.getName());
                        ivHelpAvatar.setImageDrawable(ContextCompat.getDrawable(HelpAndSupportActivity.this, R.drawable.help_male_avatar));
                        btnCall.setText("CALL HIM");
                    }
                    tvConsultantNumber.setText(Methods.fromHtml("<a href=\"\">" + riaSupportModel.getPhoneNumber() + "</a>"));
                    tvConsultantNumber.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_DIAL);
                            i.setData(Uri.parse("tel:" + riaSupportModel.getPhoneNumber()));
                            startActivity(i);

                        }
                    });
                    tvEmail.setText(Methods.fromHtml("<a href=\"mail:" + riaSupportModel.getEmail() + "\">" + riaSupportModel.getEmail() + "</a>"));
                    String genderVal = (riaSupportModel.getGender() == 1) ? "her" : "him";
                    tvTextHelp.setText(Methods.fromHtml(riaSupportModel.getName() + " is your dedicated web consultant who will be assisting you with all your queries related to your NowFloats website. You can call " + genderVal + " anytime from <b>9.30 am to 6.30 pm</b> on all working days."));
                } else {
//                    finish();
//                    Intent call = new Intent(Intent.ACTION_DIAL);
//                    String callString = "tel:" + getString(R.string.contact_us_number);
//                    call.setData(Uri.parse(callString));
//                    startActivity(call);
                    tvTextRia.setVisibility(View.GONE);
                    tvTextFaq1.setVisibility(View.GONE);
                    tvTextFaq2.setText(Methods.fromHtml("For Product related queries, please refer to our <a href=\"" + getString(R.string.faqs_url) + "\">FAQs</a>"));

                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (pd != null && pd.isShowing()) {
                    pd.dismiss();
                }
                AlertDialog dialog = new AlertDialog.Builder(HelpAndSupportActivity.this)
                        .setMessage("Error while getting dedicated web consultant")
                        .setCancelable(false)
                        .show();

            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent();
                i.setAction(Intent.ACTION_DIAL);
                if (mRiaSupportModel != null) {
                    i.setData(Uri.parse("tel:" + mRiaSupportModel.getPhoneNumber()));
                } else {
                    i.setData(Uri.parse("tel:" + tvConsultantNumber.getText().toString()));
                }
                startActivity(i);
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Hotline.showConversations(HelpAndSupportActivity.this);
            }
        });

        btnSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                if (mRiaSupportModel != null) {
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mRiaSupportModel.getEmail()});
                } else {
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{tvEmail.getText().toString()});
                }

                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                }
            }
        });

        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HelpAndSupportActivity.this, "This feature will be available soon", Toast.LENGTH_SHORT).show();
            }
        });


        tvTextRia.setText(Methods.fromHtml("If your query is unanswered, please contact us at"));


        CharSequence charSequence = Methods.fromHtml("<a href=\"mailto:" + getString(R.string.settings_feedback_link) + "\">" + getString(R.string.settings_feedback_link) + "</a> " +
                "or call at " + getString(R.string.contact_us_number) +
                " or <a href=\"" + CHAT_INTENT_URI + "\"><u>CHAT</u></a>.");

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(charSequence);
        makeLinkClickable(spannableStringBuilder, charSequence);

        tvTextFaq1.setMovementMethod(LinkMovementMethod.getInstance());
        tvTextFaq1.setText(spannableStringBuilder);


        tvTextFaq2.setText(Methods.fromHtml("Product related queries, please refer to our <a href=\"" + getString(R.string.faqs_url) + "\">FAQs</a>"));
        tvTextFaq2.setMovementMethod(LinkMovementMethod.getInstance());

        pd = ProgressDialog.show(this, "", getString(R.string.please_wait));
    }

    protected void makeLinkClickable(SpannableStringBuilder sp, CharSequence charSequence) {

        URLSpan[] spans = sp.getSpans(0, charSequence.length(), URLSpan.class);

        for (final URLSpan urlSpan : spans) {

            if (urlSpan.getURL().equalsIgnoreCase(CHAT_INTENT_URI)) {

                ClickableSpan clickableSpan = new ClickableSpan() {
                    public void onClick(View view) {
                        Hotline.showConversations(HelpAndSupportActivity.this);
                    }
                };
                sp.setSpan(clickableSpan, sp.getSpanStart(urlSpan),
                        sp.getSpanEnd(urlSpan), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
           onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
