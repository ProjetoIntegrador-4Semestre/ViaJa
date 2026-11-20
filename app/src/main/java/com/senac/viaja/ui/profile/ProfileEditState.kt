package com.senac.viaja.ui.profile
import androidx.lifecycle.*
import com.senac.viaja.data.model.*
import com.senac.viaja.data.remote.*
class ProfileEditState: ViewModel(){
    val changed=MutableLiveData(0);
    var owner=0L;
    var name="";
    var email="";
    var mode="tourism";
    var password="";
    var selected=mutableSetOf<Long>();
    var options=emptyList<InterestOption>();
    var restoredForm=false;
    var ready=false;
    var busy=false;
    var error:String?=null;
    var result:ProfileSession?=null
    fun notifyChanged(){
        changed.value=(changed.value?:0)+1
    }
    fun load(requestedMode: String? = null){
        if(busy)return;
        busy=true;
        error=null;
        notifyChanged();
        ApiClient.service.account().deliver({
            if(!ready&&!restoredForm){
                name=it.name;
                email=it.email;
                mode=requestedMode ?: it.defaultMode;
                selected=it.interestIds.toMutableSet()
            };
            options=it.interests;
            owner=it.id;
            ready=true;
            busy=false;
            notifyChanged()
        },{
            busy=false;
            error=it;
            notifyChanged()
        })
    }
    fun save(){
        if(busy||!ready)return;
        if(name.trim().isEmpty()||!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()){
            error="Confira nome e e-mail.";
            notifyChanged();
            return
        };
        busy=true;
        error=null;
        notifyChanged();
        ApiClient.service.saveAccount(ProfileWrite(name.trim(),email.trim(),mode,selected,password.ifBlank{
            null
        })).deliver({
            password="";
            result=it;
            busy=false;
            notifyChanged()
        },{
            busy=false;
            error=it;
            notifyChanged()
        })
    }
}
