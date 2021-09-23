package no.nordicsemi.android.ei.util

import androidx.annotation.Keep
import androidx.annotation.NonNull
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


class ZipPackage(@NonNull data: ByteArray?) {
    private val MANIFEST = "manifest.json"
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    private lateinit var manifest: Manifest
    var binaries = arrayListOf<android.util.Pair<Int, ByteArray>>()
        private set

    @Keep
    inner class Manifest(val formatVersion: Int, val files: Array<File>) {
        @Keep
        inner class File(
            val version: String,
            val file: String,
            val size: Int = 0,
            @SerializedName("image_index")
            val imageIndex: Int = 0
        )
    }

    init {
        initZipPackage(data = data)
    }

    @Throws(IOException::class)
    private fun initZipPackage(@NonNull data: ByteArray?) {
        var ze: ZipEntry
        val entries: MutableMap<String?, ByteArray> = HashMap()

        // Unzip the file and look for the manifest.json.
        val zis = ZipInputStream(ByteArrayInputStream(data))
        while (true) {
            ze = zis.nextEntry ?: break
            if (ze.isDirectory) throw IOException("Invalid ZIP")
            val name = validateFilename(ze.name)
            when {
                name == MANIFEST -> {
                    manifest = gson.fromJson(InputStreamReader(zis), Manifest::class.java)
                }
                name.endsWith(".bin") -> {
                    val content = zis.readBytes()//getData(zis)
                    entries[name] = content
                }
                else -> {
                    Timber.w("Unsupported file found: %s", name)
                }
            }
        }

        // Search for images.
        manifest.files.onEach { file ->
            val name = file.file
            val content = entries[name] ?: throw IOException("File not found: $name")
            binaries.add(android.util.Pair(file.imageIndex, content))
        }
    }

    @Throws(IOException::class)
    private fun getData(@NonNull zis: ZipInputStream): ByteArray {
        val buffer = ByteArray(1024)

        // Read file content to byte array
        val os = ByteArrayOutputStream()
        var count: Int
        while (zis.read(buffer).also { count = it } != -1) {
            os.write(buffer, 0, count)
        }
        return os.toByteArray()
    }

    /**
     * Validates the path (not the content) of the zip file to prevent path traversal issues.
     *
     *
     *  When unzipping an archive, always validate the compressed files' paths and reject any path
     * that has a path traversal (such as ../..). Simply looking for .. characters in the compressed
     * file's path may not be enough to prevent path traversal issues. The code validates the name of
     * the entry before extracting the entry. If the name is invalid, the entire extraction is aborted.
     *
     *
     *
     * @param filename The path to the file.
     * @param intendedDir The intended directory where the zip should be.
     * @return The validated path to the file.
     * @throws java.io.IOException Thrown in case of path traversal issues.
     */
    @Throws(IOException::class)
    private fun validateFilename(
        @NonNull filename: String,
        @NonNull intendedDir: String = "."
    ): String {
        val f = File(filename)
        val canonicalPath: String = f.canonicalPath
        val iD = File(intendedDir)
        val canonicalID: String = iD.canonicalPath
        return when {
            canonicalPath.startsWith(canonicalID) -> {
                canonicalPath.substring(1) // remove leading "/"
            }
            else -> {
                throw IllegalStateException("File is outside extraction target directory.")
            }
        }
    }

}