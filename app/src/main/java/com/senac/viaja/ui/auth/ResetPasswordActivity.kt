package com.senac.viaja.ui.auth
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.core.widget.doAfterTextChanged
import com.senac.viaja.R
import com.senac.viaja.ui.main.*
class ResetPasswordActivity:AppCompatActivity(){
    private lateinit var state:RecoveryFlowState;
    private lateinit var body:android.widget.LinearLayout
    override fun onCreate(saved:Bundle?){
        setTheme(R.style.Theme_Viaja_Journey);
        super.onCreate(saved);
        body=journey("Redefinir senha");
        state=ViewModelProvider(this)[RecoveryFlowState::class.java];
        state.changed.observe(this){
            render()
        }
    }
    private fun render(){
        body.removeAllViews();
        body.words("Escolha uma nova senha",24f);
        body.words(state.error?:state.message.ifBlank{
            "Cole o código recebido por e-mail. Ele é temporário e só pode ser usado uma vez."
        });
        if(!state.complete){
            body.entry("Código",state.code).apply{
                isSaveEnabled=false;
                isEnabled=!state.busy;
                doAfterTextChanged{
                    state.code=it.toString()
                }
            };
            body.entry("Nova senha",state.password,android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD).apply{
                isSaveEnabled=false;
                isEnabled=!state.busy;
                doAfterTextChanged{
                    state.password=it.toString()
                }
            };
            body.entry("Confirme a senha",state.confirm,android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD).apply{
                isSaveEnabled=false;
                isEnabled=!state.busy;
                doAfterTextChanged{
                    state.confirm=it.toString()
                }
            };
            body.press("Salvar nova senha"){
                state.reset()
            }.isEnabled=!state.busy
        }else{
            com.senac.viaja.data.repository.AuthRepository(this).clearSession();
            body.press("Entrar novamente"){
                startActivity(Intent(this,LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish()
            }
        }
    }
}
