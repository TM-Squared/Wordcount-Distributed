package core

import java.net.Socket
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class CompressionProtocol extends CommunicationProtocol {
    private def compress(data: String): Array[Byte] = {
        val baos = new ByteArrayOutputStream()
        val gzip = new GZIPOutputStream(baos)
        gzip.write(data.getBytes("UTF-8"))
        gzip.close()
        baos.toByteArray()
    }

    private def decompress(data: Array[Byte]): String = {
        val bais = new ByteArrayInputStream(data)
        val gzip = new GZIPInputStream(bais)
        scala.io.Source.fromInputStream(gzip).mkString
    }

    def send(socket: Socket, message: String): Unit = {
        val out = socket.getOutputStream
        val compressedData = compress(message + "\nEND")
        out.write(compressedData)
        out.flush()
    }

    def receive(socket: Socket): String = {
        val in = socket.getInputStream
        val buffer = new Array[Byte](1024 * 1024)
        val bytesRead = in.read(buffer)
        val message = decompress(buffer.take(bytesRead))
        message.split("\nEND")(0)
    }
}
