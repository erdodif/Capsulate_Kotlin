package com.erdodif.capsulate.project

import kotlinx.io.buffered
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteString

class OutOfProjectException(path: Path) :
    Exception("Relative path points outside of the current project: ${SystemFileSystem.resolve(path)}")

operator fun Path.plus(other:Path): Path = Path(this,other.toString())

class Project(val directory: Path) {
    fun listFiles(): Collection<Path> = SystemFileSystem.list(directory)

    private fun assertPathInProject(path: Path) {
        if (SystemFileSystem.resolve(directory).toString() !in SystemFileSystem.resolve(
                path
            ).toString()
        ) throw OutOfProjectException(path)
    }

    fun readFile(path: Path): String? {
        assertPathInProject(path)
        val absolutePath: Path = if (!path.isAbsolute){
            path
        }
        else{
            directory+path
        }
        if (!SystemFileSystem.exists(absolutePath)) return null
        val source = SystemFileSystem.source(absolutePath).buffered()
        val stringBuilder = StringBuilder()
        while(!source.exhausted()){
            val bytestring = source.readByteString()
            stringBuilder.append(bytestring)
        }
        source.close()
        return stringBuilder.toString()
    }

    fun fileExists(path:Path): Boolean{
        assertPathInProject(path)
        return SystemFileSystem.exists(path)
    }

    fun overrideFile(path: Path, content: String){
        assertPathInProject(path)
        if(!fileExists(path))
            throw FileNotFoundException("File not found in given location: $path")
        if(SystemFileSystem.metadataOrNull(path)?.isDirectory == true)
            throw FileNotFoundException("The given location is a directory: $path")
        if(SystemFileSystem.metadataOrNull(path)?.isRegularFile == false)
            throw FileNotFoundException("The given location is not a file: $path")
        val sink = SystemFileSystem.sink(path).buffered()
        val byteArray = content.encodeToByteArray()
        sink.write(byteArray)
    }

    fun saveFileAs(path: Path, content: String){
        assertPathInProject(path)
        if(SystemFileSystem.metadataOrNull(path)?.isDirectory == true)
            throw FileNotFoundException("The given location is a directory: $path")
        if(SystemFileSystem.metadataOrNull(path)?.isRegularFile == false)
            throw FileNotFoundException("The given location is not a file: $path")
        val sink = SystemFileSystem.sink(path).buffered()
        val byteArray = content.encodeToByteArray()
        sink.write(byteArray)
    }
}
