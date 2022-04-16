package com.sparkdata

import java.io.ByteArrayInputStream
import java.nio.file.Files
import com.sparkdata.objects.SerializableFileStatus
import com.sparkdata.utils.FileListing
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, LocalFileSystem, Path}
import org.scalatest.{BeforeAndAfterEach, FunSpec, Matchers}

trait TestSpec extends FunSpec with Matchers with BeforeAndAfterEach {

  var testingBaseDir: java.nio.file.Path = _
  var testingBaseDirName: String = _
  var testingBaseDirPath: Path = _
  var localFileSystem: LocalFileSystem = _

  override def beforeEach(): Unit = {
    super.beforeEach()
    testingBaseDir = Files.createTempDirectory("test_output")
    testingBaseDirName = testingBaseDir.toString
    localFileSystem = FileSystem.getLocal(new Configuration())
    testingBaseDirPath = localFileSystem.makeQualified(new Path(testingBaseDirName))
  }

  override def afterEach(): Unit = {
    super.afterEach()
    FileUtils.deleteDirectory(testingBaseDir.toFile)
  }

  def createFile(relativePath: Path, content: Array[Byte]): SerializableFileStatus = {
    val path = new Path(testingBaseDirPath, relativePath)
    localFileSystem.mkdirs(path.getParent)
    val in = new ByteArrayInputStream(content)
    val out = localFileSystem.create(path)
    IOUtils.copy(in, out)
    in.close()
    out.close()
    SerializableFileStatus(localFileSystem.getFileStatus(path))
  }

  def fileStatusToResult(f: SerializableFileStatus): FileListing = {
    FileListing(f.getPath.toString, if (f.isFile) Some(f.getLen) else None)
  }

}
