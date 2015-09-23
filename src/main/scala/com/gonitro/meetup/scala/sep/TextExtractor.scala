package com.gonitro.meetup.scala.sep

import java.io.File

import org.apache.pdfbox.pdfparser.NonSequentialPDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.PDFTextStripper


object TextExtractor {
  def parseFile(file: File): PDDocument = {
    val doc = new NonSequentialPDFParser(file.getCanonicalPath)
    doc.parse()
    doc.getPDDocument
  }

  def extractText(doc: PDDocument) = {
    val stripper = new PDFTextStripper()
    stripper.getText(doc)
  }
}
