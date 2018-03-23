import java.util.Random;

class Position {
    private float x;
    private float y;

    float getX() {
        return this.x;
    }

    float getY() {
        return this.y;
    }

    Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    Position(int id) {
        Random rand = new Random();
        if(id == 0) {
            this.x = rand.nextInt(15001);
            this.y = 0;
        }
        if(id == 1) {
            this.x = 15000;
            this.y = rand.nextInt(5001);
        }
        if(id == 2) {
            this.x = rand.nextInt(15001);
            this.y = 5000;
        }
        if(id == 3) {
            this.x = 0;
            this.y = rand.nextInt(5001);
        }
    }
}
