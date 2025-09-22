package no.nordicsemi.android.ei.util

import androidx.annotation.Keep
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import io.runtime.mcumgr.dfu.mcuboot.model.ImageSet
import io.runtime.mcumgr.dfu.mcuboot.model.TargetImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ZipPackage(data: ByteArray) {
    @Suppress("unused")
    @Keep
    private class Manifest {
        private val formatVersion = 0
        val files: Array<File> = arrayOf()

        @Keep
        class File {
            /**
             * The version number of the image. This is a string in the format "X.Y.Z-text".
             */
            private val version: String? = null

            /**
             * The name of the image file.
             */
            val file: String? = null

            /**
             * The size of the image file in bytes. This is declared size and does not have to
             * be equal to the actual file size.
             */
            private val size = 0

            /**
             * Image index is used for multi-core devices. Index 0 is the main core (app core),
             * index 1 is secondary core (net core), etc.
             *
             *
             * For single-core devices this is not present in the manifest file and defaults to 0.
             */
            val imageIndex: Int = 0

            /**
             * The slot number where the image is to be sent. By default images are sent to the
             * secondary slot and then swapped to the primary slot after the image is confirmed
             * and the device is reset.
             *
             *
             * However, if the device supports Direct XIP feature it is possible to run an app
             * from a secondary slot. The image has to be compiled for this slot. A ZIP package
             * can contain images for both slots. Only the one targeting the available one will
             * be sent.
             * @since NCS v 2.5, nRF Connect Device Manager 1.8.
             */
            val slot: Int = TargetImage.SLOT_SECONDARY
        }
    }

    private var manifest: Manifest? = null
    val binaries: ImageSet

    init {
        var ze: ZipEntry
        val entries: MutableMap<String?, ByteArray> = HashMap()

        // Unzip the file and look for the manifest.json.
        val zis = ZipInputStream(ByteArrayInputStream(data))
        while (true) {
            ze = zis.nextEntry ?: break
            if (ze.isDirectory) throw IOException("Invalid ZIP")

            val name = validateFilename(ze.name)

            if (name == MANIFEST) {
                val gson = GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
                manifest = gson.fromJson(InputStreamReader(zis), Manifest::class.java)
            } else if (name.endsWith(".bin")) {
                val content = getData(zis)
                entries[name] = content
            } else {
                throw IOException("Unsupported file found: $name")
            }
        }

        binaries = ImageSet()

        // Search for images.
        for (file in manifest!!.files) {
            val name = file.file
            val content = entries[name] ?: throw IOException("File not found: $name")

            binaries.add(TargetImage(file.imageIndex, file.slot, content))
        }
    }

    @Throws(IOException::class)
    private fun getData(zis: ZipInputStream): ByteArray {
        val buffer = ByteArray(1024)

        // Read file content to byte array
        val os = ByteArrayOutputStream()
        var count: Int
        while ((zis.read(buffer).also { count = it }) != -1) {
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
     * @return The validated path to the file.
     * @throws java.io.IOException Thrown in case of path traversal issues.
     */
    @Throws(IOException::class)
    private fun validateFilename(
        filename: String
    ): String {
        val f = File(filename)
        val canonicalPath = f.canonicalPath

        val iD = File(".")
        val canonicalID = iD.canonicalPath

        if (canonicalPath.startsWith(canonicalID)) {
            return canonicalPath.substring(1) // remove leading "/"
        } else {
            throw IllegalStateException("File is outside extraction target directory.")
        }
    }

    companion object {
        private const val MANIFEST = "manifest.json"
    }
}