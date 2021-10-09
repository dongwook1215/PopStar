package com.example.popstar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.popstar.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*

class  DetailviewFragment : Fragment(){

    var firestore: FirebaseFirestore ?= null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firestore= FirebaseFirestore.getInstance()
        var view = LayoutInflater.from(inflater.context).inflate(R.layout.fragment_detail,container,false)
        view.detailviewfragment_recyclerview.adapter = DetailRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class DetailRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
    {
        var contentDTOs: ArrayList<ContentDTO>
        var contentUidList: ArrayList<String>
        init {
            contentDTOs= ArrayList()
            contentUidList= ArrayList()
            //현재 로그인한 유저의 uid
            var uid = FirebaseAuth.getInstance().currentUser?.uid

            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                for(snapshot in querySnapshot!!.documents){
                        var item = snapshot.toObject(ContentDTO::class.java)
                        if(!(item!!.userId.toString().equals("kdo6338@gmail.com"))){
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                        }
                }

                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view!!) {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewHolder = (holder as CustomViewHolder).itemView
            //유저아이디
            viewHolder.detailviewitem_profile_textview.text = contentDTOs[position].userId
            //이미지
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl).into(viewHolder.detailviewitem_imageview_content)
            //설명 텍스트
            viewHolder.detailviewitem_explain_textview.text = contentDTOs[position].explain
            //좋아요 카운터 설정
            viewHolder.detailviewitem_favoritecounter_textview.text = "좋아요 "+contentDTOs[position].favoriteCount+"개"
            var uid = FirebaseAuth.getInstance().currentUser!!.uid
            viewHolder.detailviewitem_favorite_imageview.setOnClickListener{
                favoriteEvent(position)
            }
            //좋아요를 클릭
            if(contentDTOs!![position].favorites.containsKey(uid)){
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)
            }else{
                viewHolder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }
        }
        private fun favoriteEvent(position: Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction {
                transaction ->
                var uid = FirebaseAuth.getInstance().currentUser!!.uid
                var contentDTO =transaction.get(tsDoc!!).toObject(ContentDTO::class.java)
                //좋아요를 누른 상태(누르지 않은 상태)
                if(contentDTO!!.favorites.containsKey(uid)){
                    contentDTO?.favoriteCount =contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                }else{
                    contentDTO?.favorites[uid] = true
                    contentDTO?.favoriteCount =contentDTO?.favoriteCount + 1
                }
                transaction.set(tsDoc,contentDTO)
            }
        }
    }

}