package one.codehz.container.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import one.codehz.container.R
import one.codehz.container.adapters.LogListAdapter
import one.codehz.container.adapters.PropertyListAdapter
import one.codehz.container.ext.MakeLoaderCallbacks
import one.codehz.container.ext.get
import one.codehz.container.ext.vClientImpl
import one.codehz.container.models.AppModel
import one.codehz.container.models.AppPropertyModel
import one.codehz.container.models.LogModel
import one.codehz.container.provider.MainProvider

class BasicDetailFragment(model: AppModel) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = inflater.inflate(R.layout.application_basic_detail, container, false)!!

    val contentList by lazy<RecyclerView> { view!![R.id.content_list] }
    val logList by lazy<RecyclerView> { view!![R.id.log_list] }

    val propertyLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        contentAdapter.updateModels(AppPropertyModel(model).getItems())
    }
    val logLoader by MakeLoaderCallbacks({ context }, { it() }) { ctx ->
        context.contentResolver.query(MainProvider.LOG_URI, arrayOf("time", "data"), "`package` = \"${model.packageName}\"", null, "time ASC").use {
            generateSequence { if (it.moveToNext()) it else null }.map { LogModel(it.getString(0), it.getString(1).lineSequence().first()) }.toList()
        }.run {
            logListAdapter.updateModels(this)
        }
    }
    val contentAdapter by lazy {
        PropertyListAdapter<AppPropertyModel>()
    }
    val logListAdapter by lazy {
        LogListAdapter()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(contentList) {
            adapter = contentAdapter
            layoutManager = LinearLayoutManager(context)
        }
        with(logList) {
            adapter = logListAdapter
            layoutManager = LinearLayoutManager(context)
        }
        loaderManager.restartLoader(0, null, propertyLoader)
        loaderManager.restartLoader(1, null, logLoader)
    }

    override fun onResume() {
        super.onResume()
        loaderManager.restartLoader(0, null, propertyLoader)
        loaderManager.restartLoader(1, null, logLoader)
    }
}