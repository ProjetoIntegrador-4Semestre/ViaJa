package com.senac.viaja.ui.auth
import androidx.lifecycle.*
import com.senac.viaja.data.model.*
import com.senac.viaja.data.remote.*
import retrofit2.*
class GoogleLoginState:ViewModel(){
    val changed=MutableLiveData(0);
    var pollToken="";
    var url:String?=null;
    var result:GoogleSession?=null;
    var busy=false;
    var error:String?=null;
    var message="Abra a autenticação do Google no navegador seguro."
    private fun update(){
        changed.value=(changed.value?:0)+1
    }
    fun start(){
        if(busy)return;
        busy=true;
        error=null;
        update();
        ApiClient.service.googleStart().deliver({
            pollToken=it.pollToken;
            url=it.authorizationUrl;
            busy=false;
            message="Conclua a autenticação no navegador e volte ao Viajá.";
            update()
        },{
            busy=false;
            error=it;
            update()
        })
    }
    fun check(){
        if(busy||pollToken.isBlank())return;
        busy=true;
        error=null;
        update();
        ApiClient.service.googleResult(GooglePoll(pollToken)).enqueue(object:Callback<GoogleSession>{
            override fun onResponse(call:Call<GoogleSession>,response:Response<GoogleSession>){
                busy=false;
                if(response.code()==202)message="Aguardando sua autorização no navegador." else if(response.isSuccessful&&response.body()!=null){
                    result=response.body();
                    pollToken=""
                }else{
                    error="Autenticação expirada ou não confirmada. Inicie novamente.";
                    pollToken=""
                };
                update()
            };
            override fun onFailure(call:Call<GoogleSession>,t:Throwable){
                busy=false;
                error="Não foi possível consultar a autenticação.";
                update()
            }
        })
    }
}
