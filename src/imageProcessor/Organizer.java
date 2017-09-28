package imageProcessor;

import static imageProcessor.URLLoader.DELIM;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import intravenous.tools.CroppingTool;

/**
 * Runs an organizer which prompts the user to crop a cloud image and then
 * categorize it by cloud type.
 * @author Gage Davidson
 */
class Organizer extends JFrame {
    
    /**
     * Tracks what button is pressed by the user.
     */
    Cloud typePressed;
    
    private JButton buttonCirrus;
    
    private JButton     buttonCumulus;
    private JButton     buttonOther;
    private JButton     buttonStratus;
    private JButton     buttonWaterspout;
    /**
     * The panel which displays the image.
     */
    private ImageDrawer panelImage;
    
    /**
     * Code mostly generated by the Eclipse plugin WindowBuilder.
     */
    Organizer() {
        setTitle("Cloud Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 780, 585);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        
        JPanel panelButtons = new JPanel();
        contentPane.add(panelButtons, BorderLayout.SOUTH);
        
        buttonCumulus = new JButton("Cumulus");
        buttonCumulus.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                typePressed = Cloud.CUMULUS;
            }
        });
        panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panelButtons.add(buttonCumulus);
        
        buttonCirrus = new JButton("Cirrus");
        buttonCirrus.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                typePressed = Cloud.CIRRUS;
            }
        });
        panelButtons.add(buttonCirrus);
        
        buttonStratus = new JButton("Stratus");
        buttonStratus.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                typePressed = Cloud.STRATUS;
            }
        });
        panelButtons.add(buttonStratus);
        
        buttonWaterspout = new JButton("Waterspout");
        buttonWaterspout.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                typePressed = Cloud.WATERSPOUT;
            }
        });
        panelButtons.add(buttonWaterspout);
        
        buttonOther = new JButton("Other");
        buttonOther.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent evt) {
                typePressed = Cloud.OTHER;
            }
        });
        panelButtons.add(buttonOther);
        
        panelImage = new ImageDrawer();
        contentPane.add(panelImage, BorderLayout.CENTER);
        
        buttonCumulus.setEnabled(false);
        buttonCirrus.setEnabled(false);
        buttonStratus.setEnabled(false);
        buttonWaterspout.setEnabled(false);
        buttonOther.setEnabled(false);
        
        setVisible(true);
    }
    
    /**
     * Runs the organization sequence which iterates through the existing images
     * in the selected images folder. This involves prompting the user to crop
     * each image and then categorize it based on what cloud type is present in
     * the image.
     */
    void runOrganizer() {
        /* initialize all the cloud type folders */
        File imageFolder = new File(URLLoader.imagesDir);
        File cumulusFolder = new File(URLLoader.imagesDir + "cumulus" + DELIM);
        File cirrusFolder = new File(URLLoader.imagesDir + "cirrus" + DELIM);
        File stratusFolder = new File(URLLoader.imagesDir + "stratus" + DELIM);
        File waterspoutFolder = new File(URLLoader.imagesDir + "waterspout" + DELIM);
        File otherFolder = new File(URLLoader.imagesDir + "other" + DELIM);
        
        cumulusFolder.mkdir();
        cirrusFolder.mkdir();
        stratusFolder.mkdir();
        waterspoutFolder.mkdir();
        otherFolder.mkdir();
        
        /* iterate through existing files */
        for (File imageFile : imageFolder.listFiles()) {
            if (imageFile.isDirectory()) continue;
            
            BufferedImage image;
            
            /* load the image */
            try {
                image = ImageIO.read(imageFile);
            } catch (IOException ex) {
                String path = imageFile.getPath();
                System.err.println(
                        "failed to load image " + path.substring(path.lastIndexOf(DELIM)) + ": " + ex.getMessage());
                continue;
            }
            
            /* user crops the image */
            image = CroppingTool.getCrop(image);
            
            /* display the image */
            panelImage.setImage(image);
            panelImage.repaint();
            
            Cloud type;
            
            buttonCumulus.setEnabled(true);
            buttonCirrus.setEnabled(true);
            buttonStratus.setEnabled(true);
            buttonWaterspout.setEnabled(true);
            buttonOther.setEnabled(true);
            
            /* wait for user to press button */
            while (typePressed == null)
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            
            buttonCumulus.setEnabled(false);
            buttonCirrus.setEnabled(false);
            buttonStratus.setEnabled(false);
            buttonWaterspout.setEnabled(false);
            buttonOther.setEnabled(false);
            
            type = typePressed;
            typePressed = null;
            
            File saveFolder;
            
            /* select save folder based on what button user pressed */
            switch (type) {
            case CUMULUS:
                saveFolder = cumulusFolder;
                break;
            case CIRRUS:
                saveFolder = cirrusFolder;
                break;
            case STRATUS:
                saveFolder = stratusFolder;
                break;
            case WATERSPOUT:
                saveFolder = waterspoutFolder;
                break;
            case OTHER:
            default:
                saveFolder = otherFolder;
                break;
            }
            
            /* create new save file path */
            String imagePath = imageFile.getPath();
            String imageName = imagePath.substring(imagePath.lastIndexOf(DELIM));
            
            String newPath = saveFolder.getPath();
            if (!newPath.endsWith(DELIM)) newPath += DELIM;
            
            newPath += imageName;
            
            File newImageFile = new File(newPath);
            
            /* save the image in the new folder */
            try {
                ImageIO.write(image, imageName.substring(imageName.lastIndexOf(".") + 1), newImageFile);
            } catch (IOException ex) {
                System.err.println("failed to load image " + newPath.substring(newPath.lastIndexOf(DELIM)) + ": "
                        + ex.getMessage());
            }
            
            /* delete the old image */
            if (!imageFile.delete())
                System.err.println("failed to delete image " + imagePath.substring(imagePath.lastIndexOf(DELIM)));
        }
    }
}
