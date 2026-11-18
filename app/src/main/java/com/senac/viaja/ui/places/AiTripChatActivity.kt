package com.senac.viaja.ui.places
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import androidx.core.view.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.senac.viaja.R
import com.senac.viaja.adapter.ChatMessageAdapter
import com.senac.viaja.data.repository.AuthRepository
import com.senac.viaja.data.remote.*
import com.senac.viaja.ui.main.*
import com.senac.viaja.ui.trips.ItineraryRules
class AiTripChatActivity:AppCompatActivity(){
    private lateinit var state:AiChatState;
    private lateinit var input:EditText;
    private var binding=false;
    private lateinit var panel:LinearLayout
    override fun onCreate(saved:Bundle?){
        setTheme(R.style.Theme_Viaja_Vi);
        super.onCreate(saved);
        if(AuthRepository(this).getUser()==null){
            startActivity(android.content.Intent(this,com.senac.viaja.ui.auth.LoginActivity::class.java));
            finish();
            return
        };
        enableEdgeToEdge();
        setContentView(R.layout.activity_ai_trip_chat);
        val root=findViewById<LinearLayout>(R.id.viRoot);
        ViewCompat.setOnApplyWindowInsetsListener(root){
            v,insets->val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout());
            val keyboard=insets.getInsets(WindowInsetsCompat.Type.ime());
            v.updatePadding(left=bars.left,top=bars.top,right=bars.right,bottom=maxOf(bars.bottom,keyboard.bottom));
            insets
        }
        panel=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL;
            setPadding(16,0,16,0)
        };
        root.addView(panel,root.indexOfChild(findViewById(R.id.viSuggestions)))
        state=ViewModelProvider(this)[AiChatState::class.java];
        state.initialize(intent.getLongExtra("trip_id",-1).takeIf{
            it>0
        },intent.getStringExtra("trip_name").orEmpty(),intent.getStringExtra("trip_mode")?:"tourism")
        input=findViewById(R.id.etAiInput);
        input.doAfterTextChanged{
            if(!binding){
                state.session.draft=it.toString();
                state.persist();
                findViewById<View>(R.id.btnAiSend).isEnabled=it?.isNotBlank()==true
            }
        }
        findViewById<View>(R.id.viBack).setOnClickListener{
            finish()
        };
        findViewById<View>(R.id.viMenu).setOnClickListener{
            menu(it)
        };
        findViewById<View>(R.id.tvAiEditContext).setOnClickListener{
            MaterialAlertDialogBuilder(this).setItems(arrayOf("Turismo","Trabalho","Ritmo tranquilo","Ritmo equilibrado","Ritmo intenso")){
                _,i->when(i){
                    0->state.session.mode="tourism";
                    1->state.session.mode="work";
                    else->state.session.pace=listOf("Tranquilo","Equilibrado","Intenso")[i-2]
                };
                state.persist();
                render()
            }.show()
        };
        findViewById<View>(R.id.btnAiSend).setOnClickListener{
            if(state.busy)state.cancel()else state.send()
        };
        findViewById<View>(R.id.viSuggestPlan).setOnClickListener{
            input.setText("Quero organizar os dias da minha viagem.");
            plan()
        };
        findViewById<View>(R.id.viSuggestBudget).setOnClickListener{
            input.setText("Como posso aproveitar minha viagem respeitando meu orçamento?")
        };
        state.changed.observe(this){
            render()
        }
    }
    override fun onResume(){
        super.onResume();
        if(::state.isInitialized&&AuthRepository(this).getUser()?.id!=state.owner)finish()
    }
    private fun render(){
        if(isFinishing||isDestroyed)return;
        if(AuthRepository(this).getUser()?.id!=state.owner){
            finish();
            return
        };
        binding=true;
        if(input.text.toString()!=state.session.draft){
            input.setText(state.session.draft);
            input.setSelection(input.length())
        };
        binding=false;
        input.isEnabled=!state.busy;
        findViewById<TextView>(R.id.tvAiContext).text=state.session.tripName.ifBlank{
            "Conversa livre"
        };
        findViewById<View>(R.id.tvAiEditContext).isEnabled=!state.busy;
        findViewById<TextView>(R.id.viContextDetail).text="${if(state.session.mode=="work")"Trabalho" else "Turismo"} · ${state.session.pace}";
        val list=findViewById<RecyclerView>(R.id.recycler_ai_chat);
        if(list.layoutManager==null)list.layoutManager=LinearLayoutManager(this).apply{
            stackFromEnd=true
        };
        list.adapter=ChatMessageAdapter(state.session.messages);
        if(state.session.messages.isNotEmpty())list.scrollToPosition(state.session.messages.lastIndex)
        val quota=state.quota;
        findViewById<TextView>(R.id.viServiceStatus).text=state.error?:if(state.busy)"A Vi está preparando a resposta…" else if(quota?.available==true)"${quota.remaining} gerações · renovação ${quota.renewsAt.take(10)} · ${quota.dailyRemaining} mensagens restantes hoje" else "A assistente está temporariamente indisponível. Seu rascunho foi preservado."
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAiSend).apply{
            setIconResource(if(state.busy)R.drawable.ic_stop else R.drawable.ic_arrow_right);
            isEnabled=state.busy||input.text.isNotBlank();
            contentDescription=if(state.busy)"Interromper espera" else "Enviar mensagem"
        };
        findViewById<TextView>(R.id.viPrivacy).text="Contexto da viagem e interesses são enviados ao serviço de IA ao enviar uma mensagem."
        panel.removeAllViews();
        state.proposal?.let{
            turn->panel.words(if(turn.applied)"Proposta aplicada ao roteiro." else "Proposta: ${turn.stops.size} paradas para revisar");
            if(!turn.applied)panel.press("Revisar proposta"){
                val details=turn.stops.joinToString("\n"){
                    "Dia ${it.day} · ${it.scheduledTime?:"Sem horário"} · ${state.names[it.localId]?:"Lugar ${it.localId}"}"
                };
                MaterialAlertDialogBuilder(this).setTitle("Proposta de roteiro").setMessage(details+"\n\nAplicar substitui o roteiro salvo. Paradas removidas e seus memos serão excluídos.").setNegativeButton("Voltar",null).setPositiveButton("Aprovar e aplicar"){
                    _,_->state.apply()
                }.show()
            }.isEnabled=!state.busy
        }
    }
    private fun plan(){
        val id=state.session.tripId?:run{
            MaterialAlertDialogBuilder(this).setMessage("Abra a Vi a partir de uma viagem para gerar um roteiro.").setPositiveButton("Entendi",null).show();
            return
        };
        ApiClient.service.getTripById(id).deliver({
            trip->val duration=ItineraryRules.duration(trip.startDate,trip.endDate);
            if(duration!=null){
                state.send("plan",duration)
            }else{
                val field=EditText(this).apply{
                    inputType=android.text.InputType.TYPE_CLASS_NUMBER;
                    hint="Quantidade de dias (1 a 30)"
                };
                MaterialAlertDialogBuilder(this).setTitle("Quantos dias planejar?").setView(field).setPositiveButton("Gerar proposta"){
                    _,_->val days=field.text.toString().toIntOrNull();
                    if(days!=null&&days in 1..30)state.send("plan",days)else{
                        state.error="Escolha entre 1 e 30 dias.";
                        state.notifyChanged()
                    }
                }.setNegativeButton("Cancelar",null).show()
            }
        },{
            state.error=it;
            state.notifyChanged()
        })
    }
    private fun menu(anchor:View){
        if(state.busy)return;
        PopupMenu(this,anchor).apply{
            menu.add("Nova conversa");
            menu.add("Histórico e sincronização");
            menu.add("Gerar proposta de roteiro");
            menu.add("Retomar último pedido");
            menu.add("Atualizar disponibilidade");
            menu.add("Excluir conversa");
            setOnMenuItemClickListener{
                when(it.title.toString()){
                    "Nova conversa"->{
                        val old=state.session;
                        state.create(old.tripId,old.tripName,old.mode)
                    };
                    "Histórico e sincronização"->{
                        state.remoteHistory{
                            if(!isFinishing&&!isDestroyed)MaterialAlertDialogBuilder(this@AiTripChatActivity).setTitle("Conversas desta conta").setItems(state.sessions.map{
                                it.title
                            }.toTypedArray()){
                                _,i->state.activate(state.sessions[i].id)
                            }.setNegativeButton("Fechar",null).show()
                        }
                    };
                    "Gerar proposta de roteiro"->plan();
                    "Retomar último pedido"->{
                        state.session.pending?.let{
                            input.setText(it.text)
                        }?:state.session.messages.lastOrNull{
                            it.isUser
                        }?.let{
                            input.setText(it.text)
                        }
                    };
                    "Atualizar disponibilidade"->state.refreshQuota();
                    "Excluir conversa"->MaterialAlertDialogBuilder(this@AiTripChatActivity).setTitle("Excluir esta conversa?").setNegativeButton("Manter",null).setPositiveButton("Excluir"){
                        _,_->state.delete()
                    }.show()
                };
                true
            }
        }.show()
    }
}
