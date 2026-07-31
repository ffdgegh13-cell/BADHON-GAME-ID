package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

/**
 * A lightweight custom QR-style matrix renderer for Compose canvas.
 * Renders a visual matrix representation with finder patterns and data blocks.
 */
@Composable
fun SimpleQRCodeCanvas(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    codeColor: Color = Color(0xFF0F172A)
) {
    val matrixSize = 25
    val grid = remember(text) { generateSimpleQRMatrix(text, matrixSize) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val cellWidth = size.width / matrixSize
            val cellHeight = size.height / matrixSize

            for (row in 0 until matrixSize) {
                for (col in 0 until matrixSize) {
                    if (grid[row][col]) {
                        drawRect(
                            color = codeColor,
                            topLeft = Offset(col * cellWidth, row * cellHeight),
                            size = Size(cellWidth + 0.5f, cellHeight + 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private fun generateSimpleQRMatrix(text: String, size: Int): Array<BooleanArray> {
    val matrix = Array(size) { BooleanArray(size) }

    // Helper to draw Finder Patterns at corners
    fun drawFinderPattern(startRow: Int, startCol: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuterBorder = r == 0 || r == 6 || c == 0 || c == 6
                val isInnerCenter = r in 2..4 && c in 2..4
                matrix[startRow + r][startCol + c] = isOuterBorder || isInnerCenter
            }
        }
    }

    // Top-Left Finder
    drawFinderPattern(0, 0)
    // Top-Right Finder
    drawFinderPattern(0, size - 7)
    // Bottom-Left Finder
    drawFinderPattern(size - 7, 0)

    // Timing patterns
    for (i in 7 until size - 7) {
        matrix[6][i] = i % 2 == 0
        matrix[i][6] = i % 2 == 0
    }

    // Hash text to deterministically fill inner data cells
    val hash = try {
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
    } catch (e: Exception) {
        text.toByteArray()
    }

    var byteIdx = 0
    var bitIdx = 0

    for (r in 0 until size) {
        for (c in 0 until size) {
            // Skip finder patterns and timing patterns
            val inTopLeftFinder = r < 8 && c < 8
            val inTopRightFinder = r < 8 && c >= size - 8
            val inBottomLeftFinder = r >= size - 8 && c < 8
            val isTiming = r == 6 || c == 6

            if (!inTopLeftFinder && !inTopRightFinder && !inBottomLeftFinder && !isTiming) {
                val currentByte = hash[byteIdx % hash.size].toInt()
                val bit = (currentByte shr (7 - bitIdx)) and 1
                matrix[r][c] = bit == 1

                bitIdx++
                if (bitIdx == 8) {
                    bitIdx = 0
                    byteIdx++
                }
            }
        }
    }

    return matrix
}
