package io.udash.demos.files.jetty

import java.io.File
import java.net.URLDecoder
import javax.servlet.http.HttpServletRequest

import io.udash.demos.files.services.FilesStorage
import io.udash.rpc.utils.FileDownloadServlet

class DemoFileDownloadServlet(filesDir: String, contextPrefix: String) extends FileDownloadServlet {
  override protected def resolveFile(request: HttpServletRequest): File = {
    val name = URLDecoder.decode(request.getRequestURI.stripPrefix(contextPrefix + "/"), "UTF-8")
    new File(filesDir, name)
  }

  override protected def presentedFileName(name: String): String =
    FilesStorage.allFiles
      .find(_.serverFileName == name)
      .map(_.name)
      .getOrElse(name)
}
