package com.example.linkfbla;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaMailAPI extends AsyncTask<Void,Void,Boolean>  {

    static final String EMAIL_TO = "hughmarch03@gmail.com";
    static final String EMAIL = "linkfbla.do.not.reply@gmail.com";
    static final String PASSWORD = "VbM0pHyCmL";

    private Context mContext;
    private Session mSession;

    private String mSubject;
    private String mMessage;

    private ProgressDialog mProgressDialog;

    interface OnCompleteListener {
        void onComplete(boolean successful);
    }

    private OnCompleteListener listener;

    public JavaMailAPI(Context mContext, String mSubject, String mMessage, OnCompleteListener listener) {
        this.mContext = mContext;
        this.mSubject = mSubject;
        this.mMessage = mMessage;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Show progress dialog while sending email
        mProgressDialog = ProgressDialog.show(mContext,"Sending message", "Please wait...",false,false);
    }

    @Override
    protected void onPostExecute(Boolean successful) {
        super.onPostExecute(successful);
        //Dismiss progress dialog when message successfully send
        mProgressDialog.dismiss();

        listener.onComplete(successful);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        //Creating properties
        Properties props = new Properties();

        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        //Creating a new session
        mSession = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    //Authenticating the password
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL, PASSWORD);
                    }
                });

        try {
            //Creating MimeMessage object
            MimeMessage mm = new MimeMessage(mSession);

            //Setting sender address
            mm.setFrom(new InternetAddress(EMAIL));
            //Adding receiver
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(EMAIL_TO));
            //Adding subject
            mm.setSubject(mSubject);
            //Adding message
            mm.setText(mMessage);
            //Sending email
            Transport.send(mm);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}