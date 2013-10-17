package com.example.news;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

public class MyActivity extends Activity {
    ArrayList<Rss> rssItems;
    ArrayAdapter<Rss> arrayAdapter;
    ListView rsslistView;
    URL url;
    MyTask mt;

    public class MyTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(Void... voids) {
            HttpURLConnection conn=null;
            try {
                conn = (HttpURLConnection) url.openConnection();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String tag_item="";
                    String tag_title="";
                    String tag_description="";
                    String tag_pudDate="";
                    String tag_link="";

                    InputStream is = conn.getInputStream();

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();

                    Document document = db.parse(is);
                    Element element = document.getDocumentElement();
                    NodeList nodeList = element.getElementsByTagName("item");
                    if (nodeList.getLength()>0){
                        tag_item="item";
                        tag_title="title";
                        tag_description="description";
                        tag_pudDate="pubDate";
                        tag_link="link";
                    }else
                    {
                        nodeList = element.getElementsByTagName("entry");
                        tag_item="entry";
                        tag_title="title";
                        tag_description="summary";
                        tag_pudDate="updated";
                        tag_link="link";
                    }


                    if (nodeList.getLength() > 0) {
                        for (int i = 0; i < nodeList.getLength(); i++) {

                            Element entry = (Element) nodeList.item(i);

                            Element _titleE = (Element) entry.getElementsByTagName(tag_title).item(0);
                            Element _descriptionE = (Element) entry.getElementsByTagName(tag_description).item(0);
                            Element _pubDateE = (Element) entry.getElementsByTagName(tag_pudDate).item(0);
                            Element _linkE = (Element) entry.getElementsByTagName(tag_link).item(0);
                            String _title = "";
                            String _description = "";
                            Date _pubDate =new Date();
                            String _link = "";
                            try{
                            _title = _titleE.getFirstChild().getNodeValue();
                            }catch (Exception e)
                            {

                            }
                            try{
                            _description = _descriptionE.getFirstChild().getNodeValue();
                            }catch (Exception e){

                            }
                            try{
                            _pubDate = new Date(_pubDateE.getFirstChild().getNodeValue());
                            }catch (Exception e){

                            }
                            try {
                            _link = _linkE.getFirstChild().getNodeValue();
                            }catch (Exception e){

                            }
                            Rss rssItem = new Rss(_title, _description, _pubDate, _link);

                            rssItems.add(rssItem);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            rsslistView.setAdapter(arrayAdapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        rsslistView = (ListView) findViewById(R.id.listView);
        rssItems = new ArrayList<Rss>();
        arrayAdapter = new ArrayAdapter<Rss>(this, R.layout.list_item, rssItems);
        final Button button = (Button) findViewById(R.id.button);
        final EditText editText = (EditText) findViewById(R.id.editText);
        editText.setText("http://stackoverflow.com/feeds/tag/android");
        try {
            url = new URL(editText.getText().toString());
            refresh();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error", 5000);
        }

        refresh();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rssItems.clear();
                rsslistView.setAdapter(arrayAdapter);
                try {
                    url = new URL(editText.getText().toString());
                    refresh();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error", 5000);
                }
            }
        });

        rsslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long arg) {
                Rss rssItem = rssItems.get(index);
                Intent intent = new Intent();
                intent.putExtra("Title", rssItem.toString());
                intent.putExtra("Content", rssItem.getDescription()+"<br>"+"<a href=\""  + rssItem.getLink()+ "\">Link</a>");
                intent.setClass(getApplicationContext(), RssFull.class);
                startActivity(intent);

            }
        });


    }

    void refresh() {
        rssItems.clear();
        mt = new MyTask();
        mt.execute();
    }
}