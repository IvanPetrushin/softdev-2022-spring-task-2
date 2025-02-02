import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

/**
 * Вывод содержимого указанной в качестве аргумента директории в виде отсортированного списка имен файлов.
 *
 *
 *
Флаг -l (long) переключает вывод в длинный формат, в котором, кроме имени файла, указываются права на выполнение/чтение/запись в виде битовой маски XXX, время последней модификации и размер в байтах.
Флаг -h (human-readable) переключает вывод в человеко-читаемый формат (размер в кило-, мега- или гигабайтах, права на выполнение в виде rwx).
Флаг -r (reverse) меняет порядок вывода на противоположный.
Флаг -o (output) указывает имя файла, в который следует вывести результат; если этот флаг отсутствует, результат выводится в консоль.
В случае, если в качестве аргумента указан файл, а не директория, следует вывести информацию об этом файле.
Command Line: ls [-l] [-h] [-r] [-o output.file] directory_or_file
Actually will be: java -jar ls.jar [-l] [-h] [-r] [-o output.file] directory_or_file
 */

class Ls(private val lFlag: Boolean, private val hFlag: Boolean, private val rFlag: Boolean, private val outputFile: String?, private val fileOrDir: String) {

    private fun xxx(file: File): String {
        val xxx = mutableListOf("0", "0", "0")
        if (file.canRead()) xxx[0] = "1"
        if (file.canWrite()) xxx[1] = "1"
        if (file.canExecute()) xxx[2] = "1"
        return (xxx.joinToString(""))
    }

    private fun rwx(file: File): String {
        val rwx = mutableListOf("-", "-", "-")
        if (file.canRead()) rwx[0] = "r"
        if (file.canWrite()) rwx[1] = "w"
        if (file.canExecute()) rwx[2] = "x"
        return rwx.joinToString("")
    }

    private fun toHumanReadableSize(file: File): String {
        return when (file.length()) {
            in (0..1024) -> file.length().toString() + "B"
            in (1024..1024.0.pow(2).toInt()) -> (file.length() / 1024).toString() + "KB"
            in (1024..1024.0.pow(3).toInt()) -> (file.length() / 1024.0.pow(2).toInt()).toString() + "MB"
            in (1024..1024.0.pow(4).toInt()) -> (file.length() / 1024.0.pow(3).toInt()).toString() + "GB"
            else -> "The size of file is bigger then 1024 GB"
        }
    }

    private fun toFile(file: File): String {
        val nameOfFile = file.name
        var (rwx, xxx, lastModify, size) = listOf("", "", "", "")
        if (lFlag) {
            lastModify = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date(file.lastModified()))
            size = file.length().toString()
            xxx = xxx(file)
        }
        if (hFlag) {
            size = toHumanReadableSize(file)
            rwx = rwx(file)
        }
        return listOf(rwx, xxx, size, lastModify, nameOfFile).joinToString(" ").replace(Regex("""[ ]+"""), " ").trim()
    }

    private fun toFileList(file: File): MutableList<File> {
        val fileList = mutableListOf<File>()
        if (file.isDirectory) file.listFiles()?.forEach { fileList.add(it) }
        else fileList.add(file)
        return fileList.sorted() as MutableList<File>
    }

    private fun toDir(fileList: MutableList<File>): MutableList<String> {
        val sortedList = mutableListOf<String>()
        val actualList = if (rFlag) {
            fileList.asReversed()
        } else fileList
        actualList.forEach { sortedList.add(toFile(it)) }
        return sortedList
    }

    val finalList = toDir(toFileList(File(fileOrDir)))

    @Throws(IOException::class)
    fun fileOutput(stream: OutputStream) = stream.bufferedWriter().use {
        for (file in finalList) {
            it.write(file)
            it.newLine()
        }
    }
}