import java.util.ArrayList;
import java.util.Random;

/**
 * Created by sawyerprecious on 2019-04-11.
 */
public class Lane {

    private int w;
    public int difficulty;

    public Lane(int width, int diff) {
        w = width;
        difficulty = diff;
    }

    public Object[] getLaneContents() {


        ArrayList temp = new ArrayList();


        // probability of "safety lane"
        Random rnum = new Random();
        int num = rnum.nextInt(difficulty + 1);

        float cSafe = num / 10;

        if (cSafe < 1 || difficulty == 0) {
            for (int i = 0; i < w; i++) {
                temp.add("B");
            }


            Object[] obj = new Object[temp.size()];


            for (int i = 0; i < temp.size(); i++) {
                obj[i] = temp.get(i);
            }

            return obj;
        }


        if (difficulty == 999) {
            for (int i = 0; i < w; i++) {
                temp.add("G");
            }


            Object[] obj = new Object[temp.size()];


            for (int i = 0; i < temp.size(); i++) {
                obj[i] = temp.get(i);
            }

            return obj;
        }


        for (int i = 0; i < w; i++) {


            Random rspot = new Random();
            int n = rspot.nextInt(difficulty);

            float sSafe = n/2;

            if (sSafe < 10) {

                temp.add(" ");

            } else {

                temp.add("0");

            }

        }


        Object[] obj = new Object[temp.size()];


        for (int i = 0; i < temp.size(); i++) {
            obj[i] = temp.get(i);
        }
        return obj;
    }
}
