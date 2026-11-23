package com.senac.viaja.ui.places
import android.graphics.BitmapFactory
import android.widget.ImageView
import java.util.concurrent.Executors
internal object CatalogImages{
    private val executor=Executors.newFixedThreadPool(2)
    fun show(view:ImageView,url:String?){
        val uri=runCatching{android.net.Uri.parse(url)}.getOrNull()
        val secure=uri?.scheme=="https"
        val local=uri?.scheme=="http"&&(uri.host=="127.0.0.1"||uri.host=="10.0.2.2")
        val allowed=secure||local
        view.tag=url;
        view.setImageDrawable(null);
        view.visibility=if(allowed)android.view.View.VISIBLE else android.view.View.GONE;
        if(!allowed)return
        executor.execute{
            runCatching{
                val connection=java.net.URL(url).openConnection().apply{
                    connectTimeout=8000;
                    readTimeout=8000
                };
                connection.getInputStream().use{
                    stream->val out=java.io.ByteArrayOutputStream();
                    val bytes=ByteArray(8192);
                    while(true){
                        val n=stream.read(bytes);
                        if(n<0)break;
                        require(out.size()+n<=2*1024*1024);
                        out.write(bytes,0,n)
                    };
                    val data=out.toByteArray();
                    val options=BitmapFactory.Options().apply{
                        inJustDecodeBounds=true
                    };
                    BitmapFactory.decodeByteArray(data,0,data.size,options);
                    require(options.outWidth in 1..8192&&options.outHeight in 1..8192);
                    options.inSampleSize=maxOf(1,maxOf(options.outWidth,options.outHeight)/1024);
                    options.inJustDecodeBounds=false;
                    BitmapFactory.decodeByteArray(data,0,data.size,options)
                }
            }.onSuccess{
                bitmap->view.post{
                    if(view.tag==url)view.setImageBitmap(bitmap)
                }
            }
        }
    }
}
