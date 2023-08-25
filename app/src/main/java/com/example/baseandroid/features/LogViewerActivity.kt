package com.example.baseandroid.features

import android.content.ClipDescription.compareMimeTypes
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
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.CircularArray
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.baseandroid.R
import com.example.baseandroid.databinding.ActivityLogViewerBinding
import com.example.baseandroid.utils.logviewer.DownloadsFileSaver
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
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
    private var logLines = CircularArray<LogLine>()
    private var rawLogLines = CircularArray<String>()
    private var recyclerView: RecyclerView? = null
    private var saveButton: MenuItem? = null
    private val year by lazy {
        val yearFormatter: DateFormat = SimpleDateFormat("yyyy", Locale.US)
        yearFormatter.format(Date())
    }
    private val appName by lazy { getString(R.string.app_name) }


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

    private var lastUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAG = appName.plus("\\LogViewerActivity")

        Companion.appName = appName

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
        Log.i(TAG, "onCreate: ")

        binding.shareFab.setOnClickListener {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.log_viewer, menu)
        saveButton = menu.findItem(R.id.save_log)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.save_log -> {
                saveButton?.isEnabled = false
                lifecycleScope.launch { saveLog() }
                Toast.makeText(this, "Logs Saved", Toast.LENGTH_LONG).show()
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
        saveButton?.isEnabled = true
        if (outputFile == null) return null

        return outputFile?.uri
    }

    private suspend fun streamingLog() = withContext(Dispatchers.IO) {
        val builder = ProcessBuilder().command("logcat", "all", "threadtime")
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
                    } else if (!logLines.isEmpty) {
                        logLines[logLines.size() - 1].msg += "\n$line"
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
                    val fullLen = logLines.size() + bufferedLogLines.size
                    if (fullLen >= MAX_LINES) {
                        val numToRemove = fullLen - MAX_LINES + 1
                        logLines.removeFromStart(numToRemove)
                        logAdapter.notifyItemRangeRemoved(0, numToRemove)
                        posStart -= numToRemove

                    }
                    for (bufferedLine in bufferedLogLines) {
                        if (TAG.compareTo(bufferedLine.tag) == 0 || bufferedLine.level == "V")  // add tag or level for filtering here
                            logLines.addLast(bufferedLine)
                    }
                    bufferedLogLines.clear()
                    logAdapter.notifyItemRangeInserted(posStart, logLines.size() - posStart)
                    posStart = logLines.size()

                    if (isScrolledToBottomAlready) {
                        recyclerView?.scrollToPosition(logLines.size() - 1)
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

        override fun getItemCount() = logLines.size()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.log_viewer_entry, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val line = logLines[position]
            val spannable =
                if (position > 0 && logLines[position - 1].tag == line.tag) SpannableString(line.msg)
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
            m.addRow(arrayOf("${Companion.appName}-log.txt", it.size.toLong()))
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