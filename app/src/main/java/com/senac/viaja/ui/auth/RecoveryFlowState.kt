package com.senac.viaja.ui.auth
import androidx.lifecycle.*
import com.senac.viaja.data.remote.*
import com.senac.viaja.data.model.*
class RecoveryFlowState:ViewModel(){
    val changed=MutableLiveData(0);
    var email="";
    var code="";
    var password="";
    var confirm="";
    var busy=false;
    var error:String?=null;
    var message="";
    var complete=false
    private fun update(){
        changed.value=(changed.value?:0)+1
    }
    fun request(){
        if(busy)return;
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()){
            error="Informe um e-mail válido.";
            update();
            return
        };
        busy=true;
        error=null;
        update();
        ApiClient.service.recovery(RecoveryWrite(email.trim())).deliver({
            busy=false;
            message=it.message.orEmpty();
            update()
        },{
            busy=false;
            error=it;
            update()
        })
    }
    fun reset(){
        if(busy)return;
        if(code.isBlank()||password.length<8||password.toByteArray(Charsets.UTF_8).size>72||password!=confirm){
            error="Confira o código e duas senhas iguais, de 8 a 72 bytes.";
            update();
            return
        };
        busy=true;
        error=null;
        update();
        ApiClient.service.reset(ResetWrite(code.trim(),password)).deliver({
            busy=false;
            password="";
            confirm="";
            code="";
            message=it.message.orEmpty();
            complete=true;
            update()
        },{
            busy=false;
            error=it;
            update()
        })
    }
}
