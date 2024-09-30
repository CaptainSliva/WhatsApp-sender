package com.example.whatsupsend

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections


var data = mutableListOf<RecyclerData>()
var flag = false
lateinit var adapter: RVadapter

class MainActivity : AppCompatActivity()  {


    //contact permission code
    private val CONTACT_PERMISSION_CODE = 1;
    //contact pick code
    private val CONTACT_PICK_CODE = 2

    lateinit var phone:TextView
    lateinit var name:TextView
    lateinit var messagesDao: MessagesDao
    lateinit var animator: ItemAnimator
    var permomentOpen = false

    private lateinit var prefs: SharedPreferences

//    private val scope = CoroutineScope(newSingleThreadContext("thread"))



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (checkContactPermission()){
            //allowed
        }
        else{
            //not allowed, request
            requestContactPermission()
        }

        phone = findViewById(R.id.tphoneNumber)
        name = findViewById(R.id.tname)
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val addMessage = findViewById<ImageButton>(R.id.bAddMessage)
        val RV = findViewById<RecyclerView>(R.id.messagesList)
        val chooseContact = findViewById<ImageButton>(R.id.bChooseContact)
        val sendMessage = findViewById<ImageButton>(R.id.bSendMessage)
        val whatsapp = findViewById<ImageView>(R.id.whatsapp)
        val permomentSwitch = findViewById<SwitchCompat>(R.id.permoment_open_send)
        val layoutManager = GridLayoutManager(this, 1)
        adapter = RVadapter(this@MainActivity, data)
        animator = RV.itemAnimator!!
        RV.layoutManager = layoutManager
        RV.adapter = adapter

        if(prefs.contains("phoneNumber")){
            // Получаем число из настроек
            phone.text = prefs.getString("phoneNumber", "")
            name.text = prefs.getString("phoneName", "")
            permomentOpen = prefs.getBoolean("permomentOpenSend", false)
            permomentSwitch.isChecked = permomentOpen
            // Выводим на экран данные из настроек
        }

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "MessagesDB"
        ).build()

        data.clear()
        messagesDao = db.userDao()
        CoroutineScope(Dispatchers.IO).launch {
//            messagesDao.delete() //////////////////////////////////////////////////////////
            loadMessages()
            adapter.notifyDataSetChanged()
        }

        if (permomentOpen) {
            Thread.sleep(500)
            sendToWhatsApp(this)
        }




        addMessage.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {

                RV.itemAnimator = null
                if (flag) {
//                    messagesDao.insertMessage(Messages(messagesDao.getNumberOfRows(), "", flag))
                    data.add(RecyclerData("", R.color.black))
                }
                else {
//                    messagesDao.insertMessage(Messages(messagesDao.getNumberOfRows(), "", flag))
                    data.add(RecyclerData("", R.color.current))
                    flag = true
                }

            }
            Log.d("chec", data.toString())
            adapter.notifyDataSetChanged()
        }

        permomentSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                permomentOpen = true
            } else {
                permomentOpen = false
            }
        }

        chooseContact.setOnClickListener {
            pickContact()
            changePrefs()
        }

        sendMessage.setOnClickListener {
            sendToWhatsApp(this)
        }

        whatsapp.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            val toNumber = "${phone.text}"
            val url = "https://api.whatsapp.com/send?phone=$toNumber"
            i.setPackage("com.whatsapp") //com.whatsapp or com.whatsapp.w4b
            i.setData(Uri.parse(url))
            startActivity(i)
        }

