package com.pxnc.util

import com.pxnc.scheduler.settings.Settings
import com.pxnc.util.AdvancedTableBuilder.Border

import scala.collection.mutable

/**
  * @author Roman Maksyutov
  */
class AdvancedTableBuilder[T](
                               title: Option[String],
                               obj: T,
                               alignment: TableAlignment,
                               border: Border
                             ) {

  import com.pxnc.util.AdvancedTableBuilder.{Margin, MaxTableLength, MinTableLength, TitleWrap}

  override def toString: String = {

    val s = String.valueOf(obj)
    val (tableLen, maxTextLen, lines) = defineLengthsAndLines(title, s)
    val lineLen = tableLen - border.length * 2

    val sb = new mutable.StringBuilder(tableLen * (title.map(_ => 4).getOrElse(2) + lines.length * maxTextLen))

    //top
    sb.append('\n').append(border.lefTop)
    fill(sb, border.horizontal, lineLen)
    sb.append(border.rightTop).append('\n')

    //title
    title foreach { title =>
      stretch(sb, TitleWrap + title + TitleWrap, border.vertical, border.titleSpace, tableLen, TableAlignments.Center)
      sb.append('\n')

      sb.append(border.leftMiddle)
      fill(sb, border.horizontal, lineLen)
      sb.append(border.rightMiddle).append('\n')
    }

    //body
    lines foreach { lineText =>
      stretch(sb, lineText, border.vertical, border.space, tableLen)
      sb.append('\n')
    }
    sb.setLength(sb.length - 1)

    //bottom
    sb.append('\n').append(border.lefBottom)
    fill(sb, border.horizontal, lineLen)
    sb.append(border.rightBottom).append('\n')

    sb.toString
  }

  def defineLengthsAndLines(title: Option[String], s: String) = {

    val lines = s.split("\n")
    val bordersLen = 2 * (border.length + Margin)
    val titleLen = title map (_.length + (2 * (TitleWrap.length - border.length))) getOrElse 0
    val maxLen = (title.toList ++ lines).foldLeft(1) { case (l, sub) => l.max(sub.length) }.max(titleLen)

    val tableLen = (maxLen + bordersLen).max(MinTableLength).min(MaxTableLength)
    val maxTextLen = tableLen - bordersLen

    val allLines = lines flatMap { line =>
      val len = line.length
      for (i <- 0 to ((len - 1) / maxTextLen))
        yield line.substring(i * maxTextLen, len.min((i + 1) * maxTextLen))
    }

    (tableLen, maxTextLen, allLines)
  }


  private def fill(appendTo: mutable.StringBuilder, s: String, len: Int) {
    for (_ <- 1 to len) {
      appendTo.append(s)
    }
    appendTo.toString
  }

  private def stretch(appendTo: mutable.StringBuilder,
                      text: String,
                      border: String,
                      space: String,
                      length: Int,
                      alignment: TableAlignment = alignment) {
    val textLen = text.length
    val len = length - (border.length * 2)
    val len1 = lengthBefore(alignment, textLen, len)
    val len2 = len - len1 - textLen

    appendTo.append(border)
    fill(appendTo, space, len1)
    appendTo.append(text)
    fill(appendTo, space, len2)
    appendTo.append(border)
  }

  private val lengthBefore: (TableAlignment, Int, Int) => Int = {
    case (TableAlignments.Left, _, _) =>
      Margin

    case (TableAlignments.Center, textLen, len) =>
      (len - textLen) / 2

    case (TableAlignments.Right, textLen, len) =>
      len - Margin - textLen

    case x =>
      throw new IllegalArgumentException(s"Unexpected arguments: $x")

  }

}

object AdvancedTableBuilder {

  object LightBorder extends Border {
    val lefTop = "┌"
    val rightTop = "┐"

    val leftMiddle = "├"
    val rightMiddle = "┤"

    val middleTop = "┬"
    val middleBottom = "┴"

    val lefBottom = "└"
    val rightBottom = "┘"

    val horizontal = "─"
    val vertical = "│"

    val cross = "┼"

    val space = " "

    val titleSpace = "⬝"

    val length = 1
  }

  object RoundBorder extends Border {
    val lefTop = "╭"
    val rightTop = "╮"

    val leftMiddle = "├"
    val rightMiddle = "┤"

    val middleTop = "┬"
    val middleBottom = "┴"

    val lefBottom = "╰"
    val rightBottom = "╯"

    val horizontal = "─"
    val vertical = "│"

    val cross = "┼"

    val space = " "

    val titleSpace = " "

    val length = 1
  }

  object BoldBorder extends Border {
    val lefTop = "┏"
    val rightTop = "┓"

    val leftMiddle = "┣"
    val rightMiddle = "┫"

    val middleTop = "┳"
    val middleBottom = "┻"

    val lefBottom = "┗"
    val rightBottom = "┛"

    val horizontal = "━"
    val vertical = "┃"

    val cross = "╋"

    val space = " "

    val titleSpace = "⬛"

    val length = 1
  }

  object DoubleBorder extends Border {
    val lefTop = "╔"
    val rightTop = "╗"

    val leftMiddle = "╠"
    val rightMiddle = "╣"

    val middleTop = "╦"
    val middleBottom = "╩"

    val lefBottom = "╚"
    val rightBottom = "╝"

    val horizontal = "═"
    val vertical = "║"

    val cross = "╬"

    val space = " "

    val titleSpace = "≡"

    val length = 1
  }


  trait Border {
    def lefTop: String

    def rightTop: String

    def leftMiddle: String

    def rightMiddle: String

    def middleTop: String

    def middleBottom: String

    def lefBottom: String

    def rightBottom: String

    def horizontal: String

    def vertical: String

    def cross: String

    def space: String

    def titleSpace: String

    def length: Int
  }

  private val MinTableLength = Settings.MinTableLength

  private val MaxTableLength = Settings.MaxTableLength

  private val Margin = 1

  private val TitleWrap = "  "

}
