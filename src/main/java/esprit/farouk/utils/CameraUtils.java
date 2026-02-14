package esprit.farouk.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;

import java.nio.ByteBuffer;

/**
 * Utility class for camera operations and image conversion.
 * Handles JavaCV Frame/Mat conversions and JavaFX Image creation.
 */
public class CameraUtils {

    /**
     * Creates a camera frame grabber for the specified device.
     *
     * @param deviceIndex Camera device index (0 for default webcam)
     * @return Initialized FrameGrabber
     * @throws FrameGrabber.Exception if camera initialization fails
     */
    public static FrameGrabber createCameraGrabber(int deviceIndex) throws FrameGrabber.Exception {
        FrameGrabber grabber = FrameGrabber.createDefault(deviceIndex);
        grabber.setImageWidth(640);
        grabber.setImageHeight(480);
        grabber.start();
        return grabber;
    }

    /**
     * Converts OpenCV Mat to JavaFX Image for display.
     *
     * @param mat OpenCV Mat object
     * @return JavaFX Image
     */
    public static Image matToImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();

        byte[] buffer = new byte[width * height * channels];
        mat.data().get(buffer);

        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        if (channels == 3) {
            // BGR to RGB conversion
            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int blue = buffer[index++] & 0xFF;
                    int green = buffer[index++] & 0xFF;
                    int red = buffer[index++] & 0xFF;
                    int rgb = (0xFF << 24) | (red << 16) | (green << 8) | blue;
                    pixelWriter.setArgb(x, y, rgb);
                }
            }
        } else if (channels == 1) {
            // Grayscale
            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int gray = buffer[index++] & 0xFF;
                    int rgb = (0xFF << 24) | (gray << 16) | (gray << 8) | gray;
                    pixelWriter.setArgb(x, y, rgb);
                }
            }
        }

        return image;
    }

    /**
     * Converts JavaCV Frame to OpenCV Mat.
     *
     * @param frame JavaCV Frame
     * @return OpenCV Mat
     */
    public static Mat frameToMat(Frame frame) {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        return converter.convert(frame);
    }

    /**
     * Safely releases camera grabber resources.
     *
     * @param grabber FrameGrabber to release
     */
    public static void releaseGrabber(FrameGrabber grabber) {
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                System.err.println("Error releasing camera: " + e.getMessage());
            }
        }
    }
}
