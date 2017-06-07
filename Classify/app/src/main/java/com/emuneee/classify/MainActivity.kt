package com.emuneee.classify

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class MainActivity : Activity() {

    lateinit var title: TextView
    lateinit var progress: TextView
    lateinit var status: TextView
    lateinit var imageView: ImageView
    lateinit var next: Button
    lateinit var prev: Button
    lateinit var notCongested: Button
    lateinit var isCongested: Button
    lateinit var ditch: Button
    lateinit var busy: ProgressBar

    val URL = "TODO"
    var images: ArrayList<Image>? = null
    var index = 0
    lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = FirebaseDatabase.getInstance().reference

        title = findViewById(R.id.title) as TextView
        progress = findViewById(R.id.progress) as TextView
        status = findViewById(R.id.status) as TextView
        imageView = findViewById(R.id.image) as ImageView
        next = findViewById(R.id.next) as Button
        prev = findViewById(R.id.prev) as Button
        isCongested = findViewById(R.id.congested) as Button
        notCongested = findViewById(R.id.no) as Button
        ditch = findViewById(R.id.ditch) as Button
        busy = findViewById(R.id.busy) as ProgressBar
    }

    override fun onStart() {
        super.onStart()
        busy.visibility = View.VISIBLE

        database.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                images = ArrayList<Image>()

                if (dataSnapshot.hasChild("index")) {
                    index = (dataSnapshot.child("index").value as Long).toInt()
                }

                val it = dataSnapshot.child("data").children
                it.forEach { child ->
                    images!!.add(child.getValue(Image::class.java))
                }
                initClickListeners()
                loadImage()
                busy.visibility = View.INVISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun updateImage(id: Int, image: Image) {
        updateStatus(image)
        busy.visibility = View.VISIBLE

        Observable.create(Observable.OnSubscribe<Boolean> { subscriber ->
            database.child("data")
                    .child(id.toString())
                    .child("isCongested")
                    .setValue(image.isCongested)
            database.child("index").setValue(index)
            subscriber.onNext(true)
            subscriber.onCompleted()
        })
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            busy.visibility = View.INVISIBLE

            if (index < images!!.size + 1) {
                index++
                loadImage()
            }
        })
    }

    fun initClickListeners() {
        prev.setOnClickListener {

            if (index > 0) {
                index--
                loadImage()
            }
        }

        next.setOnClickListener {

            if (index < images!!.size + 1) {
                index++
                loadImage()
            }
        }

        isCongested.setOnClickListener {
            val image = getImage()
            image.isCongested = 1
            updateImage(index, image)
        }

        notCongested.setOnClickListener {
            val image = getImage()
            image.isCongested = 0
            updateImage(index, image)
        }

        ditch.setOnClickListener {
            val image = getImage()
            image.isCongested = -1
            updateImage(index, image)
        }
    }

    fun getImage() : Image {
        return images!![index]
    }

    fun updateStatus(image: Image) {
        when (image.isCongested) {
            -1 -> status.text = "Ditch"
            0 -> status.text = "Not Congested"
            1 -> status.text = "Congested"
        }
    }

    @SuppressLint("SetTextI18n")
    fun loadImage() {
        val image = images!![index]
        updateStatus(image)
        title.text = image.filename
        progress.text = "${index + 1} of ${images!!.size}"
        val url = URL + image.filename
        Glide.with(this)
                .load(url)
                .into(imageView)
    }
}
