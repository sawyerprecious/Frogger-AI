import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by sawyerprecious on 2019-04-21.
 */
public class CreateGenomeImage {

    public static void createGenomeImage(Genome genome, String path) {
        Random r = new Random();
        HashMap<Integer, Point> nodeGenePositions = new HashMap<Integer, Point>();
        int nodeSize = 40;
        int connectionSizeBulb = 6;
        int imageSize = 1024;

        BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);

        Graphics g = image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imageSize, imageSize);

        g.setColor(Color.BLUE);
        for (NodeGene gene : genome.getNodes().values()) {
            if (gene.getType() == NodeGene.GENETYPE.INPUT) {
                g.setColor(Color.GREEN);
                float x = ((float)(gene.getId() + 1)/((float)countNodesByType(genome, NodeGene.GENETYPE.INPUT)+1f)) * imageSize;
                float y = imageSize-nodeSize/2;
                g.fillOval((int)(x-nodeSize/2), (int)(y-nodeSize/2), nodeSize, nodeSize);
                nodeGenePositions.put(gene.getId(), new Point((int)x,(int)y));
            } else if (gene.getType() == NodeGene.GENETYPE.HIDDEN) {
                g.setColor(Color.BLUE);
                int x = r.nextInt(imageSize-nodeSize*2)+nodeSize;
                int y = r.nextInt(imageSize-nodeSize*3)+(int)(nodeSize*1.5f);
                g.fillOval((int)(x-nodeSize/2), (int)(y-nodeSize/2), nodeSize, nodeSize);
                nodeGenePositions.put(gene.getId(), new Point((int)x,(int)y));
            } else if (gene.getType() == NodeGene.GENETYPE.OUTPUT) {
                g.setColor(Color.RED);
                float x = ((float)(gene.getId() - 5)/((float)countNodesByType(genome, NodeGene.GENETYPE.OUTPUT)+1f)) * imageSize;
                int y = nodeSize/2;
                g.fillOval((int)(x-nodeSize/2), (int)(y-nodeSize/2), nodeSize, nodeSize);
                nodeGenePositions.put(gene.getId(), new Point((int)x,(int)y));
            }
        }

        g.setColor(Color.BLACK);
        for (ConnectionGene gene : genome.getConnections().values()) {
            if (!gene.isExpressed()) {
                continue;
            }
            Point inNode = nodeGenePositions.get(gene.getInNode());
            Point outNode = nodeGenePositions.get(gene.getOutNode());

            Point lineVector = new Point((int)((outNode.x - inNode.x) * 0.95f), (int)((outNode.y - inNode.y) * 0.95f));

            g.setColor(new Color(gene.getWeight() < 0 ? 1 : 0, 0, 0, Math.min(1, Math.abs(gene.getWeight() / 2))));

            g.drawLine(inNode.x, inNode.y, inNode.x+lineVector.x, inNode.y+lineVector.y);
            g.fillRect(inNode.x+lineVector.x-connectionSizeBulb/2, inNode.y+lineVector.y-connectionSizeBulb/2, connectionSizeBulb, connectionSizeBulb);
//            g.drawString(""+gene.getWeight(), (int)(inNode.x+lineVector.x*0.25f+5), (int)(inNode.y+lineVector.y*0.25f));
        }

        g.setColor(Color.WHITE);
        for (NodeGene nodeGene : genome.getNodes().values()) {
            Point p = nodeGenePositions.get(nodeGene.getId());
            g.drawString(""+nodeGene.getId(), p.x, p.y);
        }


        try {
            ImageIO.write(image, "PNG", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int countNodesByType(Genome genome, NodeGene.GENETYPE type) {
        int c = 0;
        for (NodeGene node : genome.getNodes().values()) {
            if (node.getType() == type) {
                c++;
            }
        }
        return c;
    }




}
