package com.nipul830.tradingjournal;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout list;
    LinearLayout bottomNav;
    ArrayList<JSONObject> trades = new ArrayList<>();
    String selectedCategory = "GOLD";
    android.content.SharedPreferences prefs;

    final int BG = Color.rgb(246,248,251);
    final int TEXT = Color.rgb(18,24,38);
    final int MUTED = Color.rgb(105,114,130);
    final int BORDER = Color.rgb(226,230,237);
    final int ACCENT = Color.rgb(20,28,45);

    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}

    TextView text(String s,float size,int color){
        TextView t=new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        return t;
    }

    GradientDrawable bg(int color,float radius){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    GradientDrawable outline(int fill,int stroke,float radius){
        GradientDrawable g=bg(fill,radius);
        g.setStroke(dp(1),stroke);
        return g;
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        prefs=getSharedPreferences("journal",0);
        load();
        build();
    }

    void build(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(18),dp(18),dp(18),0);

        LinearLayout header=new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);

        TextView title=text("Trading Journal",28,TEXT);
        title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        header.addView(title);

        TextView sub=text("Track every trade. Keep it simple.",13,MUTED);
        sub.setPadding(0,dp(4),0,dp(16));
        header.addView(sub);
        root.addView(header);

        Button add=new Button(this);
        add.setText("+   ADD TRADE");
        add.setTextSize(14);
        add.setTextColor(Color.WHITE);
        add.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        add.setAllCaps(false);
        add.setBackground(bg(ACCENT,14));
        add.setPadding(dp(10),0,dp(10),0);
        add.setOnClickListener(v->dialog(-1));
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,dp(52));
        ap.bottomMargin=dp(18);
        root.addView(add,ap);

        TextView columns=text("SYMBOL     SIDE        ENTRY       SL          TP          PIP       STATUS",10,MUTED);
        columns.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        columns.setPadding(dp(12),0,dp(12),dp(8));
        root.addView(columns);

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        bottomNav=new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setPadding(dp(8),dp(8),dp(8),dp(8));
        bottomNav.setBackgroundColor(Color.WHITE);
        root.addView(bottomNav,new LinearLayout.LayoutParams(-1,dp(70)));
        addNavButton("GOLD", "Gold", 0);
        addNavButton("BTC", "Bitcoin", 1);
        addNavButton("FOREX", "Forex", 2);

        setContentView(root);
        updateNav();
        render();
    }

    void addNavButton(String key,String label,int index){
        Button b=new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setOnClickListener(v->{selectedCategory=key; updateNav(); render();});
        bottomNav.addView(b,new LinearLayout.LayoutParams(0,-1,1));
    }

    void updateNav(){
        for(int i=0;i<bottomNav.getChildCount();i++){
            Button b=(Button)bottomNav.getChildAt(i);
            String key=i==0?"GOLD":i==1?"BTC":"FOREX";
            b.setTextColor(key.equals(selectedCategory)?Color.WHITE:TEXT);
            b.setBackground(bg(key.equals(selectedCategory)?ACCENT:Color.WHITE,12));
        }
    }

    String categoryFor(String symbol){
        String s=symbol.toUpperCase(Locale.US);
        if(s.contains("GOLD")||s.contains("XAU")) return "GOLD";
        if(s.contains("BTC")) return "BTC";
        return "FOREX";
    }

    void load(){
        String s=prefs.getString("trades","[]");
        try{
            JSONArray a=new JSONArray(s);
            for(int i=0;i<a.length();i++) trades.add(a.getJSONObject(i));
        }catch(Exception ignored){}
    }

    void save(){
        JSONArray a=new JSONArray();
        for(JSONObject o:trades)a.put(o);
        prefs.edit().putString("trades",a.toString()).apply();
    }

    String value(JSONObject o,String key){
        String v=o.optString(key).trim();
        return v.isEmpty() ? "—" : v;
    }

    void render(){
        list.removeAllViews();

        ArrayList<JSONObject> visible=new ArrayList<>();
        for(JSONObject o:trades){
            String c=o.optString("category",categoryFor(o.optString("symbol")));
            if(c.equals(selectedCategory)) visible.add(o);
        }

        if(visible.isEmpty()){
            LinearLayout empty=new LinearLayout(this);
            empty.setOrientation(LinearLayout.VERTICAL);
            empty.setGravity(Gravity.CENTER);
            TextView e=text("No trades yet",18,TEXT);
            e.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            empty.addView(e);
            TextView h=text("Tap ADD TRADE to record your first trade.",13,MUTED);
            h.setPadding(0,dp(6),0,0);
            empty.addView(h);
            list.addView(empty,new LinearLayout.LayoutParams(-1,dp(260)));
            return;
        }

        for(int i=0;i<visible.size();i++){
            JSONObject o=visible.get(i);
            final int ix=trades.indexOf(o);

            LinearLayout card=new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(14),dp(14),dp(14),dp(12));
            card.setBackground(outline(Color.WHITE,BORDER,14));

            String symbol=value(o,"symbol");
            String side=value(o,"side");
            String entry=value(o,"entry");
            String sl=value(o,"sl");
            String tp=value(o,"tp");
            String pip=calculatedPips(o);
            String status=value(o,"status");

            LinearLayout top=new LinearLayout(this);
            top.setGravity(Gravity.CENTER_VERTICAL);

            TextView sym=text(symbol,18,TEXT);
            sym.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            top.addView(sym,new LinearLayout.LayoutParams(0,dp(34),1));

            TextView sideView=text(side,11,Color.WHITE);
            sideView.setGravity(Gravity.CENTER);
            sideView.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            sideView.setBackground(bg(side.equals("SELL")?Color.rgb(190,70,70):Color.rgb(35,145,95),8));
            LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(dp(58),dp(30));
            top.addView(sideView,sp);
            card.addView(top);

            String result=calculatedPips(o);
            int resultColor=result.startsWith("+")?Color.rgb(25,145,90):(result.equals("—")?MUTED:Color.rgb(205,65,65));
            TextView line=text(symbol+"  |  "+side+"  |  "+entry+"  |  "+sl+"  |  "+tp+"  |  "+result+"  |  "+status,12,resultColor);
            line.setTypeface(Typeface.MONOSPACE,Typeface.NORMAL);
            line.setSingleLine(true);
            line.setPadding(0,dp(10),0,dp(8));

            HorizontalScrollView hs=new HorizontalScrollView(this);
            hs.setHorizontalScrollBarEnabled(false);
            hs.addView(line);
            card.addView(hs);

            TextView statusView=text((status.equals("TP HIT")||status.equals("SL HIT"))?status+"  "+pip+" PIP":status,11,TEXT);
            statusView.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            statusView.setGravity(Gravity.CENTER);
            statusView.setPadding(dp(12),0,dp(12),0);
            int statusColor;
            if(status.equals("TP HIT")) statusColor=Color.rgb(224,244,234);
            else if(status.equals("SL HIT")) statusColor=Color.rgb(252,230,230);
            else if(status.equals("CLOSED")) statusColor=Color.rgb(232,236,243);
            else statusColor=Color.rgb(231,240,252);
            statusView.setBackground(bg(statusColor,8));
            card.addView(statusView,new LinearLayout.LayoutParams(-2,dp(30)));

            TextView hint=text("Tap to edit   •   Long press to delete",11,MUTED);
            hint.setPadding(0,dp(10),0,0);
            card.addView(hint);

            card.setOnClickListener(v->dialog(ix));
            card.setOnLongClickListener(v->{
                new AlertDialog.Builder(this)
                    .setTitle("Delete trade?")
                    .setMessage(symbol+" | "+side+" | "+entry+" | "+status)
                    .setNegativeButton("Cancel",null)
                    .setPositiveButton("Delete",(d,w)->{trades.remove(ix);save();render();})
                    .show();
                return true;
            });

            LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(132));
            cp.bottomMargin=dp(12);
            list.addView(card,cp);
        }
    }

    double num(String s){
        try{return Double.parseDouble(s.trim().replace(",",""));}catch(Exception e){return Double.NaN;}
    }

    double tradePips(JSONObject o){
        String status=o.optString("status");
        if(!status.equals("TP HIT") && !status.equals("SL HIT")) return Double.NaN;
        double entry=num(o.optString("entry"));
        double hit=num(status.equals("TP HIT")?o.optString("tp"):o.optString("sl"));
        if(Double.isNaN(entry)||Double.isNaN(hit)) return Double.NaN;
        return o.optString("side").equals("SELL") ? entry-hit : hit-entry;
    }

    String formatPips(double p){
        if(Math.abs(p-Math.rint(p))<0.000001) return String.valueOf((long)Math.rint(p));
        return String.format(Locale.US,"%.2f",p);
    }

    String calculatedPips(JSONObject o){
        double p=tradePips(o);
        if(Double.isNaN(p)) return "—";
        return (p>=0?"+":"")+formatPips(p);
    }

    void dialog(int edit){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20),0,dp(20),0);

        String[] labels={"Symbol","Entry","SL","TP","Pip"};
        EditText[] f=new EditText[4];

        for(int i=0;i<4;i++){
            f[i]=new EditText(this);
            f[i].setHint(labels[i]);
            f[i].setSingleLine(true);
            f[i].setTextSize(15);
            box.addView(f[i],new LinearLayout.LayoutParams(-1,dp(52)));
        }

        Spinner side=new Spinner(this);
        side.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"BUY","SELL"}));
        box.addView(side,new LinearLayout.LayoutParams(-1,dp(48)));

        Spinner status=new Spinner(this);
        status.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"OPEN","TP HIT","SL HIT","CLOSED"}));
        box.addView(status,new LinearLayout.LayoutParams(-1,dp(48)));

        if(edit>=0){
            JSONObject o=trades.get(edit);
            f[0].setText(o.optString("symbol"));
            f[1].setText(o.optString("entry"));
            f[2].setText(o.optString("sl"));
            f[3].setText(o.optString("tp"));
            side.setSelection(o.optString("side").equals("SELL")?1:0);
            String st=o.optString("status");
            int p=Arrays.asList("OPEN","TP HIT","SL HIT","CLOSED").indexOf(st);
            status.setSelection(Math.max(0,p));
        }

        AlertDialog d=new AlertDialog.Builder(this)
            .setTitle(edit<0?"Add Trade":"Edit Trade")
            .setView(box)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Save",null)
            .create();

        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{
            try{
                JSONObject o=edit<0?new JSONObject():trades.get(edit);
                String symbolInput=f[0].getText().toString().trim().toUpperCase();
                o.put("symbol",symbolInput);
                o.put("category",categoryFor(symbolInput));
                o.put("entry",f[1].getText().toString().trim());
                o.put("sl",f[2].getText().toString().trim());
                o.put("tp",f[3].getText().toString().trim());
                o.put("side",side.getSelectedItem().toString());
                o.put("status",status.getSelectedItem().toString());
                o.put("pip",calculatedPips(o));

                if(o.optString("symbol").isEmpty()){
                    f[0].setError("Required");
                    return;
                }

                if(edit<0)trades.add(0,o);
                save();
                render();
                d.dismiss();
            }catch(Exception e){
                Toast.makeText(this,"Could not save trade",Toast.LENGTH_SHORT).show();
            }
        }));
        d.show();
    }
}