package com.tugceaksoy.yemektariflerimm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_tarif.*
import java.io.ByteArrayOutputStream


class TarifFragment : Fragment() {

var secilengorsel : Uri?=null
    var secilenbitmap :Bitmap?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener {
            kaydet(it)
        }
        imageView.setOnClickListener {
            gorselSeç(it)
        }
        arguments?.let {
            var gelenbilgi=TarifFragmentArgs.fromBundle(it).bilgi
            if(gelenbilgi.equals("menudengeldim")){
                yemekismiText.setText("")
                yemektarifiText.setText("")
                button.visibility=View.VISIBLE
                val gorselsecmearkaplanı=BitmapFactory.decodeResource(context?.resources,R.drawable.gorselsecimi)
                imageView.setImageBitmap(gorselsecmearkaplanı)


            }else{
                button.visibility=View.INVISIBLE
                val secilenıd=TarifFragmentArgs.fromBundle(it).id
                context?.let {
                    try {
                        val db=it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                        val cursor=db.rawQuery("SELECT * FROM yemekler WHERE id=?", arrayOf(secilenıd.toString()))
                        val yemekismiindex=cursor.getColumnIndex("yemekismi")
                        val yemekmalzemeindex=cursor.getColumnIndex("yemekmalzeme")
                        val yemekgorselindex=cursor.getColumnIndex("gorsel")
                        while (cursor.moveToNext()){
                            yemekismiText.setText(cursor.getString(yemekismiindex))
                            yemektarifiText.setText(cursor.getString(yemekmalzemeindex))
                            val bytedizisi=cursor.getBlob(yemekgorselindex)
                            val bitmap=BitmapFactory.decodeByteArray(bytedizisi,0,bytedizisi.size)
                            imageView.setImageBitmap(bitmap)



                        }
                        cursor.close()


                    }catch (e:Exception){

                        e.printStackTrace()
                    }



                }


            }
        }
    }
fun kaydet(view:View){
    //sql
    val yemekIsmi = yemekismiText.text.toString()
    val yemekMalzemeleri = yemektarifiText.text.toString()
  if(secilenbitmap!=null){
      val kucukbitmap=kucukbitmapolustur(secilenbitmap!!,300)
      val outputStream = ByteArrayOutputStream()
      kucukbitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
      val byteDizisi = outputStream.toByteArray()

      try{
          context?.let {
              val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
              database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB)")

              val sqlString = "INSERT INTO yemekler (yemekismi, yemekmalzemesi, gorsel) VALUES (?, ?, ?)"
              val statement = database.compileStatement(sqlString)
              statement.bindString(1,yemekIsmi)
              statement.bindString(2,yemekMalzemeleri)
              statement.bindBlob(3,byteDizisi)
              statement.execute()

          }

      } catch (e: Exception){
          e.printStackTrace()
      }

      val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
      Navigation.findNavController(view).navigate(action)















  }



    }
    fun gorselSeç(view:View){
        activity?.let {

            if (ContextCompat.checkSelfPermission(it.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }
            else{
                val galeriIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)


            }
        }


     }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode==1){
            if(grantResults.size> 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       if(requestCode==2&&resultCode==Activity.RESULT_OK&&data!=null){
           secilengorsel=data.data
           try {
             context?.let {
                 if (secilengorsel!=null){
                     if(Build.VERSION.SDK_INT>=28){
                        val source= ImageDecoder.createSource(it.contentResolver,secilengorsel!!)
                  secilenbitmap=ImageDecoder.decodeBitmap(source)
                  imageView.setImageBitmap(secilenbitmap)
                     }else{
                 secilenbitmap=MediaStore.Images.Media.getBitmap(it.contentResolver,secilengorsel)
                imageView.setImageBitmap(secilenbitmap)
               }}}

           }catch (e : Exception){
               e.printStackTrace()

           }
       }

        super.onActivityResult(requestCode, resultCode, data)
    }
    fun kucukbitmapolustur(kullanıcınınSectigiBitmap: Bitmap,maximumboyut:Int):Bitmap{
        var width=kullanıcınınSectigiBitmap.width
        var height=kullanıcınınSectigiBitmap.height
        val bitmapOrani : Double = width.toDouble() / height.toDouble()
        if (bitmapOrani > 1) {
            // görselimiz yatay
            width = maximumboyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()} else {
            //görselimiz dikey
            height = maximumboyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()

        }

        return Bitmap.createScaledBitmap(kullanıcınınSectigiBitmap,width,height,true)
        


    }

    }
