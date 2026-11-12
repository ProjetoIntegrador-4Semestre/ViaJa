package com.senac.viaja.data.remote
import retrofit2.*
internal fun <T> Call<T>.deliver(success: (T)->Unit,error: (String)->Unit){
    enqueue(object: Callback<T>{
        override fun onResponse(call: Call<T>,response: Response<T>){
            val value=response.body();
            if(response.isSuccessful&&value!=null)success(value)else error(when(response.code()){
                401->"Entre novamente para continuar.";
                403->"A operação exige confirmação de acesso ou um plano com esse recurso.";
                404,405->"Este recurso ainda não está disponível no servidor.";
                409->"Os dados mudaram. Atualize e confira antes de tentar novamente.";
                429->"Limite de uso atingido. Tente mais tarde.";
                503->"O serviço está temporariamente indisponível.";
                else->"Não foi possível concluir (${response.code()}). Seus dados foram mantidos."
            })
        }
        override fun onFailure(call: Call<T>,failure: Throwable){
            if(!call.isCanceled)error("Não foi possível confirmar a operação. Confira sua conexão e tente novamente.")
        }
    })
}
