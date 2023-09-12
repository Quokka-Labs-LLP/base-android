package com.example.baseandroid.features.logviewer.ui

import android.content.ClipData
import android.content.ClipDescription.compareMimeTypes
import android.content.ClipboardManager
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Typeface.BOLD
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.CircularArray
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseandroid.R
import com.example.baseandroid.databinding.ActivityLogViewerBinding
import com.example.baseandroid.databinding.FilterSheetBinding
import com.example.baseandroid.utils.logviewer.DownloadsFileSaver
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher
import java.util.regex.Pattern


class LogViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogViewerBinding
    private lateinit var logAdapter: LogEntryAdapter
    private var verboseLogLines = CircularArray<LogLine>()
    private var debugLogLines = CircularArray<LogLine>()
    private var infoLogLines = CircularArray<LogLine>()
    private var warningLogLines = CircularArray<LogLine>()
    private var errorLogLines = CircularArray<LogLine>()
    private var adapterLocalList = CircularArray<LogLine>()
    private var rawLogLines = CircularArray<String>()
    private var recyclerView: RecyclerView? = null
    private lateinit var year: String
    private var lastUri: Uri? = null
    private var isFabsVisible = false
    private lateinit var filterButton: MenuItem
    private var currentTag = "V"
    private var currentSource = "All"
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private fun init() {
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)

        val yearFormatter: DateFormat = SimpleDateFormat("yyyy", Locale.US)
        year = yearFormatter.format(Date())
        appName = getString(R.string.app_name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
        TAG = appName.plus("\\LogViewerActivity")
        binding = ActivityLogViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        logAdapter = LogEntryAdapter()
        binding.recyclerView.apply {
            recyclerView = this
            layoutManager = LinearLayoutManager(context)
            adapter = logAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        lifecycleScope.launch(Dispatchers.IO) { streamingLog() }
        Log.i(TAG, "in onCreate")

        binding.extendFab.shrink()
        binding.extendFab.setOnClickListener {
            if (!isFabsVisible) {
                makeFabsVisible(true)
                isFabsVisible = true
                binding.extendFab.extend()
                binding.extendFab.icon =
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_less_actions, null)
            } else {
                makeFabsVisible(false)
                isFabsVisible = false
                binding.extendFab.shrink()
                binding.extendFab.icon =
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_more_actions, null)
            }
        }

        binding.saveFab.setOnClickListener {
            binding.extendFab.callOnClick()
            lifecycleScope.launch { saveLog() }
            Toast.makeText(this, "Logs Saved", Toast.LENGTH_LONG).show()
        }
        binding.deleteFab.setOnClickListener {
            binding.extendFab.callOnClick()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Logs?")
            builder.setMessage("Do you want to clear the logs?")
            builder.setPositiveButton("Yes", null)
            builder.setNegativeButton("No", null)
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()

            val mPositiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            mPositiveButton.setOnClickListener {
                alertDialog.cancel()
                clearLogs()
                Toast.makeText(this, "Logs Deleted", Toast.LENGTH_SHORT).show()
            }
            val mNegativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            mNegativeButton.setOnClickListener {
                alertDialog.cancel()
            }
        }

        binding.shareFab.setOnClickListener {
            binding.extendFab.callOnClick()
            lifecycleScope.launch {
                val f = SimpleDateFormat(
                    "yyyyMMdd", Locale.getDefault()
                )
                val d = Date(System.currentTimeMillis())
                val dateString = f.format(d)
                lastUri = saveLog()

                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_STREAM, lastUri)
                    .putExtra(Intent.EXTRA_TITLE, dateString)
                startActivity(Intent.createChooser(shareIntent, "Share Log File"))

            }
        }
    }

    override fun onPause() {
        bottomSheetDialog.cancel()
        super.onPause()
    }

    private fun filterSheet() {
        val bottomSheetBinding = FilterSheetBinding.inflate(layoutInflater, null, false)
        bottomSheetDialog.setCancelable(true)
        when (currentTag) {
            "V" -> bottomSheetBinding.radioVerbose.isChecked = true
            "D" -> bottomSheetBinding.radioDebug.isChecked = true
            "I" -> bottomSheetBinding.radioInfo.isChecked = true
            "W" -> bottomSheetBinding.radioWarning.isChecked = true
            "E" -> bottomSheetBinding.radioError.isChecked = true
        }
        when (currentSource) {
            "All" -> bottomSheetBinding.radioAll.isChecked = true
            "User" -> bottomSheetBinding.radioUser.isChecked = true
            "System" -> bottomSheetBinding.radioSystem.isChecked = true
        }
        bottomSheetBinding.btnApply.setOnClickListener {
            when (bottomSheetBinding.groupTags.checkedRadioButtonId) {
                R.id.radio_verbose -> {
                    currentTag = "V"
                }

                R.id.radio_debug -> {
                    currentTag = "D"
                }

                R.id.radio_info -> {
                    currentTag = "I"
                }

                R.id.radio_warning -> {
                    currentTag = "W"
                }

                R.id.radio_error -> {
                    currentTag = "E"
                }
            }
            when (bottomSheetBinding.groupSources.checkedRadioButtonId) {
                R.id.radio_all -> {
                    currentSource = "All"
                }

                R.id.radio_user -> {
                    currentSource = "User"
                }

                R.id.radio_system -> {
                    currentSource = "System"
                }
            }
            logAdapter.updateList(currentTag, currentSource)
            bottomSheetDialog.cancel()
        }
        bottomSheetBinding.tvClear.setOnClickListener {
            bottomSheetBinding.groupTags.check(R.id.radio_verbose)
            bottomSheetBinding.groupSources.check(R.id.radio_all)
        }
        bottomSheetDialog.setContentView(bottomSheetBinding.root)
        bottomSheetDialog.show()
    }

    private fun clearLogs() {
        logAdapter = LogEntryAdapter()
        verboseLogLines.clear()
        debugLogLines.clear()
        infoLogLines.clear()
        warningLogLines.clear()
        errorLogLines.clear()
        adapterLocalList.clear()
        rawLogLines.clear()
        recyclerView?.adapter = logAdapter
    }

    private fun makeFabsVisible(makeVisible: Boolean) {
        if (makeVisible) {
            binding.deleteFab.visibility = View.VISIBLE
            binding.shareFab.visibility = View.VISIBLE
            binding.saveFab.visibility = View.VISIBLE
        } else {
            binding.deleteFab.visibility = View.GONE
            binding.shareFab.visibility = View.GONE
            binding.saveFab.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.log_viewer, menu)
        filterButton = menu.findItem(R.id.filter_log)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.filter_log -> {
                filterSheet()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private val downloadsFileSaver = DownloadsFileSaver(this)

    private suspend fun rawLogBytes(): ByteArray {
        val builder = StringBuilder()
        withContext(Dispatchers.IO) {
            for (i in 0 until rawLogLines.size()) {
                builder.append(rawLogLines[i])
                builder.append('\n')
            }
        }
        return builder.toString().toByteArray(Charsets.UTF_8)
    }

    private suspend fun saveLog(): Uri? {
        var outputFile: DownloadsFileSaver.DownloadsFile? = null
        withContext(Dispatchers.IO) {
            try {
                outputFile = downloadsFileSaver.save("${appName}-log.txt", "text/plain", true)
                outputFile?.outputStream?.write(rawLogBytes())
            } catch (e: Throwable) {
                outputFile?.delete()
            }
        }
        return outputFile?.uri
    }

    private suspend fun streamingLog() = withContext(Dispatchers.IO) {
        val builder = ProcessBuilder().command("logcat", "-b", "all", "-v", "threadtime", "*:V")
        builder.environment()["LC_ALL"] = "C"
        var process: Process? = null
        try {
            process = try {
                builder.start()
            } catch (e: IOException) {
                Log.e(TAG, Log.getStackTraceString(e))
                return@withContext
            }
            val stdout =
                BufferedReader(InputStreamReader(process?.inputStream, StandardCharsets.UTF_8))

            var posStart = 0
            var timeLastNotify = System.nanoTime()
            var priorModified = false
            val bufferedLogLines = arrayListOf<LogLine>()
            var timeout =
                1000000000L / 2 // The timeout is initially small so that the view gets populated immediately.
            val MAX_LINES = (1 shl 16) - 1
            val MAX_BUFFERED_LINES = (1 shl 14) - 1

            while (true) {
                val line = stdout.readLine() ?: break
                if (rawLogLines.size() >= MAX_LINES) rawLogLines.popFirst()
                rawLogLines.addLast(line)
                val logLine = parseLine(line)
                if (logLine != null) {
                    bufferedLogLines.add(logLine)
                } else {
                    if (bufferedLogLines.isNotEmpty()) {
                        bufferedLogLines.last().msg += "\n$line"
                    } else if (!verboseLogLines.isEmpty) {
                        verboseLogLines[verboseLogLines.size() - 1].msg += "\n$line"
                        priorModified = true
                    }
                }
                val timeNow = System.nanoTime()
                if (bufferedLogLines.size < MAX_BUFFERED_LINES && (timeNow - timeLastNotify) < timeout && stdout.ready()) continue
                timeout =
                    1000000000L * 5 / 2 // Increase the timeout after the initial view has something in it.
                timeLastNotify = timeNow

                withContext(Dispatchers.Main.immediate) {
                    val isScrolledToBottomAlready = recyclerView?.canScrollVertically(1) == false
                    if (priorModified) {
                        logAdapter.notifyItemChanged(posStart - 1)
                        priorModified = false
                    }
                    val fullLen = verboseLogLines.size() + bufferedLogLines.size
                    if (fullLen >= MAX_LINES) {
                        val numToRemove = fullLen - MAX_LINES + 1
                        verboseLogLines.removeFromStart(numToRemove)
                        logAdapter.notifyItemRangeRemoved(0, numToRemove)
                        posStart -= numToRemove

                    }
                    for (bufferedLine in bufferedLogLines) when (bufferedLine.level) {
                        "V" -> {
                            verboseLogLines.addLast(bufferedLine)
                        }

                        "D" -> {
                            verboseLogLines.addLast(bufferedLine)
                            debugLogLines.addLast(bufferedLine)
                        }

                        "I" -> {
                            verboseLogLines.addLast(bufferedLine)
                            debugLogLines.addLast(bufferedLine)
                            infoLogLines.addLast(bufferedLine)
                        }

                        "W" -> {
                            verboseLogLines.addLast(bufferedLine)
                            debugLogLines.addLast(bufferedLine)
                            infoLogLines.addLast(bufferedLine)
                            warningLogLines.addLast(bufferedLine)
                        }

                        "E" -> {
                            verboseLogLines.addLast(bufferedLine)
                            debugLogLines.addLast(bufferedLine)
                            infoLogLines.addLast(bufferedLine)
                            warningLogLines.addLast(bufferedLine)
                            errorLogLines.addLast(bufferedLine)
                        }
                    }
                    bufferedLogLines.clear()
                    logAdapter.notifyItemRangeInserted(posStart, adapterLocalList.size() - posStart)
                    posStart = adapterLocalList.size()

                    if (isScrolledToBottomAlready && adapterLocalList.size() > 0) {
                        recyclerView?.scrollToPosition(adapterLocalList.size() - 1)
                    }
                }
            }
        } finally {
            process?.destroy()
        }
    }

    private fun parseTime(timeStr: String): Date? {
        val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
        return try {
            formatter.parse("$year-$timeStr")
        } catch (e: ParseException) {
            null
        }
    }

    private fun parseLine(line: String): LogLine? {
        val m: Matcher = THREADTIME_LINE.matcher(line)
        return if (m.matches()) {
            LogLine(
                m.group(2)!!.toInt(),
                m.group(3)!!.toInt(),
                parseTime(m.group(1)!!),
                m.group(4)!!,
                m.group(5)!!,
                m.group(6)!!
            )
        } else {
            null
        }
    }

    private data class LogLine(
        val pid: Int,
        val tid: Int,
        val time: Date?,
        val level: String,
        val tag: String,
        var msg: String,
    )

    companion object {
        /**
         * Match a single line of `logcat -v threadtime`, such as:
         *
         * <pre>05-26 11:02:36.886 5689 5689 D AndroidRuntime: CheckJNI is OFF.</pre>
         */
        private val THREADTIME_LINE: Pattern =
            Pattern.compile("^(\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3})(?:\\s+[0-9A-Za-z]+)?\\s+(\\d+)\\s+(\\d+)\\s+([A-Z])\\s+(.+?)\\s*: (.*)$")
        private val LOGS: MutableMap<String, ByteArray> = ConcurrentHashMap()
        private lateinit var TAG: String
        private lateinit var appName: String
    }

    private inner class LogEntryAdapter : RecyclerView.Adapter<LogEntryAdapter.ViewHolder>() {
        private val debugColor by lazy {
            ResourcesCompat.getColor(
                resources, R.color.debug_tag_color, theme
            )
        }

        private val defaultColor by lazy {
            ResourcesCompat.getColor(
                resources, R.color.default_color, theme
            )
        }

        private val errorColor by lazy {
            ResourcesCompat.getColor(
                resources, R.color.error_tag_color, theme
            )
        }

        private val infoColor by lazy {
            ResourcesCompat.getColor(
                resources, R.color.info_tag_color, theme
            )
        }

        private val warningColor by lazy {
            ResourcesCompat.getColor(
                resources, R.color.warning_tag_color, theme
            )
        }

        init {
            adapterLocalList = verboseLogLines
        }

        private inner class ViewHolder(val layout: View, var isSingleLine: Boolean = true) :
            RecyclerView.ViewHolder(layout)

        private fun levelToColor(level: String): Int {
            return when (level) {
                "V", "D" -> debugColor
                "E" -> errorColor
                "I" -> infoColor
                "W" -> warningColor
                else -> defaultColor
            }
        }

        override fun getItemCount() = adapterLocalList.size()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.log_viewer_entry, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val line = adapterLocalList[position]
            val spannable =
                if (position > 0 && adapterLocalList[position - 1].tag == line.tag) SpannableString(
                    line.msg
                )
                else SpannableString("${line.tag}: ${line.msg}").apply {
                    setSpan(
                        StyleSpan(BOLD),
                        0,
                        "${line.tag}:".length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    setSpan(
                        ForegroundColorSpan(levelToColor(line.level)),
                        0,
                        "${line.tag}:".length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            holder.layout.apply {
                findViewById<MaterialTextView>(R.id.log_date).text = line.time.toString()
                findViewById<MaterialTextView>(R.id.log_msg).apply {
                    setSingleLine()
                    text = spannable
                    setOnClickListener {
                        isSingleLine = !holder.isSingleLine
                        holder.isSingleLine = !holder.isSingleLine
                    }
                }
                this.setOnLongClickListener {
                    val clipboard: ClipboardManager =
                        getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("log", "${line.tag}: ${line.msg}")
                    clipboard.setPrimaryClip(clip)
                    true
                }
            }
        }

        fun updateList(tag: String, source: String) {
            when (tag) {
                "V" -> {
                    adapterLocalList = verboseLogLines
                }

                "D" -> {
                    adapterLocalList = debugLogLines
                }

                "I" -> {
                    adapterLocalList = infoLogLines
                }

                "W" -> {
                    adapterLocalList = warningLogLines
                }

                "E" -> {
                    adapterLocalList = errorLogLines
                }
            }
            notifyDataSetChanged()
            lifecycleScope.launch {
                delay(500)
                if (adapterLocalList.size() > 0) recyclerView?.scrollToPosition(adapterLocalList.size() - 1)
            }
        }
    }

    class ExportedLogContentProvider : ContentProvider() {
        private fun logForUri(uri: Uri): ByteArray? = LOGS[uri.pathSegments.lastOrNull()]

        override fun insert(uri: Uri, values: ContentValues?): Uri? = null

        override fun query(
            uri: Uri,
            projection: Array<out String>?,
            selection: String?,
            selectionArgs: Array<out String>?,
            sortOrder: String?,
        ): Cursor? = logForUri(uri)?.let {
            val m = MatrixCursor(
                arrayOf(
                    android.provider.OpenableColumns.DISPLAY_NAME,
                    android.provider.OpenableColumns.SIZE
                ), 1
            )
            m.addRow(arrayOf("$appName-log.txt", it.size.toLong()))
            m
        }

        override fun onCreate(): Boolean = true

        override fun update(
            uri: Uri,
            values: ContentValues?,
            selection: String?,
            selectionArgs: Array<out String>?,
        ): Int = 0

        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
            0

        override fun getType(uri: Uri): String? = logForUri(uri)?.let { "text/plain" }

        override fun getStreamTypes(uri: Uri, mimeTypeFilter: String): Array<String>? =
            getType(uri)?.let { if (compareMimeTypes(it, mimeTypeFilter)) arrayOf(it) else null }

        override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
            if (mode != "r") return null
            val log = logForUri(uri) ?: return null
            return openPipeHelper(uri, "text/plain", null, log) { output, _, _, _, l ->
                try {
                    FileOutputStream(output.fileDescriptor).write(l!!)
                } catch (_: Throwable) {
                }
            }
        }
    }
}