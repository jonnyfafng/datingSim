import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Character {
    //this sets the scenario, relationship caps, and the lists used
    private static final int REL_MIN = -9, REL_MAX = 9;
    private int relationship = 0;
    private int scenarioCount = 0;
    private BufferedImage[][] sprites;
    private final List<Choice> choices = new ArrayList<>();
    private final List<String> dialogue;

    //blueprint for how the choice is laid out for you in the actual game
    public static class Choice {
        public String label, wellResponse, mehResponse, badResponse;
        public String wellAns, mehAns, badAns;
        public Choice(String label, String wellR, String wellA, String mehR, String mehA, String badR, String badA) {
            this.label = label; this.wellResponse = wellR; this.wellAns = wellA;
            this.mehResponse = mehR; this.mehAns = mehA; this.badResponse = badR; this.badAns = badA;
        }
    }
    //constructor
    public Character(String spritePath, String scenarioPath) {
        loadSprites(spritePath);
        dialogue = extractTextFromFile(scenarioPath);
        splitScenarios(dialogue);
        scenarioCount = 0;
    }
    //this creates julie for you
    public static Character createPresetCharacter() {
        return new Character(
                Objects.requireNonNull(Character.class.getResource("/assets/spritesheetDefaultChar.png")).getPath(),
                Objects.requireNonNull(Character.class.getResource("/assets/datingSimTemplate.txt")).getPath()
        );
    }
    //this processes the spritesheet into actually readable sprites
    private void loadSprites(String path) {
        try {
            BufferedImage sheet = ImageIO.read(new File(path));
            int w = 1000;
            int h = 1000;
            sprites = new BufferedImage[1][3];

            for (int c = 0; c < 3; c++) {
                sprites[0][c] = sheet.getSubimage(c * w, 0, w, h);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load sprites.", e);
        }
    }
    //this chooses the respective sprite for the relationship value
    public BufferedImage getExpressionSprite() {
        if (relationship >= 3) return sprites[0][2];
        if (relationship <= -3) return sprites[0][1];
        return sprites[0][0];
    }
    //file I/O for the dialogue
    private List<String> extractTextFromFile(String path) {
        List<String> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) out.add(line.trim());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scenario file.");
        }
        return out;
    }
    //separates the large dialogue list
    private void splitScenarios(List<String> data) {
        String[] prefixes = {"goodChoice", "midChoice", "badChoice"};
        for (String prefix : prefixes)
            for (int i = 1; i <= 3; i++) {
                String base = prefix + i;
                String label = getData(data, base);
                if (label.isEmpty()) continue;
                choices.add(new Choice(
                        label,
                        getData(data, base + "wellResponse"), getData(data, base + "wellAns"),
                        getData(data, base + "mehResponse"), getData(data, base + "mehAns"),
                        getData(data, base + "badResponse"), getData(data, base + "badAns")
                ));
            }
    }

    private String getData(List<String> data, String key) {
        for (String s : data)
            if (s.startsWith(key + ":") || s.startsWith(key + "[")) {
                int start = s.indexOf("[");
                int end = s.indexOf("]");
                if (start >= 0 && end > start) return s.substring(start + 1, end).trim();
            }
        return "";
    }
    //selects the next scenario depending on relationship and chance
    public Choice nextScenario() {
        if (scenarioCount++ >= 6 || choices.isEmpty()) return null;
        List<Choice> good = new ArrayList<>();
        List<Choice> neutral = new ArrayList<>();
        List<Choice> bad = new ArrayList<>();

        for (Choice c : choices) {
            String labelLower = c.label.toLowerCase();
            if (labelLower.contains("good")) good.add(c);
            else if (labelLower.contains("mid") || labelLower.contains("neutral")) neutral.add(c);
            else bad.add(c);
        }

        Choice selected;
        int r = relationship;

        if (r > 2 && !good.isEmpty()) {
            selected = good.get(ThreadLocalRandom.current().nextInt(good.size()));
        } else if (r < -2 && !bad.isEmpty()) {
            selected = bad.get(ThreadLocalRandom.current().nextInt(bad.size()));
        } else if (!neutral.isEmpty()) {
            selected = neutral.get(ThreadLocalRandom.current().nextInt(neutral.size()));
        } else {
            selected = choices.get(ThreadLocalRandom.current().nextInt(choices.size()));
        }

        choices.remove(selected);
        return selected;
    }
    //this adds, subtracts, or does nothing to your relationship depending on choice
    public void applyChoice(int alignment) {
        int delta = alignment > 0 ? ThreadLocalRandom.current().nextInt(2, 5)
                : alignment < 0 ? -ThreadLocalRandom.current().nextInt(2, 5)
                : ThreadLocalRandom.current().nextInt(-1, 2);
        relationship = Math.max(REL_MIN, Math.min(REL_MAX, relationship + delta));
    }

    public String getEnding() {
        if (dialogue == null || dialogue.isEmpty()) return "The End.";

        String best = getData(dialogue, "bestEnding");
        String good = getData(dialogue, "goodEnding");
        String neutral = getData(dialogue, "midEnding");
        String bad = getData(dialogue, "badEnding");

        int r = relationship;

        if (r == 9 && !best.isEmpty()) return best;
        else if (r >= 3 && !good.isEmpty()) return good;
        else if (r >= -3 && !neutral.isEmpty()) return neutral;
        else if (!bad.isEmpty()) return bad;

        return "The End.";
    }

    public String getName() {
        if (dialogue == null || dialogue.isEmpty()) return "";

        String line = dialogue.getFirst();

        int start = line.indexOf("[");
        int end = line.indexOf("]");

        if (start >= 0 && end > start) {
            return line.substring(start + 1, end).trim();
        }

        return "";
    }

    public int getScenNumber() { return scenarioCount; }
    public int getRelationship() { return relationship; }
}
