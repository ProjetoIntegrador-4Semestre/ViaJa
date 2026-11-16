package com.senac.viaja.ui.trips
import androidx.lifecycle.*
import kotlinx.coroutines.*
import com.senac.viaja.data.model.*
import com.senac.viaja.data.remote.*
import com.senac.viaja.data.repository.OfflineRepository
class TripResourcesState:ViewModel(){
    val changed=MutableLiveData(0);
    var owner=0L;
    var tripId=0L;
    var points=emptyList<MapStop>();
    var weather:WeatherView?=null;
    var error:String?=null;
    var busy=false;
    var pdf:TripBundle?=null;
    var mapReady=false;
    var weatherError:String?=null;
    var message=""
    private var remainingLoads=0
    private fun finishLoad(){
        remainingLoads--;
        busy=remainingLoads>0;
        notifyChanged()
    }
    fun notifyChanged(){
        changed.value=(changed.value?:0)+1
    }
    fun load(){
        if(busy)return;
        busy=true;
        remainingLoads=2;
        error=null;
        weatherError=null;
        notifyChanged();
        ApiClient.service.tripMap(tripId).deliver({
            points=it;
            mapReady=true;
            finishLoad()
        },{
            error=it;
            finishLoad()
        });
        ApiClient.service.tripWeather(tripId).deliver({
            weather=it;
            weatherError=null;
            finishLoad()
        },{
            weatherError=it;
            finishLoad()
        })
    }
    fun export(full:Boolean){
        if(busy)return;
        busy=true;
        error=null;
        notifyChanged();
        ApiClient.service.tripExport(tripId,full).deliver({
            pdf=it;
            busy=false;
            notifyChanged()
        },{
            error=it;
            busy=false;
            notifyChanged()
        })
    }
    fun writePdf(context:android.content.Context,uri:android.net.Uri,bundle:TripBundle){
        if(busy)return
        busy=true
        error=null
        message="Exportando PDF…"
        notifyChanged()
        val app=context.applicationContext
        viewModelScope.launch{
            runCatching{
                withContext(Dispatchers.IO){
                    require(com.senac.viaja.data.repository.AuthRepository(app).getUser()?.id==owner)
                    app.contentResolver.openOutputStream(uri)?.use{ TripPdf.write(bundle,it) }
                        ?:throw IllegalStateException("Destino indisponível")
                }
            }.onSuccess{ message="PDF exportado." }.onFailure{
                message=""
                error="Não foi possível exportar o PDF. Escolha o destino e tente novamente."
            }
            busy=false
            notifyChanged()
        }
    }
    fun download(context:android.content.Context){
        if(busy)return;
        busy=true;
        error=null;
        notifyChanged();
        ApiClient.service.tripOffline(tripId).deliver({
            bundle->viewModelScope.launch{
                runCatching{
                    withContext(Dispatchers.IO){
                        OfflineRepository(context.applicationContext).save(bundle)
                    }
                }.onSuccess{
                    message="Viagem baixada para leitura offline."
                }.onFailure{
                    error=it.message
                };
                busy=false;
                notifyChanged()
            }
        },{
            error=it;
            busy=false;
            notifyChanged()
        })
    }
}
