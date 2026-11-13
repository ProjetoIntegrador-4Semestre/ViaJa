package com.senac.viaja.ui.main
import android.view.View
import android.widget.*
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.senac.viaja.R
internal fun AppCompatActivity.journey(title: String,tab: PageTab?=null): LinearLayout{
    val body=LinearLayout(this).apply{
        orientation=LinearLayout.VERTICAL;
        setPadding(20.dp,16.dp,20.dp,24.dp);
        setBackgroundColor(getColor(R.color.vi_canvas))
    }
    val scroll=ScrollView(this).apply{
        isFillViewport=true;
        addView(body)
    }
    setPageContent(scroll,title,tab){
        finish()
    };
    return body
}
internal val android.content.Context.oneDp get()=resources.displayMetrics.density
internal val Int.dp get()=(this*android.content.res.Resources.getSystem().displayMetrics.density).toInt()
internal fun LinearLayout.words(text: String,size: Float=15f): TextView=TextView(context).also{
    it.text=text;
    it.textSize=size;
    it.setTextColor(context.getColor(R.color.vi_ink));
    it.setPadding(0,8.dp,0,8.dp);
    addView(it)
}
internal fun LinearLayout.press(text: String,action: ()->Unit): MaterialButton=MaterialButton(context).also{
    it.text=text;
    it.minHeight=48.dp;
    it.setTextColor(context.getColor(R.color.white));
    it.backgroundTintList=android.content.res.ColorStateList.valueOf(context.getColor(R.color.vi_primary));
    it.setOnClickListener{
        action()
    };
    addView(it,LinearLayout.LayoutParams(-1,-2).apply{
        topMargin=8.dp
    })
}
internal fun LinearLayout.entry(hint: String,value: String="",type: Int=InputType.TYPE_CLASS_TEXT): EditText=EditText(context).also{
    it.hint=hint;
    it.setText(value);
    it.inputType=type;
    it.setTextColor(context.getColor(R.color.vi_ink));
    it.setHintTextColor(context.getColor(R.color.vi_muted));
    it.minHeight=56.dp;
    addView(it)
}
