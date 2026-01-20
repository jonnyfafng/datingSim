import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.util.Objects;

public class GUI {

    private final JFrame frame;
    private final CardLayout layout;
    private final JPanel cards;
    private JPanel gamePanel;
    private JProgressBar relationshipBar;
    private Character character;
    private BufferedImage bgSharp;
    private BufferedImage bgBlurred;
    private boolean blurBackground = true;

    static void main() {
        SwingUtilities.invokeLater(GUI::new);
    }
    //constructor
    public GUI() {
        frame = new JFrame("Dating Sim");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        loadBackground();

        layout = new CardLayout();
        cards = new JPanel(layout);

        cards.add(menuPanel(), "menu");
        cards.add(selectPanel(), "select");

        frame.setContentPane(cards);
        frame.setVisible(true);
        layout.show(cards, "menu");
    }
    //creates background, makes a blurred version.
    private void loadBackground() {
        Image img = new ImageIcon(
                Objects.requireNonNull(
                        getClass().getResource("/assets/background.png"))
        ).getImage();

        bgSharp = new BufferedImage(
                img.getWidth(null),
                img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = bgSharp.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        bgBlurred = blur(bgSharp);
    }
    //blurs background
    private BufferedImage blur(BufferedImage src) {
        float[] kernel = {
                1, 4, 7, 4, 1,
                4,16,26,16,4,
                7,26,41,26,7,
                4,16,26,16,4,
                1, 4, 7, 4, 1
        };

        for (int i = 0; i < kernel.length; i++) kernel[i] /= 273f;

        BufferedImage dst = new BufferedImage(
                src.getWidth(),
                src.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        new ConvolveOp(new Kernel(5, 5, kernel),
                ConvolveOp.EDGE_NO_OP, null)
                .filter(src, dst);

        return dst;
    }
    //creates the actual background here
    private JPanel backgroundPanel() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage img = blurBackground ? bgBlurred : bgSharp;
                if (img != null) {
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };
        p.setOpaque(false);
        return p;
    }
    //first screen in the game
    private JPanel menuPanel() {
        blurBackground = true;
        JPanel bg = backgroundPanel();
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        JLabel logo = new JLabel(
                new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/gameLogo.png"))));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);


        ImageIcon playIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/playButton.png")));

        JButton playButton = new JButton(playIcon);
        playButton.setBorder(null);
        playButton.setContentAreaFilled(false);
        playButton.setFocusPainted(false);
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        playButton.addActionListener(_ -> layout.show(cards, "select"));


        ImageIcon infoIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/infoButton.png")));

        JButton infoButton = new JButton(infoIcon);
        infoButton.setBorder(null);
        infoButton.setContentAreaFilled(false);
        infoButton.setFocusPainted(false);
        infoButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoButton.addActionListener(_ -> showInfo());
        Box box = Box.createVerticalBox();
        box.add(logo);
        box.add(Box.createVerticalStrut(-100));
        box.add(playButton);
        box.add(Box.createVerticalStrut(20));
        box.add(infoButton);

        content.add(box);
        bg.add(content);

        return bg;
    }
    //info tab, tells you all you need to know
    private void showInfo() {
        String infoText = "<html><body style='width:300px; font-family:Monospaced;'>"
                + "<h2>Dating Sim Instructions</h2>"
                + "<ul>"
                + "<li>In this game, you can interact with any character you want!</li>"
                + "<li>The characters will interact with you through dilemmas, which you have to respond to.</li>"
                + "<li>Your choices affect the relationship bar which is an indicator of what the character thinks of you.</li>"
                + "<li>At the end, you will see an ending based on the character's final opinion on you.</li>"
                +"<li>You can create your own custom character through the 'Custom' character option.</li>"
                +"<li>Enter a .txt file holding all the dialogue you want the character to say, and then enter a 3000x1000 PNG of your spritesheet after.</li>"
                + "</ul>"
                + "</body></html>";

        JOptionPane.showMessageDialog(frame, infoText, "Game Info", JOptionPane.INFORMATION_MESSAGE);
    }
    //this is where you select character
    private JPanel selectPanel() {
        blurBackground = true;
        JPanel bg = backgroundPanel();

        JLabel title = new JLabel("Choose Character", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Monospaced", Font.BOLD, 32));
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        JButton preset = new JButton(
                new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/defaultButton.png"))));
        JButton custom = new JButton(
                new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/customButton.png"))));
        JButton back = new JButton(
                new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/backButton.png"))));

        JButton[] buttons = { preset, custom, back };
        for (JButton b : buttons) {
            b.setBorder(null);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setOpaque(false);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        preset.addActionListener(_ -> startPreset());
        custom.addActionListener(_ -> startCustom());
        back.addActionListener(_ -> layout.show(cards, "menu"));

        Box box = Box.createVerticalBox();
        box.add(Box.createVerticalGlue());
        box.add(title);
        box.add(Box.createVerticalStrut(30));
        box.add(preset);
        box.add(Box.createVerticalStrut(20));
        box.add(custom);
        box.add(Box.createVerticalStrut(20));
        box.add(back);
        box.add(Box.createVerticalGlue());

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(box, BorderLayout.CENTER);

        bg.add(content);
        return bg;
    }
    //this is where the actual gameplay is.
    private JPanel scenarioPanel(Character.Choice choice) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);


        JLabel dilemmaCount = new JLabel("Day " + character.getScenNumber(), SwingConstants.RIGHT);
        dilemmaCount.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        panel.add(dilemmaCount, BorderLayout.NORTH);

        relationshipBar = createRelationshipBar();
        panel.add(relationshipBar, BorderLayout.EAST);

        JLabel spriteLabel = new JLabel("", SwingConstants.CENTER);
        updateSprite(spriteLabel);
        panel.add(spriteLabel, BorderLayout.CENTER);

        JLabel dilemmaText = new JLabel(formatHtml(choice.label));
        JPanel choicePanel = new JPanel(new GridLayout(1, 3, 10, 10));
        choicePanel.setOpaque(false);

        JButton wellBtn = createChoiceButton(choice.wellResponse);
        JButton mehBtn = createChoiceButton(choice.mehResponse);
        JButton badBtn = createChoiceButton(choice.badResponse);

        choicePanel.add(wellBtn);
        choicePanel.add(mehBtn);
        choicePanel.add(badBtn);

        JButton contBtn = new JButton("Continue to next day");
        contBtn.setVisible(false);

        wellBtn.addActionListener(_ -> handleChoice(1, choice.wellAns, dilemmaText, choicePanel, contBtn));
        mehBtn.addActionListener(_ -> handleChoice(0, choice.mehAns, dilemmaText, choicePanel, contBtn));
        badBtn.addActionListener(_ -> handleChoice(-1, choice.badAns, dilemmaText, choicePanel, contBtn));

        JPanel dialogueBox = createDialogueBox(dilemmaText, choicePanel);
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);

        JPanel centeredDialogue = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centeredDialogue.setOpaque(false);
        centeredDialogue.add(dialogueBox);

        bottomWrapper.add(centeredDialogue, BorderLayout.CENTER);
        bottomWrapper.add(contBtn, BorderLayout.SOUTH);

        panel.add(bottomWrapper, BorderLayout.SOUTH);

        return panel;
    }

    private void handleChoice(int alignment, String answer, JLabel dilemmaText, JPanel choices, JButton contBtn) {
        character.applyChoice(alignment);
        dilemmaText.setText(formatHtml(answer));
        relationshipBar.setValue(character.getRelationship());
        for (Component c : choices.getComponents()) c.setEnabled(false);
        contBtn.setVisible(true);
        contBtn.addActionListener(_ -> showNext());
    }
    //brings you to next scenario
    private void showNext() {
        Character.Choice next = character.nextScenario();
        if (next == null) {
            swapGamePanel(endingPanel());
        } else {
            blurBackground = false;
            JPanel bg = backgroundPanel();
            bg.add(scenarioPanel(next));
            swapGamePanel(bg);
        }
    }
   //this shows the ending, chatGPT helped me out a lot with this since this part was irking me a lot
    private JPanel endingPanel() {
        String e = character.getEnding();
        if (e == null || e.isEmpty()) e = "The End.";

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.BLACK);

        JLabel endingLabel = new JLabel("<html><body style='color:white; font-family:Monospaced; text-align:center;'>"
                + e + "</body></html>");
        endingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        endingLabel.setVerticalAlignment(SwingConstants.TOP);

        JPanel labelWrapper = new JPanel(new BorderLayout());
        labelWrapper.setBackground(Color.BLACK);
        labelWrapper.add(endingLabel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(labelWrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.BLACK);

        p.add(scroll, BorderLayout.CENTER);

        JButton back = new JButton("Main Menu");
        back.addActionListener(_ -> layout.show(cards, "menu"));
        p.add(back, BorderLayout.SOUTH);

        return p;
    }
    //this changes the sprite depending on the relationship value
    private void updateSprite(JLabel l) {
        BufferedImage img = character.getExpressionSprite();
        if (img == null) return;

        int newW = (int) (img.getWidth() * 0.50);
        int newH = (int) (img.getHeight() * 0.50);
        Image scaled = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);

        l.setIcon(new ImageIcon(scaled));
    }

    private void swapGamePanel(JPanel p) {
        if (gamePanel != null) cards.remove(gamePanel);
        gamePanel = p;
        cards.add(gamePanel, "game");
        layout.show(cards, "game");
        frame.revalidate();
        frame.repaint();
    }

    private void startPreset() {
        character = Character.createPresetCharacter();
        showNext();
    }
    //this is where you choose files to create a custom character
    private void startCustom() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Scenario Files", "txt"));
        if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        File scenario = fc.getSelectedFile();

        fc.setFileFilter(new FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));
        if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
        File sprite = fc.getSelectedFile();

        character = new Character(sprite.getAbsolutePath(), scenario.getAbsolutePath());
        showNext();
    }
    //this makes a button where you make the choices in the scenarios
    private JButton createChoiceButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("Monospaced", Font.BOLD, 14));
        b.setBackground(new Color(40, 40, 40));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return b;
    }
    //this creates the relationship bar
    private JProgressBar createRelationshipBar() {
        JProgressBar bar = new JProgressBar(-9, 9);
        bar.setValue(character.getRelationship());
        bar.setOrientation(SwingConstants.VERTICAL);
        bar.setPreferredSize(new Dimension(30, 200));
        bar.setBackground(Color.DARK_GRAY);
        bar.setForeground(Color.PINK);
        bar.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        return bar;
    }
    //this is what makes the dialogue box and the name indicator
    private JPanel createDialogueBox(JLabel textLabel, JPanel choices) {
        JPanel outer = new JPanel(null);
        outer.setBackground(Color.BLACK);
        outer.setPreferredSize(new Dimension(900, 220));

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.BLACK);
        box.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        box.setBounds(0, 20, 900, 200);

        textLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        textLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));

        box.add(textLabel, BorderLayout.NORTH);
        box.add(choices, BorderLayout.CENTER);

        JLabel nameTag = new JLabel(" " + character.getName() + " ");
        nameTag.setFont(new Font("Monospaced", Font.BOLD, 14));
        nameTag.setForeground(Color.WHITE);
        nameTag.setBackground(Color.BLACK);
        nameTag.setOpaque(true);
        nameTag.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        nameTag.setBounds(20, 0, nameTag.getPreferredSize().width + 10, 30);

        outer.add(nameTag);
        outer.add(box);

        return outer;
    }

    private String formatHtml(String text) {
        return "<html><body style='color:white; width:850px;'>"
                + text +
                "</body></html>";
    }

}
