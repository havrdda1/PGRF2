package rasterdata;

import io.vavr.collection.Stream;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class PresenterAWT<AnyPixelType>
        implements Presenter<AnyPixelType, Graphics> {
    private final @NotNull
    Function<AnyPixelType, Integer> pixelType2Integer;

    public PresenterAWT(
            final @NotNull Function<AnyPixelType, Integer> pixelType2Integer) {
        this.pixelType2Integer = pixelType2Integer;
    }

    @NotNull
    @Override
    public Graphics present(@NotNull Image<AnyPixelType> image,
                            @NotNull Graphics device) {
        BufferedImage img = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB
        );
//        pro r z rozsahu 0..getHeight-1
//                pro c z rozsahu 0 getWidth-1
//                    img.setRGB(c, r, image.getPixel(c, r));

//        Stream.rangeClosed(0, image.getWidth() - 1).forEach(
        Stream.range(0, image.getHeight())
              .forEach(
                      r -> Stream.range(0, image.getWidth())
                                 .forEach(
                                         c -> image.getPixel(c, r)
                                                   .ifPresent(
                                                           value -> img.setRGB(c, r, pixelType2Integer.apply(value))
                                                   )
                                 )
              );

        device.drawImage(img, 0, 0, null);
        return device;
    }
}