//        RV.addOnItemTouchListener(
//            RecyclerItemClickListener(
//                this,
//                RV,
//                object : RecyclerItemClickListener.OnItemClickListener {
//                    override fun onItemClick(view: View, position: Int) {
//                        Log.d("click", view.toString())
//                        Log.d("click", position.toString())
//                        if (deleteTouch) {
//                            createDialog(this@MainActivity, adapter, RV, position).show()
//                            DrawableCompat.setTint(delete.drawable, ContextCompat.getColor(applicationContext, R.color.cream))
//                            deleteTouch = false
//                        }
//                    }
//
//                    override fun onLongItemClick(view: View, position: Int) {
//                        // do whatever
//                    }
//                })
//        )

        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        RV.addItemDecoration(dividerItemDecoration)
        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(RV)
    }

    override fun onStop() {
        super.onStop()
        Log.d("Stop", "stop")
        changePrefs()
        CoroutineScope(Dispatchers.IO).launch {changeMessages()}

    }

    fun changePrefs() {
        val editor = prefs.edit()
        editor.putString("phoneNumber", phone.text.toString())
        editor.putString("phoneName", name.text.toString())
        editor.putBoolean("permomentOpenSend", permomentOpen)
        editor.apply()
    }

    suspend fun changeMessages() {

        messagesDao.delete()
        flag = false
        Log.d("checK", data.toString())
        if (data.size > 1) {
            for (i in 0..data.size-1) {
                val item = data[i]
                var itemFlag = true
                if (item.getColor() == R.color.current) itemFlag = false
                if (itemFlag && flag) {
                    messagesDao.insertMessage(Messages(i, item.getText(), itemFlag))
                }
                else {
                    messagesDao.insertMessage(Messages(i, item.getText(), itemFlag))
//                if (messagesDao.getLastFlag()) {
//                    messagesDao.insertMessage(Messages(i, data[i].getText(), flag))
//                } else {
//                    messagesDao.insertMessage(Messages(i, data[i].getText(), false))
//                }
                    flag = true
                }
            }
        }
        else {
            val item = data[0]
            var itemFlag = true
            if (item.getColor() == R.color.current) itemFlag = false
            if (itemFlag && flag) {
                messagesDao.insertMessage(Messages(0, item.getText(), itemFlag))
            }
            else {
                messagesDao.insertMessage(Messages(0, item.getText(), itemFlag))
                flag = true
            }

        }
        Log.d("checI", messagesDao.getAll().toString())


        Log.d("data", messagesDao.getAll().toString())

    }

    suspend fun loadMessages() {
        Log.d("checL", messagesDao.getAll().toString())
        messagesDao.getAll().forEach { message ->
            if (message.flag) {
                data.add(RecyclerData(message.message, R.color.black))
                Log.d("add", "addI")
            }
            else {
                data.add(RecyclerData(message.message, R.color.current))
                flag = true
                Log.d("add", "addE")
            }
        }
    }


    fun sendToWhatsApp(context: Context) {
        for (i in 0..data.size-1) {
            val item = data[i]
            if (item.getColor() == R.color.current) {

                try {
                    val waIntent = Intent(Intent.ACTION_SEND)
                    waIntent.setType("text/plain")
                    val text = item.getText()
                    //Check if package exists or not. If not then code
                    //in catch block will be called
                    waIntent.putExtra(Intent.EXTRA_TEXT, text)
                    waIntent.setPackage("com.whatsapp")
                    startActivity(Intent.createChooser(waIntent, "Share with"))
                } catch (e: NameNotFoundException) {
                    Toast.makeText(context, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                        .show()
                }

                Thread.sleep(2000)
                changeColor(i)
                if(data.size > 1) {
                    item.setColor(R.color.black)
                }
                adapter.notifyDataSetChanged()
                break
            }
        }
    }


    var simpleCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            recyclerView.itemAnimator = animator

            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            Collections.swap(data, fromPosition, toPosition)

            recyclerView.adapter!!.notifyItemMoved(fromPosition, toPosition)
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }
    }

    private fun checkContactPermission(): Boolean{
        //check if permission was granted/allowed or not, returns true if granted/allowed, false if not
        return  ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactPermission(){
        //request the READ_CONTACTS permission
        val permission = arrayOf(android.Manifest.permission.READ_CONTACTS)
        ActivityCompat.requestPermissions(this, permission, CONTACT_PERMISSION_CODE)
    }

    private fun pickContact(){
        //intent ti pick contact
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, CONTACT_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //handle permission request results || calls when user from Permission request dialog presses Allow or Deny
        if (requestCode == CONTACT_PERMISSION_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //permission granted, can pick contact
            }
            else{
                //permission denied, cann't pick contact, just show message
                Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //handle intent results || calls when user from Intent (Contact Pick) picks or cancels pick contact
        if (resultCode == RESULT_OK){
            //calls when user click a contact from contacts (intent) list
            if (requestCode == CONTACT_PICK_CODE){

                val cursor1: Cursor
                val cursor2: Cursor?

                //get data from intent
                val uri = data!!.data
                cursor1 = contentResolver.query(uri!!, null, null, null, null)!!
                if (cursor1.moveToFirst()){
                    //get contact details
                    val contactId = cursor1.getString(with(cursor1) { getColumnIndex(ContactsContract.Contacts._ID) })
                    val contactName = cursor1.getString(with(cursor1) { getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME) })
                    val idResults = cursor1.getString(with(cursor1) { getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER) })
                    val idResultHold = idResults.toInt()
                    //set details: contact id, contact name, image
                    name.text = ("$contactName")

                    if (idResultHold == 1){
                        cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+contactId,
                            null,
                            null
                        )
                        //a contact may have multiple phone numbers
                        while (cursor2!!.moveToNext()){
                            //get phone number
                            val contactNumber = cursor2.getString(with(cursor2) { getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER) })
                            //set phone number
                            phone.text = ("${contactNumber}")

                        }
                        cursor2.close()
                    }
                    cursor1.close()
                }
            }

        }
        else{
            //cancelled picking contact
            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }
    }






    @Database(entities = [Messages::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun userDao(): MessagesDao
    }
}
fun changeColor(position: Int) {
    if (data[position].getColor() == R.color.current) {
        if (data.size-1 == position) {
            data[0].setColor(R.color.current)
        }
        else {
            data[position+1].setColor(R.color.current)
        }
        if (data.size == 1) {
            flag = true
            data[position].setColor(R.color.current)
        }
    }
}

fun chooseItem(position: Int) {
    if (data[position].getColor() != R.color.current) {
        for (i in 0..data.size-1) {
            if (i == position) {
                data[i].setColor(R.color.current)
            }
            else {
                data[i].setColor(R.color.black)
            }
        }
    }
}