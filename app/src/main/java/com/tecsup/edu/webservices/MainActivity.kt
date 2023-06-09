package com.tecsup.edu.webservices
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = UserAdapter()
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        viewModel.users.observe(this, Observer { users ->
            adapter.setUsers(users)
        })

        fetchData()
    }

    private fun fetchData() {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://reqres.in/api/users?page=1")
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseData = response.body()?.string()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                        val jsonObject = JSONObject(responseData)
                        val jsonArray = jsonObject.getJSONArray("data")

                        val users = ArrayList<User>()

                        for (i in 0 until jsonArray.length()) {
                            val userObject = jsonArray.getJSONObject(i)
                            val firstName = userObject.getString("first_name")
                            val email = userObject.getString("email")
                            val avatar = userObject.getString("avatar")

                            val user = User(firstName, email, avatar)
                            users.add(user)
                        }

                        viewModel.setUsers(users)
                    } else {
                        // Manejar el error de la solicitud
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // Manejar el error de la solicitud
            }
        }
    }
}


