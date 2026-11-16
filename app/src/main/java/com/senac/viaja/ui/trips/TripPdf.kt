package com.senac.viaja.ui.trips
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.senac.viaja.data.model.TripBundle
import java.io.OutputStream
internal object TripPdf{
    fun write(bundle:TripBundle,output:OutputStream){
        val document=PdfDocument();
        val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply{
            color=Color.rgb(24,60,52);
            textSize=11f
        };
        var page:PdfDocument.Page?=null;
        var y=0f;
        var pageNo=0
        fun newPage(){
            page?.let{
                document.finishPage(it)
            };
            page=document.startPage(PdfDocument.PageInfo.Builder(595,842,++pageNo).create());
            y=42f;
            page!!.canvas.drawText("Viajá · ${bundle.trip.name.take(65)} · $pageNo",40f,y,paint);
            y+=30
        }
        fun line(text:String){
            if(page==null||y>795f)newPage();
            page!!.canvas.drawText(text,40f,y,paint);
            y+=16
        }
        fun paragraph(value:String){
            for(raw in value.replace("\r","").split('\n')){
                var rest=raw;
                while(rest.isNotEmpty()){
                    var count=rest.length.coerceAtMost(100);
                    while(count>1&&paint.measureText(rest.take(count))>510f)count--;
                    val part=rest.take(count);
                    line(part);
                    rest=rest.drop(count)
                };
                if(raw.isEmpty())line("")
            }
        }
        try{
            paragraph(TripFormRules.datesLabel(bundle.trip.startDate,bundle.trip.endDate));
            paragraph("Modo: ${if(bundle.trip.mode=="work")"Trabalho" else "Turismo"}");
            bundle.stops.forEach{
                paragraph("Dia ${it.day} · ${it.time?:"Sem horário"} · ${it.name}${if(it.completed)" · Concluída" else ""}");
                it.address?.let(::paragraph);
                if(bundle.full)it.note?.let(::paragraph)
            }
            if(bundle.full){
                paragraph("\nAnotações");
                bundle.notes.forEach{
                    paragraph(it.note.orEmpty())
                };
                bundle.memos.forEach{
                    paragraph("Parada ${it.localId}: ${it.note.orEmpty()}")
                };
                paragraph("\nChecklist");
                bundle.checklist.forEach{
                    paragraph("${if(it.completed)"Concluído" else "Pendente"} · ${it.label}")
                };
                bundle.expenses?.let{
                    paragraph("\nGastos: ${ExpenseRules.currency(it.totalCents)}");
                    paragraph(ExpenseRules.balanceLabel(it.remainingCents));
                    it.expenses.forEach{
                        e->paragraph("${TripFormRules.displayDate(e.expenseDate)} · ${e.description} · ${ExpenseRules.currency(e.amountCents)}")
                    }
                }
            }
            if(page==null)newPage();
            page?.let{
                document.finishPage(it);
                page=null
            };
            document.writeTo(output)
        }finally{
            page?.let{
                runCatching{
                    document.finishPage(it)
                }
            };
            document.close()
        }
    }
}
