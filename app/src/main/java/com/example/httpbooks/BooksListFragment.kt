package com.example.httpbooks

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_books_list.*

class BooksListFragment : Fragment() {
    private var asyncTask: BooksDownloadTask? = null
    private val booksList = mutableListOf<Book>()
    private var adapter: ArrayAdapter<Book>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_books_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, booksList)
        adapter = BookListAdapter(requireContext(), booksList)
        listView.emptyView = txtMessage
        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, pos, id ->
            run {
                this.clickOnItem(parent, pos);
            }
        }

        listView.setOnItemLongClickListener { parent, view, pos, id ->
            run {
                this.longClickOnItem(parent, pos)
            }
        }

        if (booksList.isNotEmpty()) {
            showProgress(false)
        } else {
            if (asyncTask == null) {
                if (BookHttp.hasConnection(requireContext())) {
                    startDownloadJson()
                } else {
                    progressBar.visibility = View.GONE
                    txtMessage.setText(R.string.error_no_connection)
                }
            } else if (asyncTask?.status == AsyncTask.Status.RUNNING) {
                showProgress(true)
            }
        }
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            txtMessage.setText(R.string.message_progress)
        }
        txtMessage.visibility = if (show) View.VISIBLE else View.GONE
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun startDownloadJson() {
        if (asyncTask?.status != AsyncTask.Status.RUNNING) {
            asyncTask = BooksDownloadTask()
            asyncTask?.execute()
        }
    }

    private fun updateBookList(result: List<Book>?) {
        if (result != null) {
            booksList.clear()
            booksList.addAll(result)
        } else {
            txtMessage.setText(R.string.error_load_books)
        }
        adapter?.notifyDataSetChanged()
        asyncTask = null
    }

    inner class BooksDownloadTask : AsyncTask<Void, Void, List<Book>?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            showProgress(true)
        }

        override fun doInBackground(vararg strings: Void): List<Book>? {
            return BookHttp.loadBooksGson()
        }

        override fun onPostExecute(livros: List<Book>?) {
            super.onPostExecute(livros)
            showProgress(false)
            updateBookList(livros)
        }
    }

    fun clickOnItem(parent: AdapterView<*>, pos: Int) {
        val element = parent.getItemAtPosition(pos) as Book
        Toast.makeText(requireContext(), "${element.title} - ${element.author} - ${element.year}", Toast.LENGTH_LONG).show()
    }

    fun longClickOnItem(parent: AdapterView<*>, pos: Int): Boolean {
        val element = parent.getItemAtPosition(pos) as Book

        if(adapter != null) {
            booksList.remove(element)

            adapter?.notifyDataSetChanged()

            Toast.makeText(requireContext(), "Livro de título ${element.title} removido", Toast.LENGTH_LONG).show()

            return true
        }

        Toast.makeText(requireContext(), "Não foi possível remover o livro de título ${element.title}", Toast.LENGTH_LONG).show()

        return false
    }
}