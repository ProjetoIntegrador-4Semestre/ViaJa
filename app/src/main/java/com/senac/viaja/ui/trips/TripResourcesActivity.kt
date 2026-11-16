package com.senac.viaja.ui.trips
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.senac.viaja.R
import com.senac.viaja.ui.main.*
import com.senac.viaja.data.repository.AuthRepository
class TripResourcesActivity:AppCompatActivity(){
    private lateinit var state:TripResourcesState;
    private lateinit var body:android.widget.LinearLayout;
    private var web:WebView?=null;
    private var mapSignature="";
    private var launching=false
    private val pdfDestination=registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){
        uri->
        launching=false
        if(::state.isInitialized){
            val bundle=state.pdf
            state.pdf=null
            when{
                uri==null->{ state.message="Exportação cancelada."; state.notifyChanged() }
                AuthRepository(this).getUser()?.id!=state.owner->finish()
                bundle==null->{ state.error="A exportação foi interrompida. Solicite o PDF novamente."; state.notifyChanged() }
                else->state.writePdf(applicationContext,uri,bundle)
            }
        }
    }

    override fun onCreate(saved:Bundle?){
        setTheme(R.style.Theme_Viaja_Journey);
        super.onCreate(saved);
        val user=AuthRepository(this).getUser()?:run{
            finish();
            return
        };
        val tripId=intent.getLongExtra(TRIP_ID,-1).takeIf{
            it>0
        }?:run{
            finish();
            return
        };
        body=journey("Mapa, clima e arquivos");
        state=ViewModelProvider(this)[TripResourcesState::class.java];
        state.owner=user.id;
        state.tripId=tripId;
        launching=saved?.getBoolean("launching")?:false;
        state.changed.observe(this){
            render()
        };
        if(!state.mapReady&&!state.busy&&!launching&&state.pdf==null)state.load()
    }
    override fun onSaveInstanceState(out:Bundle){
        out.putBoolean("launching",launching);
        super.onSaveInstanceState(out)
    }
    override fun onResume(){
        super.onResume();
        if(::state.isInitialized&&AuthRepository(this).getUser()?.id!=state.owner)finish()
    }
    override fun onDestroy(){
        web?.destroy();
        super.onDestroy()
    }
    private fun render(){
        if(isFinishing||isDestroyed)return;
        if(AuthRepository(this).getUser()?.id!=state.owner){finish();return}
        body.removeAllViews();
        body.words(state.error?:if(state.busy)"Consultando dados…" else state.message);
        body.press("Atualizar mapa e clima"){
            state.load()
        }.isEnabled=!state.busy
        val points=MapHtml.valid(state.points);
        body.words("Mapa da viagem",22f);
        if(points.isEmpty())body.words(if(state.mapReady)"Não há paradas com coordenadas disponíveis." else "Carregando paradas…")else{
            val map=web?:WebView(this).also{
                web=it;
                it.settings.javaScriptEnabled=true;
                it.settings.allowFileAccess=false;
                it.settings.allowContentAccess=false;
                it.settings.domStorageEnabled=false;
                it.settings.mixedContentMode=android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW;
                it.settings.userAgentString=it.settings.userAgentString+" Viaja/1.0";
                it.webViewClient=object:WebViewClient(){
                    override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest):Boolean{
                        return true
                    };
                    override fun onReceivedError(view:WebView,request:WebResourceRequest,error:WebResourceError){
                        if(request.isForMainFrame)state.message="O mapa não carregou. Use a navegação externa abaixo."
                    }
                }
            }
            val html=MapHtml.render(points)
            if(mapSignature!=html){
                mapSignature=html;
                map.loadDataWithBaseURL("https://viaja.invalid/",html,"text/html","UTF-8",null)
            }
            (map.parent as? android.view.ViewGroup)?.removeView(map);
            body.addView(map,android.widget.LinearLayout.LayoutParams(-1,320.dp));
            body.words("© OpenStreetMap contributors · mapa online",12f)
            points.forEach{
                point->body.press("Navegar: ${point.name}"){
                    runCatching{
                        startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("geo:${point.lat},${point.lng}?q=${point.lat},${point.lng}")))
                    }.onFailure{
                        state.error="Nenhum aplicativo de mapas disponível.";
                        state.notifyChanged()
                    }
                }
            }
        }
        body.words("Previsão disponível para o destino",22f);
        val weather=state.weather;
        if(weather!=null&&state.weatherError!=null)body.words("Não foi possível atualizar a previsão: ${state.weatherError}. Abaixo está a última consulta disponível.",12f)
        if(weather!=null){
            body.words("${weather.source} · atualização ${weather.fetchedAt} · ${weather.timezone}",12f);
            weather.days.forEach{
                body.words("${TripFormRules.displayDate(it.date)} · mínima ${it.minimum?.toString()?:"--"} °C · máxima ${it.maximum?.toString()?:"--"} °C")
            };
            body.words("Previsão aproximada para um ponto do destino. As datas acima podem ser diferentes das datas da viagem.",12f)
        }else body.words(state.weatherError?:"Consultando previsão…")
        body.words("Leve o planejamento com você",22f);
        body.press("Exportar PDF do roteiro"){
            state.export(false)
        }.isEnabled=!state.busy;
        body.press("Exportar PDF completo · Premium"){
            state.export(true)
        }.isEnabled=!state.busy;
        body.press("Baixar viagem para leitura offline · Premium"){
            state.download(applicationContext)
        }.isEnabled=!state.busy;
        body.press("Abrir viagens baixadas"){
            startActivity(Intent(this,OfflineTripsActivity::class.java))
        }
        if(state.pdf!=null&&!launching){
            launching=true;
            pdfDestination.launch("viaja-roteiro.pdf")
        }
    }
    companion object{
        const val TRIP_ID="resources_trip_id"
    }
}
