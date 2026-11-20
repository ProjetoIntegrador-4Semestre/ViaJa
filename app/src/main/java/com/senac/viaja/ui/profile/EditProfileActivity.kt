package com.senac.viaja.ui.profile
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senac.viaja.R
import com.senac.viaja.ui.main.*
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.model.ViajaUser
class EditProfileActivity: AppCompatActivity(){
    private lateinit var state:ProfileEditState;
    private lateinit var body:android.widget.LinearLayout;
    private var owner=0L
    private val requestedMode by lazy { intent.getStringExtra(EXTRA_MODE) }
    private var scrollToInterests = false
    override fun onCreate(savedInstanceState:Bundle?){
        setTheme(R.style.Theme_Viaja_Journey);
        super.onCreate(savedInstanceState);
        val user=AuthRepository(this).getUser()?:run{
            finish();
            return
        };
        owner=user.id;
        scrollToInterests = savedInstanceState?.getBoolean(EXTRA_INTERESTS)
            ?: intent.getBooleanExtra(EXTRA_INTERESTS, false)
        body=journey("Seu perfil");
        state=ViewModelProvider(this)[ProfileEditState::class.java];
        if(!state.ready){
            if(savedInstanceState?.getLong("owner")==owner && savedInstanceState.getBoolean("hasDraft")){
                state.restoredForm=true;
                state.mode=savedInstanceState.getString("mode")?:state.mode;
                state.selected=savedInstanceState.getLongArray("interests")?.toMutableSet()?:state.selected;
                state.name=savedInstanceState.getString("name")?:user.name.orEmpty();
                state.email=savedInstanceState.getString("email")?:user.email
            } else {
                state.mode=requestedMode?:user.defaultMode?:"tourism";
                state.name=user.name.orEmpty();
                state.email=user.email
            }
        };
        state.changed.observe(this){
            render()
        };
        if(!state.ready)state.load(requestedMode)
    }
    override fun onSaveInstanceState(out:Bundle){
        out.putLong("owner",owner);
        out.putBoolean("hasDraft",state.ready||state.restoredForm);
        out.putString("mode",state.mode);
        out.putLongArray("interests",state.selected.toLongArray());
        out.putString("name",state.name);
        out.putString("email",state.email);
        out.putBoolean(EXTRA_INTERESTS, scrollToInterests)
        super.onSaveInstanceState(out)
    }
    private fun render(){
        if(isFinishing||isDestroyed)return;
        val auth=AuthRepository(this);
        if(auth.getUser()?.id!=owner){
            finish();
            return
        };
        state.result?.let{
            state.result=null;
            if(it.user.id==owner&&auth.install(it.token,ViajaUser(it.user.id,it.user.email,it.user.name,it.user.defaultMode))){
                setResult(RESULT_OK);
                finish();
                return
            }else{
                state.error="Não foi possível renovar a sessão."
            }
        }
        body.removeAllViews();
        body.words("Preferências que acompanham suas próximas viagens",22f);
        body.words(state.error?:if(state.busy)"Consultando sua conta…" else "Suas viagens existentes mantêm o próprio modo.")
        body.entry("Nome",state.name).apply{
            isEnabled=!state.busy;
            doAfterTextChanged{
                state.name=it.toString()
            }
        }
        body.entry("E-mail",state.email,InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS).apply{
            isEnabled=!state.busy;
            doAfterTextChanged{
                state.email=it.toString()
            }
        }
        body.entry("Senha atual · necessária para trocar o e-mail",state.password,InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD).apply{
            isSaveEnabled=false;
            isEnabled=!state.busy;
            doAfterTextChanged{
                state.password=it.toString()
            }
        }
        body.press("Modo inicial: ${if(state.mode=="work")"Trabalho" else "Turismo"}"){
            MaterialAlertDialogBuilder(this).setItems(arrayOf("Turismo","Trabalho")){
                _,index->state.mode=if(index==1)"work" else "tourism";
                state.notifyChanged()
            }.show()
        }.isEnabled=!state.busy
        val interestsHeading = body.words(getString(R.string.profile_interests), 19f)
        state.options.forEach{
            option->body.addView(MaterialCheckBox(this).apply{
                text=option.name;
                isChecked=option.id in state.selected;
                isEnabled=!state.busy;
                setOnCheckedChangeListener{
                    _,checked->if(checked)state.selected.add(option.id)else state.selected.remove(option.id)
                }
            })
        }
        body.press("Salvar perfil"){
            state.save()
        }.isEnabled=state.ready&&!state.busy
        body.press("Tentar carregar novamente"){
            state.load(requestedMode)
        }.isEnabled=!state.busy
        if (scrollToInterests && state.ready) {
            body.post {
                if (interestsHeading.parent == body) {
                    (body.parent as ScrollView).scrollTo(0, interestsHeading.top)
                    scrollToInterests = false
                }
            }
        }
    }

    companion object {
        private const val EXTRA_MODE = "profile_mode"
        private const val EXTRA_INTERESTS = "profile_open_interests"

        internal fun open(context: Context, mode: String?, interests: Boolean): Intent =
            Intent(context, EditProfileActivity::class.java)
                .putExtra(EXTRA_MODE, mode)
                .putExtra(EXTRA_INTERESTS, interests)
    }
}
