package watch.craft

import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import watch.craft.storage.BlobManager
import watch.craft.storage.SubObjectStore
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.max


class ResultsWriter(
  private val store: SubObjectStore,
  private val blobs: BlobManager
) {
  private val logger = KotlinLogging.logger {}
  private val mapper = jacksonObjectMapper().enable(INDENT_OUTPUT)

  fun writeResults(inventory: Inventory) {
    store.write("inventory.json", mapper.writeValueAsBytes(inventory))
    FRONTEND_INVENTORY_FILE.outputStream().use { mapper.writeValue(it, inventory) }

    inventory.items
      .parallelStream()
      .forEach { item -> saveResizedThumbnail(item) }
  }

  private fun saveResizedThumbnail(item: Item) {
    val original = ImageIO.read(ByteArrayInputStream(blobs.read(item.thumbnailKey!!)))

    val maxDim = max(original.width, original.height)
    val targetWidth = (original.width.toDouble() * TARGET_IMG_SIZE / maxDim).toInt()
    val targetHeight = (original.height.toDouble() * TARGET_IMG_SIZE / maxDim).toInt()

    val scaled = original.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH)

    val img = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB)
    img.createGraphics().drawImage(scaled,0,0,null)

    val target = FRONTEND_IMAGES_DIR.resolve("${item.thumbnailKey}.png")
    ImageIO.write(img, "PNG", target)
    logger.info("Image written: ${target}")
  }

  companion object {
    const val TARGET_IMG_SIZE = 200
  }
}
