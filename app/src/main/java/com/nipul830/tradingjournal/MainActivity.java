package com.nipul830.tradingjournal;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout list; ArrayList<JSONObject> trades = new ArrayList<>(); android.content.SharedPreferences prefs;
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    TextView tv(String s,int size){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.BLACK); t.setPadding(dp(16),dp(12),dp(16),dp(12)); return t; }

    @Override public void onCreate(Bundle b){super.onCreate(b); prefs=getSharedPreferences("journal",0); load(); build();}

    void build(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(12),dp(20),dp(12),0);
        TextView title=tv("Trading Journal",24); title.setTypeface(null,1); root.addView(title);
        Button add=new Button(this); add.setText("+  Add Trade"); add.setOnClickListener(v->dialog(-1)); root.addView(add);
        ScrollView scroll=new ScrollView(this); list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); scroll.addView(list); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root); render();
    }
    void load(){String s=prefs.getString("trades","[]"); try{JSONArray a=new JSONArray(s); for(int i=0;i<a.length();i++) trades.add(a.getJSONObject(i));}catch(Exception ignored){}}
    void save(){JSONArray a=new JSONArray(); for(JSONObject o:trades)a.put(o); prefs.edit().putString("trades",a.toString()).apply();}
    void render(){
        list.removeAllViews();
        if(trades.isEmpty()){ TextView e=tv("No trades yet. Tap Add Trade.",16); e.setGravity(17); list.addView(e); return; }
        for(int i=0;i<trades.size();i++){ final int ix=i; JSONObject o=trades.get(i); LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
            String line=o.optString("symbol")+" | "+o.optString("side")+" | "+o.optString("entry")+" | "+o.optString("sl")+" | "+o.optString("tp")+" | "+o.optString("pip")+" | "+o.optString("status");
            TextView row=tv(line,15); row.setOnClickListener(v->dialog(ix)); card.addView(row);
            TextView hint=tv("Tap to edit • Long press to delete",12); hint.setTextColor(Color.GRAY); card.addView(hint);
            row.setOnLongClickListener(v->{new AlertDialog.Builder(this).setTitle("Delete trade?").setMessage(line).setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{trades.remove(ix);save();render();}).show();return true;});
            list.addView(card); View sep=new View(this); sep.setBackgroundColor(0xFFE0E0E0); list.addView(sep,new LinearLayout.LayoutParams(-1,dp(1)));
        }
    }
    void dialog(int edit){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),0,dp(20),0);
        String[] labels={"Symbol","Entry","SL","TP","Pip"}; EditText[] f=new EditText[5];
        for(int i=0;i<5;i++){f[i]=new EditText(this);f[i].setHint(labels[i]);f[i].setSingleLine(true);box.addView(f[i]);}
        Spinner side=new Spinner(this); side.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"BUY","SELL"})); box.addView(side);
        Spinner status=new Spinner(this); status.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"OPEN","TP HIT","SL HIT","CLOSED"})); box.addView(status);
        if(edit>=0){JSONObject o=trades.get(edit);f[0].setText(o.optString("symbol"));f[1].setText(o.optString("entry"));f[2].setText(o.optString("sl"));f[3].setText(o.optString("tp"));f[4].setText(o.optString("pip"));side.setSelection(o.optString("side").equals("SELL")?1:0);String st=o.optString("status");status.setSelection(Arrays.asList("OPEN","TP HIT","SL HIT","CLOSED").indexOf(st));}
        AlertDialog d=new AlertDialog.Builder(this).setTitle(edit<0?"Add Trade":"Edit Trade").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{try{JSONObject o=edit<0?new JSONObject():trades.get(edit);o.put("symbol",f[0].getText().toString().trim().toUpperCase());o.put("entry",f[1].getText().toString().trim());o.put("sl",f[2].getText().toString().trim());o.put("tp",f[3].getText().toString().trim());o.put("pip",f[4].getText().toString().trim());o.put("side",side.getSelectedItem().toString());o.put("status",status.getSelectedItem().toString());if(o.optString("symbol").isEmpty()){f[0].setError("Required");return;}if(edit<0)trades.add(0,o);save();render();d.dismiss();}catch(Exception e){Toast.makeText(this,"Could not save trade",Toast.LENGTH_SHORT).show();}}));
        d.show();
    }
}