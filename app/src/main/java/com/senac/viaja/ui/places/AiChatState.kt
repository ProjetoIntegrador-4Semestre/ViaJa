package com.senac.viaja.ui.places
import android.app.Application
import androidx.lifecycle.*
import com.senac.viaja.data.model.*
import com.senac.viaja.data.remote.*
import com.senac.viaja.data.repository.*
import java.text.SimpleDateFormat
import java.util.*
class AiChatState(app:Application):AndroidViewModel(app){
    val changed=MutableLiveData(0);
    var initialized=false;
    var owner:Long?=null;
    val sessions=mutableListOf<ChatSession>();
    var activeId:String?=null;
    val session get()=sessions.first{
        it.id==activeId
    };
    var busy=false;
    var error:String?=null;
    var quota:AiQuota?=null;
    var proposal:AiTurn?=null;
    var names=emptyMap<Long,String>();
    private var call:retrofit2.Call<AiTurn>?=null;
    private var requestGeneration=0
    private val auth=AuthRepository(app);
    private var store:ChatSessionRepository?=null
    fun notifyChanged(){
        changed.value=(changed.value?:0)+1
    }
    fun initialize(tripId:Long?,tripName:String,mode:String){
        if(initialized)return;
        owner=auth.getUser()?.id;
        store=ChatSessionRepository(getApplication(),owner);
        sessions.addAll(store!!.load());
        activeId=sessions.filter{
            it.tripId==tripId
        }.maxByOrNull{
            it.updatedAt
        }?.id;
        if(activeId==null)create(tripId,tripName,mode);
        initialized=true;
        refreshQuota()
        if(session.messages.any { it.requestId!=null })activate(session.id)
    }
    fun create(tripId:Long?,tripName:String,mode:String){
        if(busy)return;
        val item=ChatSession(tripId=tripId,tripName=tripName,mode=mode);
        item.messages.add(ChatMessage("Sou a Vi. Vamos planejar sua viagem? As propostas só alteram o roteiro depois da sua aprovação.",false,"", "local"));
        sessions.add(item);
        activeId=item.id;
        proposal=null;
        persist();
        notifyChanged()
    }
    fun persist(){
        if(auth.getUser()?.id==owner)store?.save(sessions)
    }
    fun refreshQuota(){
        ApiClient.service.aiQuota().deliver({
            quota=it;
            notifyChanged()
        },{
            error=it;
            notifyChanged()
        })
    }
    private fun time()=SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date())
    fun send(kind:String="chat",days:Int?=null){
        if(busy||owner==null)return;
        val current=session;
        val text=current.draft.trim();
        if(text.isBlank())return;
        if(text.length>2000){
            error="Use até 2000 caracteres por mensagem.";
            notifyChanged();
            return
        };
        val request=current.pending?.takeIf{
            it.text==text&&it.kind==kind&&it.mode==current.mode&&it.pace==current.pace&&(kind=="chat"||it.days==days)
        }?:AiMessageWrite(UUID.randomUUID().toString(),text,kind,days,current.mode,current.pace);
        current.pending=request;
        persist();
        busy=true;
        error=null;
        val generation=++requestGeneration;
        notifyChanged()
        ApiClient.service.aiCreate(current.id,AiSessionWrite(current.tripId)).deliver({
            if(generation!=requestGeneration)return@deliver
            current.messages.removeAll{
                it.requestId==request.requestId
            };
            current.messages.add(ChatMessage(text,true,time(),"sending",request.requestId));
            persist();
            notifyChanged();
            val network=ApiClient.service.aiSend(current.id,request);
            call=network
            network.deliver({
                turn->if(generation==requestGeneration){
                    busy=false;
                    current.messages.removeAll{
                        it.requestId==request.requestId
                    };
                    current.messages.add(ChatMessage(turn.prompt,true,time(),"sent",turn.requestId));
                    current.messages.add(ChatMessage(turn.reply,false,time(),"received",turn.requestId));
                    current.pending=null;
                    current.draft="";
                    current.updatedAt=System.currentTimeMillis();
                    current.title=turn.prompt.take(48);
                    proposal=turn.takeIf{
                        it.stops.isNotEmpty()
                    };
                    persist();
                    if(current.tripId!=null)TripRepository().itineraryPlaces(current.tripId,{
                        items->names=items.associate{
                            it.id to it.name
                        };
                        notifyChanged()
                    },{
                    });
                    notifyChanged();
                    refreshQuota()
                }
            },{
                if(generation==requestGeneration){
                    busy=false;
                    error=it;
                    current.messages.replaceAll{
                        m->if(m.requestId==request.requestId)m.copy(delivery="not_sent") else m
                    };
                    persist();
                    notifyChanged()
                }
            })
        },{
            if(generation==requestGeneration){
                busy=false;
                error=it;
                notifyChanged()
            }
        })
    }
    fun cancel(){
        requestGeneration++;
        call?.cancel();
        session.messages.replaceAll { if(it.delivery=="sending")it.copy(delivery="not_sent") else it };
        busy=false;
        error="Espera interrompida. O servidor pode concluir o pedido; retome o mesmo texto para consultar o resultado sem duplicá-lo.";
        persist();
        notifyChanged()
    }
    fun remoteHistory(onLoaded:()->Unit={
    }){
        if(busy)return;
        ApiClient.service.aiSessions().deliver({
            items->items.forEach{
                remote->if(sessions.none{
                    it.id==remote.id
                })sessions.add(ChatSession(id=remote.id,title=remote.title,tripId=remote.tripId,tripName=remote.title))
            };
            persist();
            notifyChanged();
            onLoaded()
        },{
            error=it;
            notifyChanged();
            onLoaded()
        })
    }
    fun activate(id:String){
        if(busy)return;
        activeId=id;
        proposal=null;
        notifyChanged();
        ApiClient.service.aiHistory(id).deliver({
            turns->if(activeId==id&&!busy){
                val current=session;
                val unsent=current.messages.filter{
                    it.delivery=="not_sent"
                };
                current.messages.clear();
                turns.forEach{
                    t->current.messages.add(ChatMessage(t.prompt,true,"","sent",t.requestId));
                    current.messages.add(ChatMessage(t.reply,false,"","received",t.requestId))
                };
                current.messages.addAll(unsent.filter{
                    m->turns.none{
                        it.requestId==m.requestId
                    }
                });
                proposal=turns.lastOrNull{
                    it.stops.isNotEmpty()
                };
                current.tripId?.let { trip -> TripRepository().itineraryPlaces(trip,{ items -> names=items.associate { it.id to it.name }; notifyChanged() },{}) };
                persist();
                notifyChanged()
            }
        },{
            error=it;
            notifyChanged()
        })
    }
    fun apply(){
        val turn=proposal?:return;
        if(busy||turn.applied)return;
        busy=true;
        error=null;
        notifyChanged();
        ApiClient.service.aiApply(session.id,turn.requestId).deliver({
            proposal=it;
            busy=false;
            notifyChanged()
        },{
            busy=false;
            error=it;
            notifyChanged()
        })
    }
    fun delete(){
        if(busy)return;
        val id=session.id;
        busy=true;
        notifyChanged();
        ApiClient.service.aiDelete(id).enqueue(object:retrofit2.Callback<Void>{
            override fun onResponse(call:retrofit2.Call<Void>,response:retrofit2.Response<Void>){
                busy=false;
                if(response.isSuccessful){
                    val old=session;
                    sessions.removeAll{
                        it.id==id
                    };
                    create(old.tripId,old.tripName,old.mode)
                }else{
                    error="Não foi possível excluir a conversa no servidor.";
                    notifyChanged()
                }
            };
            override fun onFailure(call:retrofit2.Call<Void>,t:Throwable){
                busy=false;
                error="Não foi possível confirmar a exclusão.";
                notifyChanged()
            }
        })
    }
}
