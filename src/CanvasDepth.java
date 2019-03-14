import io.vavr.Function3;
import org.jetbrains.annotations.NotNull;
import rasterdata.*;
import rasterdata.Image;
import rasterops.*;
import solids.CubeEmpty;
import solidops.RenderSolid;
import solids.*;
import transforms.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Function;


public class CanvasDepth {
/*
    TODO
    matice-> ovladani jednotlivych objektu, ne najednou
    prepinani drateneho modelu, ??Novy WireframeRenderer, predavat v Canvasu??
    rozhlizeni mysi, ne klavesnici
 */

    private final static Function3<ZPixel, ZPixel, Double, ZPixel>
            DEPTH_PIXEL_LERP = (v1, v2, s) -> v1.mul(1 - s)
                                                .add(v2.mul(s));
    private final double STEP_ROTATION = 0.1;
    private final double STEP_MOVE = 0.1;
    private Camera cam;
    private final JPanel panel;
    private final Mat4Identity matPersp;
    private final Mat4Identity matOrtho;
    private boolean isPersp;
    private Mat4 matMultiply;
    private Mat4 matView;
    private final @NotNull
    Presenter<ZPixel, Graphics> presenter;
    private final @NotNull
    LineRasterizerLerp<ZPixel, ZPixel>
            lineRasterizerLerp;
    private final @NotNull
    TriangleRasterizer<ZPixel, ZPixel>
            triangleRasterizer;
    private @NotNull
    Image<ZPixel> image;
    private CanvasDepth(final int width, final int height) {
        JFrame frame = new JFrame();
        isPersp = true;
        frame.setLayout(new BorderLayout());
        frame.setTitle("UHK FIM PGRF : " + this.getClass()
                                               .getName());
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
/*
		image = new ImageAWT<>(
			img,
			//Function<PixelType,Integer>, kde PixelType = Color
				(Color c) -> c.getRGB(),
			//Function<Integer,PixelType>, kde PixelType = Color
				(Integer i) -> new Color(i)
		);
		presenter = new ImagePresenterAWT<>();
/*/
        final Image<ZPixel> imageEmpty =
                ImageImmutable.cleared(
                        width, height,
                        new ZPixel(new Col(255, 0, 0), 1));
        image = new ImageBlender<>(imageEmpty,
                (oldValue, newValue) ->
                        oldValue.getZ() < newValue.getZ() ?
                                oldValue : newValue);
        presenter = new PresenterAWT<>(
                zPixel -> zPixel.getCol()
                                .getARGB());
//*/
        lineRasterizerLerp = new LineRasterizerDDALerp<>(
                DEPTH_PIXEL_LERP,
//				(Col v1, Col v2, Double s) -> {
//					return v1.mul(1 - s).add(v2.mul(s));
//				},
                Function.identity()
        );

        triangleRasterizer = new TriangleRasterizerScan<>(
                DEPTH_PIXEL_LERP,
                Function.identity()
        );

        panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                present(g);
            }
        };

        panel.setPreferredSize(new Dimension(width, height));
       /*
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                final int endC = e.getX();
                final int endR = e.getY();
                final double x1 =
                        2 * (startC + 0.5) / image.getWidth() - 1;
                final double y1 =
                        -(2 * (startR + 0.5) / image.getHeight() - 1);
                final double x2 =
                        2 * (endC + 0.5) / image.getWidth() - 1;
                final double y2 =
                        -(2 * (endR + 0.5) / image.getHeight() - 1);
                image = lineRasterizerLerp.rasterize(
                        image.cleared(
                                new ZPixel(new Col(0, 0, 0), 1)),
                        -1, 1, x2, y2,
                        new ZPixel(new Col(1f, 0, 1), 0.5),
                        new ZPixel(new Col(0f, 1, 1), 0.5)
                );
                image = triangleRasterizer.rasterize(image,
                        0, 0, x1, y1, x2, y2,
                        new ZPixel(new Col(1f, 0, 0), 0.9),
                        new ZPixel(new Col(0, 0, 1f), 1.5),
                        new ZPixel(new Col(0f, 1, 0), 0.5)
                );
                panel.repaint();
            }
        });*/

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);

        cam = new Camera()
                .withPosition(new Vec3D(3, 3, 2))
                .withAzimuth(1.25 * Math.PI)
                .withZenith(-Math.atan(1.0 / 5.0))

        ;

        matPersp = new Mat4PerspRH(
                Math.PI / 2.5,
                image.getHeight() / (double) image.getWidth(),
                0.1, 1000
        );
        matOrtho = new Mat4OrthoRH(image.getWidth() / 100.0, image.getHeight() / 100.0, 0.1, 1000);
        matView = matPersp;
        matMultiply = new Mat4Identity();

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {

                    case KeyEvent.VK_W:
                        cam = cam.forward(0.2);
                        break;

                    case KeyEvent.VK_S:
                        cam = cam.backward(0.2);
                        break;

                    case KeyEvent.VK_A:
                        cam = cam.left(0.2);
                        break;

                    case KeyEvent.VK_D:
                        cam = cam.right(0.2);
                        break;


                    case KeyEvent.VK_P:
                        if (isPersp) {
                            matView = matOrtho;
                        } else {
                            matView = matPersp;
                        }
                        isPersp = !isPersp;
                        break;

                    case KeyEvent.VK_N:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Scale(0.75, 0.75, 0.75)));
                        break;
                    case KeyEvent.VK_M:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Scale(1.25, 1.25, 1.25)));
                        break;

                    case KeyEvent.VK_LEFT:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(-STEP_MOVE, 0, 0)));
                        break;
                    case KeyEvent.VK_RIGHT:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(STEP_MOVE, 0, 0)));
                        break;
                    case KeyEvent.VK_UP:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(0, STEP_MOVE, 0)));
                        break;
                    case KeyEvent.VK_DOWN:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(0, -STEP_MOVE, 0)));
                        break;
                    case KeyEvent.VK_K:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(0, 0, STEP_MOVE)));
                        break;
                    case KeyEvent.VK_L:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(0, 0, -STEP_MOVE)));
                        break;
                    case KeyEvent.VK_NUMPAD4:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotX(-STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD6:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotX(STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD8:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotY(STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD2:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotY(-STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD7:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotZ(STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD1:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotZ(-STEP_ROTATION)));
                        break;
                }


                draw();
                panel.repaint();
            }
        });
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new CanvasDepth(800, 600)::start);
    }

    private void clear() {
        image =
                image.cleared(
                        new ZPixel(new Col(0x2f, 0x2f, 0x2f), 1.0));
    }

    private void present(final Graphics graphics) {
        presenter.present(image, graphics);
    }

    private void draw() {
        clear();
        image =
                new RenderSolid<>(
                        lineRasterizerLerp,
                        triangleRasterizer,
                        Function.identity(),
                        (Point3D vertex, Double depth) ->
                                new ZPixel(new Col(vertex), depth),
                        (v1, v2, t) -> v1.mul(1 - t)
                                         .add(v2.mul(t))
                ).render(image, new Cube(),
                        new Mat4Transl(0, 2, 0)
                                .mul(matMultiply)
                                .mul(cam.getViewMatrix())
                                .mul(matView),
                        new ZPixel(new Col(1.0f, 1, 0), 0.5));
        image =
                new RenderSolid<>(
                        lineRasterizerLerp,
                        triangleRasterizer,
                        Function.identity(),
                        (Point3D vertex, Double depth) ->
                                new ZPixel(new Col(vertex), depth),
                        (v1, v2, t) -> v1.mul(1 - t)
                                         .add(v2.mul(t))
                ).render(image, new CubeEmpty(),
                        new Mat4Transl(0, 2, 0)
                                .mul(matMultiply)
                                .mul(cam.getViewMatrix())
                                .mul(matView),
                        new ZPixel(new Col(1.0f, 1, 0), 0.5));


        image =
                new RenderSolid<>(
                        lineRasterizerLerp,
                        triangleRasterizer,
                        Function.identity(),
                        (Point3D vertex, Double depth) ->
                                new ZPixel(new Col(vertex), depth),
                        (v1, v2, t) -> v1.mul(1 - t)
                                         .add(v2.mul(t))
                ).render(image, new PolygonMesh(15),
                        new Mat4Transl(0, 2, 0)
                                .mul(matMultiply)
                                .mul(cam.getViewMatrix())
                                .mul(matView),
                        new ZPixel(new Col(1.0f, 1, 0), 0.5));
        image =
                new RenderSolid<>(
                        lineRasterizerLerp,
                        triangleRasterizer,
                        VertexColNorm::getPos,
                        (VertexColNorm vertex, Double depth) ->
                                new ZPixel(vertex.getCol(), depth),
                        (v1, v2, t) -> v1.mul(1 - t)
                                         .add(v2.mul(t))
                ).render(image, new CubeSolid(),
                        new Mat4Transl(0, 4, 0)
                                .mul(matMultiply)
                                .mul(cam.getViewMatrix())
                                .mul(matView),
                        new ZPixel(new Col(1.0f, 1, 0), 0.5));

        image =
                new RenderSolid<>(
                        lineRasterizerLerp,
                        triangleRasterizer,
                        Function.identity(),
                        (Point3D vertex, Double depth) ->
                                new ZPixel(new Col(255, 0, 0), depth),
                        (v1, v2, t) -> v1.mul(1 - t)
                                         .add(v2.mul(t))
                ).render(image, new AxisX(),
                        new Mat4Transl(0, 2, 0)
                                .mul(cam.getViewMatrix())
                                .mul(matView),
                        new ZPixel(new Col(1.0f, 1, 0), 0.5));
        image =
                new RenderSolid<>(
                        lineRasterizerLerp,
                        triangleRasterizer,
                        Function.identity(),
                        (Point3D vertex, Double depth) ->
                                new ZPixel(new Col(0, 255, 0), depth),
                        (v1, v2, t) -> v1.mul(1 - t)
                                         .add(v2.mul(t))
                ).render(image, new AxisY(),
                        new Mat4Transl(0, 2, 0)
                                .mul(cam.getViewMatrix())
                                .mul(matView),
                        new ZPixel(new Col(1.0f, 1, 0), 0.5));
        image =
                new RenderSolid<>(
                        lineRasterizerLerp,
                        triangleRasterizer,
                        Function.identity(),
                        (Point3D vertex, Double depth) ->
                                new ZPixel(new Col(0, 0, 255), depth),
                        (v1, v2, t) -> v1.mul(1 - t)
                                         .add(v2.mul(t))
                ).render(image, new AxisZ(),
                        new Mat4Transl(0, 2, 0)
                                .mul(cam.getViewMatrix())
                                .mul(matView),
                        new ZPixel(new Col(1.0f, 1, 0), 0.5));

    }

    private void start() {
        draw();
        panel.repaint();
    }

}