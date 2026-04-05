package kw.test.file;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

public class PixmapSplitUtil {
    public static void splitAndSave(
            FileHandle src,
            FileHandle outputDir,
            int cols,
            int rows,
            String pre
    ) {
        Pixmap pixmap = new Pixmap(src);
        int tileWidth = pixmap.getWidth() / cols;
        int tileHeight = pixmap.getHeight() / rows;

        outputDir.mkdirs();

        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Pixmap tile = new Pixmap(tileWidth, tileHeight, pixmap.getFormat());

                tile.drawPixmap(
                        pixmap,
                        col * tileWidth, row * tileHeight,
                        tileWidth, tileHeight,
                        0, 0,
                        tileWidth, tileHeight
                );

                FileHandle out = outputDir.child(pre + index+ ".png");
                PixmapIO.writePNG(out, tile);

                tile.dispose();
                index++;
            }
        }

        pixmap.dispose();
    }


}
