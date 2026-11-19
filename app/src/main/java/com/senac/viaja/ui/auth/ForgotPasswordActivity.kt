package com.senac.viaja.ui.auth
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.core.widget.doAfterTextChanged
import com.senac.viaja.R
import com.senac.viaja.ui.main.*
class ForgotPasswordActivity:AppCompatActivity(){
    private lateinit var state:RecoveryFlowState;
    private lateinit var body:android.widget.LinearLayout
    override fun onCreate(saved:Bundle?){
        setTheme(R.style.Theme_Viaja_Journey);
        super.onCreate(saved);
        body=journey("Recuperar acesso");
        state=ViewModelProvider(this)[RecoveryFlowState::class.java];
        state.changed.observe(this){
            render()
        }
    }
    private fun render(){
        body.removeAllViews();
        body.words("Vamos recuperar seu acesso",24f);
        body.words(state.error?:state.message.ifBlank{
            "Informe o e-mail usado na sua conta."
        });
        body.entry("E-mail",state.email,android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS).apply{
            isEnabled=!state.busy;
            doAfterTextChanged{
                state.email=it.toString()
            }
        };
        body.press(if(state.busy)"Solicitando…" else "Solicitar instruções"){
            state.request()
        }.isEnabled=!state.busy;
        body.press("Já tenho um código"){
            startActivity(Intent(this,ResetPasswordActivity::class.java))
        }
    }
}
