package io.github.lordgkram.blobimg;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;

import io.github.lordgkram.GRangedSlider;
import io.github.lordgkram.blobimg.shader.LinkShader;
import io.github.lordgkram.blobimg.shader.ShaderProgramm;
import io.github.lordgkram.blobimg.shader.ShaderType;
import io.github.lordgkram.blobimg.shader.ShaderUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main {

    private JFrame frame;
    private AWTGLCanvas canvas;
    private JPanel panel;

    private JFUtil javaFontUtil;

    @LinkShader(type = ShaderType.VERTEX, data = "/display.vsh")
    @LinkShader(type = ShaderType.FRAGMENT, data = "/display.fsh")
    public ShaderProgramm shaderDisplay;

    @LinkShader(type = ShaderType.VERTEX, data = "/blob.vsh")
    @LinkShader(type = ShaderType.FRAGMENT, data = "/blob.fsh")
    public ShaderProgramm shaderBlob;

    @LinkShader(type = ShaderType.VERTEX, data = "/editStrokePrev.vsh")
    @LinkShader(type = ShaderType.FRAGMENT, data = "/editStrokePrev.fsh")
    public ShaderProgramm editShaderStrokePrev;

    @LinkShader(type = ShaderType.VERTEX, data = "/editStrokeDot.vsh")
    @LinkShader(type = ShaderType.FRAGMENT, data = "/editStrokeDot.fsh")
    public ShaderProgramm editShaderStrokeDot;

    @LinkShader(type = ShaderType.VERTEX, data = "/editStrokeBlurH.vsh")
    @LinkShader(type = ShaderType.FRAGMENT, data = "/editStrokeBlurH.fsh")
    public ShaderProgramm editShaderStrokeBlurH;
    
    @LinkShader(type = ShaderType.VERTEX, data = "/editStrokeBlurV.vsh")
    @LinkShader(type = ShaderType.FRAGMENT, data = "/editStrokeBlurV.fsh")
    public ShaderProgramm editShaderStrokeBlurV;

    private final static int TEXTURE_IDX_OUTPUT = 0;
    private final static int TEXTURE_IDX_TEXT = 1;
    private final static int TEXTURE_IDX_COLOR0 = 2;
    private final static int TEXTURE_IDX_COLOR1 = 3;
    private final static int TEXTURE_IDX_BORDER = 4;
    private final static int TEXTURE_IDX_STAR = 5;

    private GLTexture[] textureArray = new GLTexture[] {
        // glTex width height
        null, // output
        null, // text
        null, // color0
        null, // color1
        null, // border
        null, // startcolor
    };

    private int blobFb;
    
    private Edit edit = null;
    private int currEdit = -1;
    private boolean editClampAlpha = false;
    private float editClampAlphaMin = 0;
    private float editClampAlphaMax = 1;
    private Color editColor;
    private float[] gausx = new float[]{ 1 };
    private float[] gausy = new float[]{ 1 };

    private String text = "Hallo Welt!";
    private boolean textChanged = true;
    private int textBorder = 10;
    private boolean updateText = true;

    private boolean textModifChanged = true;
    private boolean textOutSizeChanged = true;

    private float displayScale = 1;
    private float displayTX = 0;
    private float displayTY = 0;
    private boolean dcup = false;
    private boolean dcdown = false;
    private boolean dcleft = false;
    private boolean dcright = false;

    private boolean editDo = false;
    private double editCX = 0;
    private double editCY = 0;

    private long lastFrame;

    private int displayVerteciesBuffer;
    private int displayVerteciesArray;
    private float[] displayVertecies = new float[]{
        // xy   texture
        0, 1,   0, 0,
        1, 0,   1, 1,
        1, 1,   1, 0,

        0, 1,   0, 0,
        0, 0,   0, 1,
        1, 0,   1, 1,
    };

    private FlatSVGIcon iconLock;
    private FlatSVGIcon iconLockOpen;
    private FlatSVGIcon iconEye;
    private FlatSVGIcon iconEyeSlash;
    private FlatSVGIcon iconPen;
    private FlatSVGIcon iconPenSelected;
    private FlatSVGIcon iconUpload;
    private FlatSVGIcon iconDownload;
    private FlatSVGIcon iconFill;
    private FlatSVGIcon iconDelete;
    private static final float iconSvgScaleFontAwsome = 1f / 16f;

    private int editId = -1;
    private Map<Integer, JButton> editButton;
    private JButton textUpdateButton;
    private Map<Integer, Boolean> viewMap;

    private float blobPerlinGridX = 16;
    private float blobPerlinGridY = 16;
    private float blobPerlinSeed = 0;
    private float blobPerlinLayerExp = 1.5f;
    private int blobPerlinLayers = 1;

    private float blobTextMinAlpha = 0.5f;

    private float blobPixelGridX = 256;
    private float blobPixelGridY = 256;

    private float blobClusterGridX = 32;
    private float blobClusterGridY = 32;
    private float blobClusterSeed = 0;

    private float blobStarGridX = 16;
    private float blobStarGridY = 16;
    private float blobStarSize = 0.125f;
    private float blobStarSeed = 0;

    private boolean export = false;
    private int exportID = -1;
    private boolean doImport = false;
    private int importID = -1;
    private BufferedImage importImage;

    private boolean fillDo = false;;
    private Color fillColor;
    private int fillImmage;

    private boolean deleteDo = false;;
    private Color deleteColor;
    private int deleteImmage;

    private JLabel sizeLable;

    private Main instance;

    public static void main(String[] args) {
        new Main();
    }

    private FlatSVGIcon fromFontAwsome(String path, Color newC) throws IOException {
        FlatSVGIcon out = new FlatSVGIcon(getClass().getResourceAsStream(path)).derive(iconSvgScaleFontAwsome);
        ColorFilter cf = new ColorFilter();
        cf.add(new Color(0, 0, 0), newC);
        out.setColorFilter(cf);
        return out;
    }

    public Main() {
        instance = this;
        start();
    }

    public void start() {
        FlatDarkLaf.setup();
        
        try {
            Color iconColor = UIManager.getColor("Button.foreground");
            Color selectedColor = UIManager.getColor("Button.hoverBorderColor");
            iconLock = fromFontAwsome("/icons/lock.svg", iconColor);
            iconLockOpen = fromFontAwsome("/icons/lock-open.svg", iconColor);
            iconEye = fromFontAwsome("/icons/eye.svg", iconColor);
            iconEyeSlash = fromFontAwsome("/icons/eye-slash.svg", iconColor);
            iconPen = fromFontAwsome("/icons/pen.svg", iconColor);
            iconUpload = fromFontAwsome("/icons/upload.svg", iconColor);
            iconDownload = fromFontAwsome("/icons/download.svg", iconColor);
            iconFill = fromFontAwsome("/icons/fill.svg", iconColor);
            iconDelete = fromFontAwsome("/icons/trash-can.svg", iconColor);

            iconPenSelected = fromFontAwsome("/icons/pen.svg", selectedColor);

            editColor = iconColor;
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame = new JFrame("Blob Image");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(1280, 720));
        GLData data = new GLData();

        javaFontUtil = new JFUtil();
        editButton = new HashMap<>();
        viewMap = new HashMap<>();

        // openGL canvas
        frame.add(canvas = new AWTGLCanvas(data) {
            private static final long serialVersionUID = 1L;
            @Override
            public void initGL() {
                System.out.printf("OpenGL version: %d.%d (Profile: %s)%n", effective.majorVersion, effective.minorVersion, effective.profile);
                GL.createCapabilities();
                GL30.glClearColor(0.3f, 0.4f, 0.5f, 1);
                GL30.glEnable(GL30.GL_CULL_FACE);
                GL30.glEnable(GL30.GL_BLEND);
                GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

                // load shader
                try {
                    ShaderUtil.createProgramms(instance);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                    return;
                }
                // vertexStuff
                // display vertecies
                displayVerteciesArray = GL30.glGenVertexArrays();
                displayVerteciesBuffer = GL30.glGenBuffers();
                GL30.glBindVertexArray(displayVerteciesArray);
                GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, displayVerteciesBuffer);
                GL30.glBufferData(GL30.GL_ARRAY_BUFFER, 6 * 4 * 4, GL30.GL_DYNAMIC_DRAW);
                GL30.glEnableVertexAttribArray(0);
                GL30.glVertexAttribPointer(0, 4, GL30.GL_FLOAT, false, 4 * 4, 0);
                GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);

                textureArray[TEXTURE_IDX_TEXT] = ImgTextureUtil.emptyImmage(1280, 720);
                textureArray[TEXTURE_IDX_COLOR0] = ImgTextureUtil.emptyImmage(1280, 720);
                Fill.fill(textureArray[TEXTURE_IDX_COLOR0], new Color(255, 255, 255, 255));
                textureArray[TEXTURE_IDX_COLOR1] = ImgTextureUtil.emptyImmage(1280, 720);
                Fill.fill(textureArray[TEXTURE_IDX_COLOR1], new Color(255, 255, 255, 255));
                textureArray[TEXTURE_IDX_BORDER] = ImgTextureUtil.emptyImmage(1280, 720);
                Fill.fill(textureArray[TEXTURE_IDX_BORDER], new Color(0, 0, 0, 255));
                textureArray[TEXTURE_IDX_STAR] = ImgTextureUtil.emptyImmage(1280, 720);
                Fill.fill(textureArray[TEXTURE_IDX_STAR], new Color(0, 0, 0, 255));
                textureArray[TEXTURE_IDX_OUTPUT] = ImgTextureUtil.emptyImmage(1280, 720);

                // framebuffer
                blobFb = GL30.glGenFramebuffers();
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, blobFb);
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, textureArray[TEXTURE_IDX_OUTPUT].getGlImmage(), 0);
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

                updateDisplayVertecies();
                lastFrame = System.currentTimeMillis();
            }
            @Override
            public void paintGL() {
                long currFrame = System.currentTimeMillis();
                long delta = currFrame - lastFrame;
                // canvas size handeling
                java.awt.geom.AffineTransform t = getGraphicsConfiguration().getDefaultTransform();
                float sx = (float) t.getScaleX(), sy = (float) t.getScaleY();
                int w = (int) (getWidth() * sx);
                int h = (int) (getHeight() * sy);
                // import
                if(doImport && currEdit < 0) {
                    doImport = false;
                    importIMPL(importID, importImage);
                    importID = -1;
                    importImage = null;
                }
                // delete
                if(deleteDo && currEdit < 0) {
                    deleteDo = false;
                    if(textureArray[deleteImmage] != null) textureArray[deleteImmage].delete();
                    textureArray[deleteImmage] = ImgTextureUtil.emptyImmage(textureArray[TEXTURE_IDX_TEXT].getWidth(), textureArray[TEXTURE_IDX_TEXT].getHeight());
                    Fill.fill(textureArray[deleteImmage], deleteColor);
                    textModifChanged = true;
                    deleteImmage = -1;
                }
                // fill
                if(fillDo && currEdit < 0) {
                    fillDo = false;
                    Fill.fill(textureArray[fillImmage], fillColor);
                    textModifChanged = true;
                    fillImmage = -1;
                }
                // text render
                if(textChanged && currEdit < 0 && updateText) {
                    textChanged = false;
                    if(textureArray[TEXTURE_IDX_TEXT] != null) textureArray[TEXTURE_IDX_TEXT].delete();
                    GLTexture textR = javaFontUtil.render(text, Color.WHITE, textBorder);
                    if(textureArray[TEXTURE_IDX_TEXT].getWidth() != textR.getWidth()) textOutSizeChanged = true;
                    if(textureArray[TEXTURE_IDX_TEXT].getHeight() != textR.getHeight()) textOutSizeChanged = true;
                    textureArray[TEXTURE_IDX_TEXT] = textR;
                    updateDisplayVertecies();
                    textModifChanged = true;
                }
                // editing
                if(editDo && currEdit == -1) startEdit(editId);
                if(currEdit != -1) {
                    Matrix4f projectionc = new Matrix4f().ortho2D(0, w, 0, h).invert();
                    Matrix4f objectc = new Matrix4f().translate(displayTX, displayTY, 0).scale(displayScale).invert();
                    objectc.mul(projectionc);
    
                    Vector4f vec = new Vector4f((float) editCX / (float) w * 2f - 1f, 1f - (float) editCY / (float) h * 2f, 0, 1);
                    vec.mul(objectc);
                    vec.mul((float) textureArray[currEdit].getWidth() / (float) textureArray[TEXTURE_IDX_TEXT].getWidth(), (float) textureArray[currEdit].getHeight() / (float) textureArray[TEXTURE_IDX_TEXT].getHeight(), 1, 1);

                    moveEdit(vec.x, vec.y);

                    if(!editDo) stopEdit();
                }
                // out rezize
                if(textOutSizeChanged) {
                    textOutSizeChanged = false;
                    textModifChanged = true;

                    if(textureArray[TEXTURE_IDX_OUTPUT] != null)
                        textureArray[TEXTURE_IDX_OUTPUT].delete();

                    textureArray[TEXTURE_IDX_OUTPUT] = ImgTextureUtil.emptyImmage(textureArray[TEXTURE_IDX_TEXT].getWidth(), textureArray[TEXTURE_IDX_TEXT].getHeight());
                    // rebind output
                    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, blobFb);
                    GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, textureArray[TEXTURE_IDX_OUTPUT].getGlImmage(), 0);
                    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

                    sizeLable.setText(textureArray[TEXTURE_IDX_TEXT].getWidth() + " x " + textureArray[TEXTURE_IDX_TEXT].getHeight());
                }
                if(textModifChanged) {
                    textModifChanged = false;
                    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, blobFb);
                    GL30.glViewport(0, 0, textureArray[TEXTURE_IDX_TEXT].getWidth(), textureArray[TEXTURE_IDX_TEXT].getHeight());
                    GL30.glClearColor(0f, 0f, 0f, 0f);
                    GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
                    Matrix4f blobProjection = new Matrix4f().ortho2D(0, textureArray[TEXTURE_IDX_TEXT].getWidth(), 0, textureArray[TEXTURE_IDX_TEXT].getHeight());
                    Matrix4f blobObject = new Matrix4f();
                    // -- setup shader
                    shaderBlob.use();
                    shaderBlob.setMat4("object", blobObject);
                    shaderBlob.setMat4("projection", blobProjection);
                    shaderBlob.setInt("text", 0);
                    shaderBlob.setInt("colorA", 1);
                    shaderBlob.setInt("colorB", 2);
                    shaderBlob.setInt("border", 3);
                    shaderBlob.setInt("star", 4);
                    shaderBlob.setBoolean("colorBVis", viewMap.get(TEXTURE_IDX_COLOR1));
                    shaderBlob.setBoolean("borderVis", viewMap.get(TEXTURE_IDX_BORDER));
                    shaderBlob.setBoolean("starVis", viewMap.get(TEXTURE_IDX_STAR));
                    shaderBlob.setVec2("perlinGrid", blobPerlinGridX, blobPerlinGridY);
                    shaderBlob.setFloat("perlinSeed", blobPerlinSeed);
                    shaderBlob.setFloat("perlinLayerExp", blobPerlinLayerExp);
                    shaderBlob.setInt("perlinLayers", blobPerlinLayers);
                    shaderBlob.setFloat("textMinAlpha", blobTextMinAlpha);
                    shaderBlob.setVec2("pixelGrid", blobPixelGridX, blobPixelGridY);
                    shaderBlob.setVec2("clusterGrid", blobClusterGridX, blobClusterGridY);
                    shaderBlob.setFloat("clusterSeed", blobClusterSeed);
                    shaderBlob.setVec2("starGrid", blobStarGridX, blobStarGridY);
                    shaderBlob.setFloat("starSize", blobStarSize);
                    shaderBlob.setFloat("starSeed", blobStarSeed);
                    // TODO: moroptions
                    // -- bind
                    GL30.glBindVertexArray(displayVerteciesArray);
                    GL30.glActiveTexture(GL30.GL_TEXTURE0);
                    GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureArray[TEXTURE_IDX_TEXT].getGlImmage());
                    GL30.glActiveTexture(GL30.GL_TEXTURE1);
                    GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureArray[TEXTURE_IDX_COLOR0].getGlImmage());
                    GL30.glActiveTexture(GL30.GL_TEXTURE2);
                    GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureArray[TEXTURE_IDX_COLOR1].getGlImmage());
                    GL30.glActiveTexture(GL30.GL_TEXTURE3);
                    GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureArray[TEXTURE_IDX_BORDER].getGlImmage());
                    GL30.glActiveTexture(GL30.GL_TEXTURE4);
                    GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureArray[TEXTURE_IDX_STAR].getGlImmage());
                    GL30.glActiveTexture(GL30.GL_TEXTURE0);
                    // -- draw
                    GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
                    // -- unbind
                    GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
                    GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
                }
                // export
                if(export) {
                    export = false;
                    exportIMPL(exportID);
                    exportID = -1;
                }
                // handle input
                displayTX += delta * ((dcright ? 1 : 0) + (dcleft ? -1 : 0));
                displayTY += delta * ((dcup ? 1 : 0) + (dcdown ? -1 : 0));
                if(displayTX > w - 10) displayTX = w - 10;
                else if(displayTX < 10 - textureArray[TEXTURE_IDX_TEXT].getWidth()) displayTX = 10 - textureArray[TEXTURE_IDX_TEXT].getWidth();
                if(displayTY > h - 10) displayTY = h - 10;
                else if(displayTY < 10 - textureArray[TEXTURE_IDX_TEXT].getHeight()) displayTY = 10 - textureArray[TEXTURE_IDX_TEXT].getHeight();
                // render frame
                GL30.glViewport(0, 0, w, h);
                GL30.glClearColor(0.3f, 0.4f, 0.5f, 1);
                GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
                Matrix4f projection = new Matrix4f().ortho2D(0, w, 0, h);
                Matrix4f object = new Matrix4f().translate(displayTX, displayTY, 0).scale(displayScale);
                shaderDisplay.use();
                shaderDisplay.setMat4("object", object);
                shaderDisplay.setMat4("projection", projection);

                GL30.glActiveTexture(GL30.GL_TEXTURE0);
                GL30.glBindVertexArray(displayVerteciesArray);
                textureArray[TEXTURE_IDX_OUTPUT].bind();
                GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);
                GL30.glBindVertexArray(0);
                GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

                //
                swapBuffers();
                lastFrame = currFrame;
            }
        }, BorderLayout.CENTER);

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                editDo = true;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                editDo = false;
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                editCX = e.getPoint().getX();
                editCY = e.getPoint().getY();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                editCX = e.getPoint().getX();
                editCY = e.getPoint().getY();
            }
        });

        canvas.addMouseWheelListener((mwe) -> {
            if(mwe.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                displayScale -= mwe.getPreciseWheelRotation() * 0.05;
                if(displayScale < 0.25) displayScale = 0.25f;
                else if(displayScale > 10) displayScale = 10;
            }
        });

        canvas.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_UP) dcup = true;
                if(e.getKeyCode() == KeyEvent.VK_DOWN) dcdown = true;
                if(e.getKeyCode() == KeyEvent.VK_LEFT) dcleft = true;
                if(e.getKeyCode() == KeyEvent.VK_RIGHT) dcright = true;
            }
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_UP) dcup = false;
                if(e.getKeyCode() == KeyEvent.VK_DOWN) dcdown = false;
                if(e.getKeyCode() == KeyEvent.VK_LEFT) dcleft = false;
                if(e.getKeyCode() == KeyEvent.VK_RIGHT) dcright = false;
                if(e.getKeyCode() == KeyEvent.VK_0 && e.isControlDown()) {
                    displayTX = 0;
                    displayTY = 0;
                    displayScale = 1;
                }
            }
            @Override
            public void keyTyped(KeyEvent e) { }
        });

        // further UI
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        JPanel fontConfigPanel = new JPanel(new GridLayout(0, 2));
        fontConfigPanel.setBorder(BorderFactory.createTitledBorder("Schrift Einstellung"));

        fontConfigPanel.add(new JLabel("Text"));
        JTextArea textArea;
        fontConfigPanel.add(textArea = new JTextArea(text, 2, 10));
        textArea.setLineWrap(true);
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                textChanged = true;
                text = textArea.getText();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                textChanged = true;
                text = textArea.getText();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                textChanged = true;
                text = textArea.getText();
            }
        });

        fontConfigPanel.add(new JLabel("TextSize"));
        JSpinner sizeSpinner;
        fontConfigPanel.add(sizeSpinner = new JSpinner(new SpinnerNumberModel(20, 1, Integer.MAX_VALUE, 1)));
        javaFontUtil.setSize(20);
        sizeSpinner.addChangeListener((ev) -> {
            int val = (int) sizeSpinner.getValue();
            javaFontUtil.setSize(val);
            textChanged = true;
        });
        sizeSpinner.addMouseWheelListener((mwe) -> {
            if(mwe.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                int val = (int) mwe.getPreciseWheelRotation();
                int cabs = Math.abs(val);
                for(int i = 0; i < cabs; i++) {
                    Object next = val < 0 ? sizeSpinner.getModel().getNextValue() : sizeSpinner.getModel().getPreviousValue();
                    if(next != null) sizeSpinner.getModel().setValue(next);
                }
            }
        });
        
        fontConfigPanel.add(new JLabel("Font"));
        JComboBox<String> fontSelect;
        fontConfigPanel.add(fontSelect = new JComboBox<>(javaFontUtil.listFonts()));
        fontSelect.addActionListener((ev) -> {
            javaFontUtil.setFont(fontSelect.getItemAt(fontSelect.getSelectedIndex()), (int) sizeSpinner.getValue());
            textChanged = true;
        });
        {
            String cFont = javaFontUtil.getCurrentFont();
            for(int i = 0; i < fontSelect.getItemCount(); i++) {
                if(cFont.equalsIgnoreCase(fontSelect.getItemAt(i))) fontSelect.setSelectedIndex(i);
            }
        }

        fontConfigPanel.add(new JLabel("Text Border"));
        JSpinner borderSpinner;
        fontConfigPanel.add(borderSpinner = new JSpinner(new SpinnerNumberModel(textBorder, 0, Integer.MAX_VALUE, 1)));
        borderSpinner.addChangeListener((ev) -> {
            int val = (int) borderSpinner.getValue();
            textBorder = val;
            textChanged = true;
        });
        borderSpinner.addMouseWheelListener((mwe) -> {
            if(mwe.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                int val = (int) mwe.getPreciseWheelRotation();
                int cabs = Math.abs(val);
                for(int i = 0; i < cabs; i++) {
                    Object next = val < 0 ? borderSpinner.getModel().getNextValue() : borderSpinner.getModel().getPreviousValue();
                    if(next != null) borderSpinner.getModel().setValue(next);
                }
            }
        });

        fontConfigPanel.add(new JLabel("Size"));
        sizeLable = new JLabel("1280 x 720");
        fontConfigPanel.add(sizeLable);
        panel.add(fontConfigPanel);

        // imageLayer

        textUpdateButton = new JButton(iconLockOpen);
        textUpdateButton.addActionListener((ev) -> {
            updateText = !updateText;
            updateTextUpdateButtion();
        });

        JPanel imageLayerPanel = new JPanel(new GridLayout(0, 1));
        imageLayerPanel.setBorder(BorderFactory.createTitledBorder("Bild Ebenen"));
        imageLayerPanel.add(imageLayer("Text", TEXTURE_IDX_TEXT, true, true, true, true, false, false, false, null));
        imageLayerPanel.add(imageLayer("Text Color 0", TEXTURE_IDX_COLOR0, true, true, true, false, false, true, true, new Color(255, 255, 255, 255)));
        imageLayerPanel.add(imageLayer("Text Color 1", TEXTURE_IDX_COLOR1, true, true, true, false, true, true, true, new Color(255, 255, 255, 255)));
        imageLayerPanel.add(imageLayer("Border Color", TEXTURE_IDX_BORDER, true, true, true, false, true, true, true, new Color(0, 0, 0, 255)));
        imageLayerPanel.add(imageLayer("Star Color", TEXTURE_IDX_STAR, true, true, true, false, true, true, true, new Color(0, 0, 0, 255)));
        imageLayerPanel.add(imageLayer("Output", TEXTURE_IDX_OUTPUT, true, false, false, false, false, false, false, null));
        panel.add(imageLayerPanel);

        // image options

        JPanel imageOptionPanel = new JPanel(new GridLayout(0, 2));
        imageOptionPanel.setBorder(BorderFactory.createTitledBorder("Bild Einstellungen"));
        addNamedSpinnerDouble(imageOptionPanel, "perlin grid X", blobPerlinGridX, 1, 512, 1, (v) -> { blobPerlinGridX = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerDouble(imageOptionPanel, "perlin grid Y", blobPerlinGridY, 1, 512, 1, (v) -> { blobPerlinGridY = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerDouble(imageOptionPanel, "perlin seed", blobPerlinSeed, -1024, 1024, 0.1, (v) -> { blobPerlinSeed = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerDouble(imageOptionPanel, "perlin layer Exponent", blobPerlinLayerExp, 1, 10, 0.1, (v) -> { blobPerlinLayerExp = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerInt(imageOptionPanel, "perlin layers", blobPerlinLayers, 1, 50, 1, (v) -> { blobPerlinLayers = v; textModifChanged = true; });

        imageOptionPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        imageOptionPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        imageOptionPanel.add(new JLabel("Text minimum Alpha"));
        JSlider blobTextMinAlphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, (int) (blobTextMinAlpha * 255));
        blobTextMinAlphaSlider.addChangeListener((e) -> {
            blobTextMinAlpha = (float) blobTextMinAlphaSlider.getValue() / 255f;
            textModifChanged = true;
        });
        imageOptionPanel.add(blobTextMinAlphaSlider);
        
        imageOptionPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        imageOptionPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        addNamedSpinnerDouble(imageOptionPanel, "pixel grid X", blobPixelGridX, 1, 512, 1, (v) -> { blobPixelGridX = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerDouble(imageOptionPanel, "pixel grid Y", blobPixelGridY, 1, 512, 1, (v) -> { blobPixelGridY = v.floatValue(); textModifChanged = true; });
        
        imageOptionPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        imageOptionPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        addNamedSpinnerDouble(imageOptionPanel, "cluster grid X", blobClusterGridX, 1, 512, 1, (v) -> { blobClusterGridX = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerDouble(imageOptionPanel, "cluster grid Y", blobClusterGridY, 1, 512, 1, (v) -> { blobClusterGridY = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerDouble(imageOptionPanel, "cluster seed", blobClusterSeed, -1024, 1024, 0.1, (v) -> { blobClusterSeed = v.floatValue(); textModifChanged = true; });

        imageOptionPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        imageOptionPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        addNamedSpinnerDouble(imageOptionPanel, "star grid X", blobStarGridX, 1, 512, 1, (v) -> { blobStarGridX = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerDouble(imageOptionPanel, "star grid Y", blobStarGridY, 1, 512, 1, (v) -> { blobStarGridY = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerDouble(imageOptionPanel, "star size", blobStarSize, 0, 0.71, 0.01f, (v) -> { blobStarSize = v.floatValue(); textModifChanged = true; });
        addNamedSpinnerDouble(imageOptionPanel, "star seed", blobStarSeed, -1024, 1024, 0.1, (v) -> { blobStarSeed = v.floatValue(); textModifChanged = true; });

        panel.add(imageOptionPanel);

        // brush
        JPanel brushPanel = new JPanel(new GridLayout(0, 2));
        brushPanel.setBorder(BorderFactory.createTitledBorder("Pinsel Einstellung"));

        try {
            JButton editChooseColor = new JButton(fromFontAwsome("/icons/droplet.svg", editColor));
            editChooseColor.addActionListener((ev) -> {
                Color newC = JColorChooser.showDialog(null, "Pinsel Farbe", editColor);
                if(newC != null) {
                    editColor = newC;
                    try {
                        editChooseColor.setIcon(fromFontAwsome("/icons/droplet.svg", editColor));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            brushPanel.add(new JLabel("Farbe"));
            brushPanel.add(editChooseColor);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        JCheckBox editClampAlphaCheckBox = new JCheckBox("Alpha einklemmen");
        GRangedSlider editClampAlphSlider = new GRangedSlider(0, 255);
        editClampAlphSlider.setEnabled(false);
        editClampAlphaCheckBox.addActionListener((ev) -> {
            editClampAlpha = editClampAlphaCheckBox.isSelected();
            editClampAlphSlider.setEnabled(editClampAlpha);
            updateEdits();
        });
        editClampAlphSlider.addChangeListener((ev) -> {
            editClampAlphaMin = editClampAlphSlider.getMinValue() / 255f;
            editClampAlphaMax = editClampAlphSlider.getMaxValue() / 255f;
            updateEdits();
        });
        brushPanel.add(editClampAlphaCheckBox);
        brushPanel.add(editClampAlphSlider);
        // TODO: brush prewiew?
        GausBrushAxisPannel editBrushX = new GausBrushAxisPannel();
        editBrushX.setBorder(BorderFactory.createTitledBorder("Pinsel X"));
        editBrushX.addChangeListener((c) -> {
            gausx = editBrushX.getGaus();
            updateEdits();
        });
        brushPanel.add(editBrushX);
        GausBrushAxisPannel editBrushY = new GausBrushAxisPannel();
        editBrushY.setBorder(BorderFactory.createTitledBorder("Pinsel Y"));
        editBrushY.addChangeListener((c) -> {
            gausy = editBrushY.getGaus();
            updateEdits();
        });
        brushPanel.add(editBrushY);

        panel.add(brushPanel);

        JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        frame.add(scrollPane, BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);
        frame.transferFocus();
        Runnable renderLoop = new Runnable() {
			@Override
            public void run() {
				if (!canvas.isValid()) {
                    GL.setCapabilities(null);
                    return;
                }
				canvas.render();
				SwingUtilities.invokeLater(this);
			}
		};
		SwingUtilities.invokeLater(renderLoop);
    }

    private void updateTextUpdateButtion() {
        if(updateText) {
            textUpdateButton.setIcon(iconLockOpen);
        } else {
            textUpdateButton.setIcon(iconLock);
        }
    }

    private JPanel imageLayer(String name, int textureListIndex, boolean hasExport, boolean hasImport, boolean hasEdit, boolean hasTextUpdate, boolean hasViewButton, boolean hasFillButton, boolean hasDeleteButton, Color deleteColor) {
        JPanel layer = new JPanel(new BorderLayout());
        layer.add(new JLabel(name), BorderLayout.CENTER);
        JPanel buttons = new JPanel();

        if(hasDeleteButton) buttons.add(deleteButton(textureListIndex, deleteColor));
        if(hasFillButton) buttons.add(fillButton(textureListIndex));
        if(hasViewButton) buttons.add(viewButton(textureListIndex));
        if(hasTextUpdate) buttons.add(textUpdateButton);
        if(hasEdit) buttons.add(editButton(textureListIndex));
        if(hasImport) buttons.add(importButton(textureListIndex));
        if(hasExport) buttons.add(exportButton(textureListIndex));

        layer.add(buttons, BorderLayout.EAST);
        return layer;
    }

    private JButton deleteButton(int textureListIndex, Color color) {
        JButton btn = new JButton(iconDelete);
        btn.addActionListener((ev) -> {
            deleteColor = color;
            deleteImmage = textureListIndex;
            deleteDo = true;
        });
        return btn;
    }

    private JButton fillButton(int textureListIndex) {
        JButton btn = new JButton(iconFill);
        btn.addActionListener((ev) -> {
            Color newC = JColorChooser.showDialog(null, "Ebenen Farbe", editColor);
            if(newC != null) {
                fillColor = newC;
                fillImmage = textureListIndex;
                fillDo = true;
            }
        });
        return btn;
    }

    private JButton viewButton(int textureListIndex) {
        JButton btn = new JButton(iconEye);
        viewMap.put(textureListIndex, true);
        btn.addActionListener((ev) -> {
            boolean curr = viewMap.get(textureListIndex);
            curr = !curr;
            viewMap.put(textureListIndex, curr);
            if(curr) {
                btn.setIcon(iconEye);
            } else {
                btn.setIcon(iconEyeSlash);
            }
            textModifChanged = true;
        });
        return btn;
    }

    private JButton editButton(int textureListIndex) {
        if(editButton.containsKey(textureListIndex)) return editButton.get(textureListIndex);
        if(editId == -1) editId = textureListIndex;
        JButton btn = new JButton(iconPen);
        editButton.put(textureListIndex, btn);
        if(editId == textureListIndex) {
            btn.setIcon(iconPenSelected);
        }
        btn.addActionListener((ev) -> {
            editId = textureListIndex;
            // set the color of all buttons
            editButton.forEach((key, value) -> {
                if(key == editId) {
                    value.setIcon(iconPenSelected);
                } else {
                    value.setIcon(iconPen);
                }
            });
        });
        return btn;
    }

    private void exportIMPL(int textureListIndex) {
        if(textureListIndex < TEXTURE_IDX_OUTPUT || textureListIndex > TEXTURE_IDX_STAR) { // don't allow exporting of internal immages
            JOptionPane.showMessageDialog(null, "Das Bild ist nicht zulässig!", "Export Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(textureArray[textureListIndex] == null) { // don't allow exporting of notexistent immages
            JOptionPane.showMessageDialog(null, "Das Bild ist nicht initialisiert!", "Export Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }
        textureArray[textureListIndex].bind();
        int w = textureArray[textureListIndex].getWidth();
        int h = textureArray[textureListIndex].getHeight();

        // create out buffer
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);

        // download texture from gpu to cpu
        GL30.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, 0);
        GL30.glGetTexImage(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);

        /*
        int pbo = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, pbo);
        GL30.glBufferData(GL30.GL_PIXEL_PACK_BUFFER, w * h * 4, GL30.GL_STREAM_COPY);
        GL30.glGetTexImage(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, 0);
        System.out.printf("w: %d, h: %d, len: %d, rem: %d%n", w, h, buffer.capacity(), buffer.remaining());
        GL30.glGetBufferSubData(GL30.GL_PIXEL_PACK_BUFFER, 0, buffer);
        GL30.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, 0);
        GL30.glDeleteBuffers(pbo);
        */

        //buffer.flip();

        // convert buffer RGBA formate to bufferedImage ARGB format
        int[] outNData = new int[w * h];
        for(int i = 0; i < w * h; i++) {
            int i2 = i * 4;
            int pixel = (buffer.get(i2 + 0) & 0xFF) << 16;
            pixel |= (buffer.get(i2 + 1) & 0xFF) << 8;
            pixel |= (buffer.get(i2 + 2) & 0xFF);
            pixel |= (buffer.get(i2 + 3) & 0xFF) << 24;
            outNData[i] = pixel;
        }
        out.setRGB(0, 0, w, h, outNData, 0, w);

        // select file
        new Thread(() -> { exportFile(out); }).start();
    }

    private void exportFile(BufferedImage image) {
        // let the user choose where to export to
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter fnef = new FileNameExtensionFilter("PNG Images", "png");
        chooser.setFileFilter(fnef);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int rtn = chooser.showSaveDialog(null);
        if(rtn == JFileChooser.APPROVE_OPTION) {
            try {
                // export image
                ImageIO.write(image, "png", chooser.getSelectedFile());
                JOptionPane.showMessageDialog(null, "Das Bild wurde gespeichert.", "Export Erfolgreich", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Das Bild konte nicht gespeichert werden!", "Export Fehler", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private JButton exportButton(int textureListIndex) {
        JButton btn = new JButton(iconUpload);
        btn.addActionListener((ev) -> {
            // note information down to export synchonusly to rendering
            export = true;
            exportID = textureListIndex;
        });
        return btn;
    }

    private void importIMPL(int textureListIndex, BufferedImage image) {
        if(textureListIndex < TEXTURE_IDX_TEXT || textureListIndex > TEXTURE_IDX_STAR) {
            new Thread(() -> { JOptionPane.showMessageDialog(null, "Das Bild ist nicht zulässig!", "Import Fehler", JOptionPane.ERROR_MESSAGE); }).start();
            return;
        }
        if(textureArray[textureListIndex] != null) {
            // delete previus texture if it exists
            textureArray[textureListIndex].delete();
            textureArray[textureListIndex] = null;
        }
        // import texture
        textureArray[textureListIndex] = ImgTextureUtil.textureFromBufferedImmage(image);
        if(textureListIndex == TEXTURE_IDX_TEXT) {
            textOutSizeChanged = true;
            updateDisplayVertecies();
            // disable auto text rerendering if importet to text
            updateText = false;
            updateTextUpdateButtion();
        }
        textModifChanged = true;
        new Thread(() -> { JOptionPane.showMessageDialog(null, "Das Bild wurde importiert.", "Import Erfolgreich", JOptionPane.ERROR_MESSAGE); }).start();
    }

    private JButton importButton(int textureListIndex) {
        JButton btn = new JButton(iconDownload);
        btn.addActionListener((ev) -> {
            JFileChooser chooser = new JFileChooser();
            FileNameExtensionFilter fnef = new FileNameExtensionFilter("PNG Images", "png");
            chooser.setFileFilter(fnef);
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int rtn = chooser.showOpenDialog(null);
            if(rtn == JFileChooser.APPROVE_OPTION) {
                boolean ok = false;
                try {
                    // note information to import synchronus
                    importImage = ImageIO.read(chooser.getSelectedFile());
                    ok = true;
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Das Bild konte nicht geladen werden!", "Import Fehler", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    ok = false;
                }
                if(ok) {
                    doImport = true;
                    importID = textureListIndex;
                }
            }
        });
        return btn;
    }

    private void updateDisplayVertecies() {
        displayVertecies[ 1] = textureArray[TEXTURE_IDX_TEXT].getHeight();
        displayVertecies[ 9] = textureArray[TEXTURE_IDX_TEXT].getHeight();
        displayVertecies[13] = textureArray[TEXTURE_IDX_TEXT].getHeight();
        displayVertecies[ 4] = textureArray[TEXTURE_IDX_TEXT].getWidth();
        displayVertecies[ 8] = textureArray[TEXTURE_IDX_TEXT].getWidth();
        displayVertecies[20] = textureArray[TEXTURE_IDX_TEXT].getWidth();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, displayVerteciesBuffer);
        GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, displayVertecies);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        textModifChanged = true;
    }

    public void updateEdit(Edit editTU) {
        editTU.setAlphaClampMax(editClampAlpha ? editClampAlphaMax : 1);
        editTU.setAlphaClampMin(editClampAlpha ? editClampAlphaMin : 0);
        editTU.setColor(editColor);
        editTU.setGausX(gausx);
        editTU.setGausY(gausy);
        editTU.updateInternal();
    }

    public void updateEdits() {
        if(edit != null) updateEdit(edit);
    }

    private void stopEdit() {
        if(edit != null) {
            edit.stop();
            edit = null;
            currEdit = -1;
        }
    }

    private void moveEdit(float x, float y) {
        if(edit != null) {
            edit.update(x, y);
            textModifChanged = true;
        }
    }

    private void startEdit(int editId_) {
        if(edit == null && editId_ > TEXTURE_IDX_OUTPUT && editId_ <= TEXTURE_IDX_STAR) {
            edit = new Edit(textureArray[editId_], editShaderStrokePrev, editShaderStrokeDot, editShaderStrokeBlurH, editShaderStrokeBlurV, editColor, editClampAlpha ? editClampAlphaMin : 0, editClampAlpha ? editClampAlphaMax : 1, gausx, gausy);
            currEdit = editId_;
            textureArray[editId_] = edit.getTarget();
        }
    }

    private void addNamedSpinnerInt(JPanel panelToAdd, String name, int curr, int min, int max, int step, Consumer<Integer> update) {
        panelToAdd.add(new JLabel(name));
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(curr, min, max, step));
        spinner.addChangeListener((ev) -> {
            update.accept((int) spinner.getValue());
        });
        spinner.addMouseWheelListener((mwe) -> {
            if(mwe.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                int val = (int) mwe.getPreciseWheelRotation();
                int cabs = Math.abs(val);
                for(int i = 0; i < cabs; i++) {
                    Object next = val < 0 ? spinner.getModel().getNextValue() : spinner.getModel().getPreviousValue();
                    if(next != null) spinner.getModel().setValue(next);
                }
            }
        });
        panelToAdd.add(spinner);
    }

    private void addNamedSpinnerDouble(JPanel panelToAdd, String name, double curr, double min, double max, double step, Consumer<Double> update) {
        panelToAdd.add(new JLabel(name));
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(curr, min, max, step));
        spinner.addChangeListener((ev) -> {
            update.accept((double) spinner.getValue());
        });
        spinner.addMouseWheelListener((mwe) -> {
            if(mwe.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                int val = (int) mwe.getPreciseWheelRotation();
                int cabs = Math.abs(val);
                for(int i = 0; i < cabs; i++) {
                    Object next = val < 0 ? spinner.getModel().getNextValue() : spinner.getModel().getPreviousValue();
                    if(next != null) spinner.getModel().setValue(next);
                }
            }
        });
        panelToAdd.add(spinner);
    }

}
