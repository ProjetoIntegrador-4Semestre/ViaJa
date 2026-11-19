package com.senac.viaja.ui.auth
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.senac.viaja.R
import com.senac.viaja.ui.main.*
import com.senac.viaja.data.repository.*
class GoogleLoginActivity:AppCompatActivity(){
    private lateinit var state:GoogleLoginState;
    private lateinit var body:android.widget.LinearLayout
    override fun onCreate(saved:Bundle?){
        setTheme(R.style.Theme_Viaja_Journey);
        super.onCreate(saved);
        body=journey("Entrar com Google");
        state=ViewModelProvider(this)[GoogleLoginState::class.java];
        if(state.pollToken.isBlank())state.pollToken=saved?.getString("poll")?.let{
            runCatching{
                SessionCipher.decrypt(it)
            }.getOrNull()
        }.orEmpty();
        state.changed.observe(this){
            render()
        }
    }
    override fun onSaveInstanceState(out:Bundle){
        if(state.pollToken.isNotBlank())out.putString("poll",SessionCipher.encrypt(state.pollToken));
        super.onSaveInstanceState(out)
    }
    override fun onResume(){
        super.onResume();
        if(::state.isInitialized&&state.pollToken.isNotBlank())state.check()
    }
    private fun render(){
        if(isFinishing||isDestroyed)return;
        state.result?.let{
            state.result=null;
            if(AuthRepository(this).install(it.token,it.user)){
                val destination = if (intent.getBooleanExtra(LoginActivity.RETURN_TO_SUBSCRIPTION, false)) {
                    LoginActivity.subscriptionAfterLogin(this)
                } else {
                    Intent(this, HomeActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(destination)
                finish();
                return
            }else state.error="Sessão inválida. Tente novamente."
        };
        body.removeAllViews();
        body.words("Sua conta Google",24f);
        body.words(state.error?:if(state.busy)"Consultando autenticação…" else state.message);
        body.press("Abrir autenticação Google"){
            state.start()
        }.isEnabled=!state.busy;
        body.press("Já autorizei · verificar"){
            state.check()
        }.isEnabled=!state.busy&&state.pollToken.isNotBlank();
        state.url?.let{
            state.url=null;
            val uri=android.net.Uri.parse(it);
            if(uri.scheme=="https"&&uri.host=="accounts.google.com")runCatching{
                startActivity(Intent(Intent.ACTION_VIEW,uri))
            }.onFailure{
                state.error="Nenhum navegador disponível."
            }else state.error="Endereço de autenticação inválido."
        }
    }
}
